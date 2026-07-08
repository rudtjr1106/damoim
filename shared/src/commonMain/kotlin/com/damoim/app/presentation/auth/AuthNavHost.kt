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
import com.damoim.app.presentation.auth.joincode.JoinCodeRoute
import com.damoim.app.presentation.auth.login.LoginRoute
import com.damoim.app.presentation.auth.profile.ProfileSetupRoute
import com.damoim.app.presentation.auth.result.JoinCompleteRoute
import com.damoim.app.presentation.auth.result.JoinRejectedRoute
import com.damoim.app.presentation.auth.start.StartRoute
import com.damoim.app.presentation.component.DamoimToastHost
import com.damoim.app.presentation.theme.DamoimStrings

/**
 * A(인증·가입) 플로우 호스트.
 *
 * 정식 Navigation 라이브러리 도입 전까지 [AuthDestination] 백스택을 직접 관리하며,
 * 각 목적지에 대응하는 Route 컴포저블을 렌더한다(Route가 ViewModel·상태·이벤트를 담당).
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
            AuthDestination.Login -> LoginRoute(
                onNavigateProfileSetup = { resetTo(AuthDestination.ProfileSetup) },
                onNavigateStart = { resetTo(AuthDestination.Start) },
                onShowError = { toast = it },
            )

            AuthDestination.ProfileSetup -> ProfileSetupRoute(
                onNavigateStart = { resetTo(AuthDestination.Start) },
            )

            AuthDestination.Start -> StartRoute(
                onJoinWithCode = { navigate(AuthDestination.JoinCode) },
                onCreateClub = { toast = DamoimStrings.TOAST_CREATE_CLUB_TODO },
            )

            AuthDestination.JoinCode -> JoinCodeRoute(
                onBack = { back() },
                onNavigateComplete = { club -> navigate(AuthDestination.JoinComplete(club)) },
                onNavigateRejected = { club, reason -> navigate(AuthDestination.JoinRejected(club, reason)) },
            )

            is AuthDestination.JoinComplete -> JoinCompleteRoute(
                club = current.club,
                onConfirm = {
                    toast = DamoimStrings.TOAST_HOME_TODO
                    resetTo(AuthDestination.Start)
                },
            )

            is AuthDestination.JoinRejected -> JoinRejectedRoute(
                club = current.club,
                reason = current.reason,
                onRetry = { back() },
                onClose = { resetTo(AuthDestination.Start) },
            )
        }

        DamoimToastHost(message = toast, onDismiss = { toast = null })
    }
}
