package com.damoim.app.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.damoim.app.core.di.AppGraph
import com.damoim.app.data.remote.core.SessionEvents
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.presentation.auth.AuthDestination
import com.damoim.app.presentation.auth.AuthNavHost
import com.damoim.app.presentation.theme.DamoimTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** 앱 루트 플로우: 인증(로그인/프로필설정/온보딩) · 메인(홈). */
private sealed interface AppFlow {
    data object Loading : AppFlow                        // 콜드스타트 세션 판정 중(스플래시)
    // returnRole != null이면 "동아리 있는 상태에서 코드로 참여" 진입 — 루트에서 뒤로가기 시 그 역할의 메인으로 복귀.
    data class Auth(val start: AuthDestination, val returnRole: ClubRole? = null) : AppFlow
    data class Main(val role: ClubRole) : AppFlow
}

/**
 * (로그인 여부, 프로필 완료 여부, 활성 동아리 여부)로 시작 플로우를 판정한다.
 * 콜드스타트와 로그인 직후가 **같은 규칙**을 쓰도록 이 함수 하나만 사용한다.
 */
private suspend fun resolveFlow(): AppFlow =
    if (!AppGraph.isLoggedIn) {
        AppFlow.Auth(AuthDestination.Login)                     // 미로그인 → 로그인
    } else {
        val user = AppGraph.getAuthUserUseCase().first()        // /me (로그인 직후엔 방금 받은 값)
        // 역할은 반드시 일회성 조회로 읽는다. observeRole()은 공유 flow(replay=1)이고 화면을 벗어나도
        // 살아있는 VM들이 구독을 유지해 캐시가 리셋되지 않는다 → 재로그인 시점엔 아직 "로그아웃 상태의
        // role=null"이 replay돼, 동아리가 있는데도 온보딩으로 보내는 오판이 난다(재조회는 진행 중).
        val role = AppGraph.fetchMyRoleUseCase()               // /members/me 직접 조회
        when {
            // 저장 토큰은 있었지만 서버가 인증을 거부(계정 삭제·DB 초기화·재사용 폐기 등) →
            // refreshTokens가 refresh 실패로 토큰을 폐기한 상태. 온보딩이 아니라 로그인으로 보낸다.
            // (네트워크 오류/오프라인이면 토큰은 유지되므로 이 분기에 걸리지 않고 아래로 진행)
            !AppGraph.isLoggedIn -> AppFlow.Auth(AuthDestination.Login)
            user.needsProfileSetup -> AppFlow.Auth(AuthDestination.ProfileSetup) // 프로필 미완료 → 31
            role != null -> AppFlow.Main(role)                  // 활성 동아리 있음 → 홈
            else -> AppFlow.Auth(AuthDestination.Start)         // 로그인·프로필 O, 동아리 X → 온보딩
        }
    }

/**
 * 최상위 네비게이션. 콜드스타트에 (로그인 여부, 프로필 완료 여부, 활성 동아리 여부)로 초기 플로우를
 * 결정하고, 이후 전환(생성/가입/탈퇴/로그아웃)은 콜백으로 즉시 처리한다(재조회 지연 깜빡임 방지).
 * 로그인(01) 성공도 [resolveFlow]로 콜드스타트와 동일하게 재판정한다.
 */
@Composable
fun RootNavHost() {
    var flow by remember { mutableStateOf<AppFlow>(AppFlow.Loading) }
    // 판정 코루틴은 반드시 루트 scope에서 — AuthNavHost가 dispose돼도 살아남아야 한다.
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { flow = resolveFlow() }

    // 실행 중 세션 만료(서버가 토큰 거부 → refresh 실패로 폐기) → 즉시 로그인으로.
    // 로그인 성공 후 지난 신호가 되살아나지 않도록 SessionEvents는 replay=0.
    LaunchedEffect(Unit) {
        SessionEvents.expired.collect { flow = AppFlow.Auth(AuthDestination.Login) }
    }

    when (val current = flow) {
        AppFlow.Loading -> SplashScreen()

        is AppFlow.Auth -> AuthNavHost(
            start = current.start,
            // 로그인 성공 → 콜드스타트와 동일 규칙으로 재판정(31 / 홈 / 32).
            // Loading을 한 번 거쳐야 AuthNavHost가 dispose되며 백스택이 새 start로 재생성된다.
            onLoggedIn = {
                scope.launch {
                    flow = AppFlow.Loading   // 판정 중 스플래시(로그인 화면과 동색이라 깜빡임 없음)
                    flow = resolveFlow()
                }
            },
            onEnterClub = { role ->
                AppGraph.enterClubUseCase(role)
                flow = AppFlow.Main(role)
            },
            // 코드로 참여 진입(동아리 보유 상태)에서 루트 뒤로가기 → 원래 메인으로 복귀.
            onExit = current.returnRole?.let { role -> { flow = AppFlow.Main(role) } },
        )

        is AppFlow.Main -> MainNavHost(
            initialRole = current.role,
            onLoggedOut = { flow = AppFlow.Auth(AuthDestination.Login) },       // 로그아웃 → 로그인
            onWithdrewToOnboarding = { flow = AppFlow.Auth(AuthDestination.Start) }, // 탈퇴 후 동아리 없음 → 온보딩
            onAddClub = { flow = AppFlow.Auth(AuthDestination.Start) },         // 33 새 동아리 생성 → 온보딩
            // 33 코드로 참여 → 코드 입력 화면 직행, 뒤로가기 시 원래 메인으로 복귀(returnRole).
            onJoinClub = { flow = AppFlow.Auth(AuthDestination.JoinCode, returnRole = current.role) },
        )
    }
}

/** 콜드스타트 세션 판정 중 표시할 브랜드 스플래시(로그인 화면 깜빡임 방지). */
@Composable
private fun SplashScreen() {
    Box(Modifier.fillMaxSize().background(DamoimTheme.colors.primary))
}
