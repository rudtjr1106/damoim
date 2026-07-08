package com.damoim.app.presentation.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import com.damoim.app.presentation.auth.joincode.JoinCodeScreen
import com.damoim.app.presentation.auth.kakao.KakaoConsentScreen
import com.damoim.app.presentation.auth.login.LoginScreen
import com.damoim.app.presentation.auth.profile.ProfileSetupScreen
import com.damoim.app.presentation.auth.result.JoinCompleteScreen
import com.damoim.app.presentation.auth.result.JoinRejectedScreen
import com.damoim.app.presentation.auth.start.StartScreen
import com.damoim.app.presentation.component.DamoimToastHost

/**
 * A(인증·가입) 플로우 호스트.
 *
 * 정식 Navigation 라이브러리 도입 전까지 [AuthDestination] 백스택을 직접 관리한다.
 * 화면 전환은 각 화면이 노출하는 콜백 → [navigate]/[back]/[resetTo]로 이뤄진다.
 *
 * 그룹 B(홈) 미구현 지점(동아리 생성/홈 진입)은 토스트로 안내한다.
 */
@Composable
fun AuthNavHost() {
    val backStack: SnapshotStateList<AuthDestination> =
        remember { mutableStateListOf(AuthDestination.Login) }
    var toast by remember { mutableStateOf<String?>(null) }

    fun navigate(destination: AuthDestination) = backStack.add(destination)
    fun back() { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
    fun resetTo(destination: AuthDestination) {
        backStack.clear(); backStack.add(destination)
    }

    Box(Modifier.fillMaxSize()) {
        when (val current = backStack.last()) {
            AuthDestination.Login -> LoginScreen(
                onKakaoClick = { navigate(AuthDestination.KakaoConsent) },
            )

            AuthDestination.KakaoConsent -> KakaoConsentScreen(
                onNavigateProfileSetup = { resetTo(AuthDestination.ProfileSetup) },
                onNavigateStart = { resetTo(AuthDestination.Start) },
                onShowError = { toast = it },
                onCancel = { back() },
            )

            AuthDestination.ProfileSetup -> ProfileSetupScreen(
                onNavigateStart = { resetTo(AuthDestination.Start) },
            )

            AuthDestination.Start -> StartScreen(
                onJoinWithCode = { navigate(AuthDestination.JoinCode) },
                onCreateClub = { toast = "동아리 생성은 다음 단계(B 홈/동아리 관리)에서 구현돼요" },
            )

            AuthDestination.JoinCode -> JoinCodeScreen(
                onBack = { back() },
                onNavigateComplete = { club -> navigate(AuthDestination.JoinComplete(club)) },
                onNavigateRejected = { club, reason -> navigate(AuthDestination.JoinRejected(club, reason)) },
            )

            is AuthDestination.JoinComplete -> JoinCompleteScreen(
                club = current.club,
                onConfirm = {
                    toast = "홈 화면은 다음 단계(B 홈/동아리 관리)에서 구현돼요"
                    resetTo(AuthDestination.Start)
                },
            )

            is AuthDestination.JoinRejected -> JoinRejectedScreen(
                club = current.club,
                reason = current.reason,
                onRetry = { back() },
                onClose = { resetTo(AuthDestination.Start) },
            )
        }

        DamoimToastHost(message = toast, onDismiss = { toast = null })
    }
}
