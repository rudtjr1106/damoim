package com.damoim.app.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.presentation.auth.AuthDestination
import com.damoim.app.presentation.auth.AuthNavHost
import com.damoim.app.presentation.theme.DamoimTheme
import kotlinx.coroutines.flow.first

/** 앱 루트 플로우: 인증(로그인/프로필설정/온보딩) · 메인(홈). */
private sealed interface AppFlow {
    data object Loading : AppFlow                        // 콜드스타트 세션 판정 중(스플래시)
    data class Auth(val start: AuthDestination) : AppFlow // 시작 화면 지정: Login / ProfileSetup / Start
    data class Main(val role: ClubRole) : AppFlow
}

/**
 * 최상위 네비게이션. 콜드스타트에 (로그인 여부, 프로필 완료 여부, 활성 동아리 여부)로 초기 플로우를
 * 결정하고, 이후 전환(생성/가입/탈퇴/로그아웃)은 콜백으로 즉시 처리한다(재조회 지연 깜빡임 방지).
 */
@Composable
fun RootNavHost() {
    var flow by remember { mutableStateOf<AppFlow>(AppFlow.Loading) }

    LaunchedEffect(Unit) {
        flow = if (!AppGraph.isLoggedIn) {
            AppFlow.Auth(AuthDestination.Login)                     // 미로그인 → 로그인
        } else {
            val ctx = AppGraph.observeMyContextUseCase().first()   // /me·/members/me 조회(실패 시 401→refresh)
            when {
                // 저장 토큰은 있었지만 서버가 인증을 거부(계정 삭제·DB 초기화·재사용 폐기 등) →
                // refreshTokens가 refresh 실패로 토큰을 폐기한 상태. 온보딩이 아니라 로그인으로 보낸다.
                // (네트워크 오류/오프라인이면 토큰은 유지되므로 이 분기에 걸리지 않고 아래로 진행)
                !AppGraph.isLoggedIn -> AppFlow.Auth(AuthDestination.Login)
                ctx.needsProfileSetup -> AppFlow.Auth(AuthDestination.ProfileSetup) // 프로필 미완료 → 31
                ctx.role != null -> AppFlow.Main(ctx.role)          // 활성 동아리 있음 → 홈
                else -> AppFlow.Auth(AuthDestination.Start)         // 로그인·프로필 O, 동아리 X → 온보딩
            }
        }
    }

    when (val current = flow) {
        AppFlow.Loading -> SplashScreen()

        is AppFlow.Auth -> AuthNavHost(
            start = current.start,
            onEnterClub = { role ->
                AppGraph.enterClubUseCase(role)
                flow = AppFlow.Main(role)
            },
        )

        is AppFlow.Main -> MainNavHost(
            initialRole = current.role,
            onLoggedOut = { flow = AppFlow.Auth(AuthDestination.Login) },       // 로그아웃 → 로그인
            onWithdrewToOnboarding = { flow = AppFlow.Auth(AuthDestination.Start) }, // 탈퇴 후 동아리 없음 → 온보딩
            onAddClub = { flow = AppFlow.Auth(AuthDestination.Start) },         // 33 새 참여/생성 → 온보딩
        )
    }
}

/** 콜드스타트 세션 판정 중 표시할 브랜드 스플래시(로그인 화면 깜빡임 방지). */
@Composable
private fun SplashScreen() {
    Box(Modifier.fillMaxSize().background(DamoimTheme.colors.primary))
}
