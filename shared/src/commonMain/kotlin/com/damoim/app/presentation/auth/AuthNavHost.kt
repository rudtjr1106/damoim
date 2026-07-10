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
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.presentation.auth.joincode.JoinCodeRoute
import com.damoim.app.presentation.auth.login.LoginRoute
import com.damoim.app.presentation.auth.profile.ProfileSetupRoute
import com.damoim.app.presentation.auth.result.JoinCompleteRoute
import com.damoim.app.presentation.auth.result.JoinRejectedRoute
import com.damoim.app.presentation.auth.start.StartRoute
import com.damoim.app.presentation.clubcreate.ClubCreateRoute
import com.damoim.app.presentation.component.DamoimToastHost

/**
 * A(인증·가입) 플로우 호스트. 온보딩이 끝나면 [onEnterClub]로 메인(홈) 플로우로 넘긴다.
 * - 동아리 생성(07) 완료 → 동아리장(LEADER)으로 홈 진입
 * - 가입 신청 완료(04) 확인 → (데모) 일반회원(MEMBER)으로 홈 진입
 */
@Composable
fun AuthNavHost(
    onEnterClub: (ClubRole) -> Unit = {},
) {
    val backStack: SnapshotStateList<AuthDestination> =
        remember { mutableStateListOf(AuthDestination.Login) }
    var toast by remember { mutableStateOf<String?>(null) }

    fun navigate(destination: AuthDestination) = backStack.add(destination)
    fun back() { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
    fun resetTo(destination: AuthDestination) { backStack.clear(); backStack.add(destination) }

    // 시스템 뒤로가기: 온보딩 스택 pop (루트=로그인에선 기본 동작)
    com.damoim.app.platform.PlatformBackHandler(enabled = backStack.size > 1) { back() }

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
                onCreateClub = { navigate(AuthDestination.ClubCreate) },
            )

            AuthDestination.ClubCreate -> ClubCreateRoute(
                onBack = { back() },
                onCreated = { onEnterClub(ClubRole.LEADER) },
                onError = { toast = it },
            )

            AuthDestination.JoinCode -> JoinCodeRoute(
                onBack = { back() },
                onCreateClub = { navigate(AuthDestination.ClubCreate) },
                onNavigateComplete = { club -> navigate(AuthDestination.JoinComplete(club)) },
                onNavigateRejected = { club, reason -> navigate(AuthDestination.JoinRejected(club, reason)) },
            )

            is AuthDestination.JoinComplete -> JoinCompleteRoute(
                club = current.club,
                onConfirm = { onEnterClub(ClubRole.MEMBER) },
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
