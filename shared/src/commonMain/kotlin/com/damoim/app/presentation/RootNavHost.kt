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

/** 앱 루트 플로우: 인증(로그인)·온보딩(생성/가입)·메인(홈). */
private sealed interface AppFlow {
    data object Loading : AppFlow                    // 콜드스타트 세션 판정 중(스플래시)
    data object Auth : AppFlow                       // 미로그인 → 로그인
    data object Onboarding : AppFlow                 // 로그인됨·활성 동아리 없음/추가참여 → 시작(재로그인 X)
    data class Main(val role: ClubRole) : AppFlow
}

/**
 * 최상위 네비게이션. 콜드스타트에 (로그인 여부, 활성 동아리 여부)로 초기 플로우를 결정하고,
 * 이후 전환(생성/가입/탈퇴/로그아웃)은 콜백으로 즉시 처리한다(재조회 지연에 따른 깜빡임 방지).
 */
@Composable
fun RootNavHost() {
    var flow by remember { mutableStateOf<AppFlow>(AppFlow.Loading) }

    LaunchedEffect(Unit) {
        flow = if (!AppGraph.isLoggedIn) {
            AppFlow.Auth
        } else {
            // 로그인됨: 활성 동아리 있으면 홈, 없으면(가입 대기/탈퇴 후) 온보딩.
            val ctx = AppGraph.observeMyContextUseCase().first()
            ctx.role?.let { AppFlow.Main(it) } ?: AppFlow.Onboarding
        }
    }

    when (val current = flow) {
        AppFlow.Loading -> SplashScreen()

        AppFlow.Auth -> AuthNavHost(
            start = AuthDestination.Login,
            onEnterClub = { role ->
                AppGraph.enterClubUseCase(role)
                flow = AppFlow.Main(role)
            },
        )

        AppFlow.Onboarding -> AuthNavHost(
            start = AuthDestination.Start,               // 로그인 건너뛰고 생성/가입부터
            onEnterClub = { role ->
                AppGraph.enterClubUseCase(role)
                flow = AppFlow.Main(role)
            },
        )

        is AppFlow.Main -> MainNavHost(
            initialRole = current.role,
            onLoggedOut = { flow = AppFlow.Auth },              // 로그아웃 → 로그인
            onWithdrewToOnboarding = { flow = AppFlow.Onboarding }, // 탈퇴 후 남은 동아리 없음 → 온보딩
            onAddClub = { flow = AppFlow.Onboarding },          // 33 새 참여/생성 → 온보딩(세션 유지)
        )
    }
}

/** 콜드스타트 세션 판정 중 표시할 브랜드 스플래시(로그인 화면 깜빡임 방지). */
@Composable
private fun SplashScreen() {
    Box(Modifier.fillMaxSize().background(DamoimTheme.colors.primary))
}
