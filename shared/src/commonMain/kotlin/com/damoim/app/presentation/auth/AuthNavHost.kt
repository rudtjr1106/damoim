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
 * - 가입 코드 제출 결과: APPROVED → 일반회원(MEMBER) 홈, PENDING → 04 대기(홈 진입 없음), REJECTED → 38
 * - [start]로 시작 화면을 지정한다: Login(미로그인) / Start(로그인됐지만 동아리 없음 → 재로그인 없이 생성·가입).
 * - 로그인(01) 성공은 [onLoggedIn]으로 올려 RootNavHost가 (프로필/동아리 유무)로 재판정한다.
 */
@Composable
fun AuthNavHost(
    start: AuthDestination = AuthDestination.Login,
    onLoggedIn: () -> Unit = {},
    onEnterClub: (ClubRole) -> Unit = {},
    // 스택 루트에서 뒤로가기 시 호출(예: 코드로 참여 진입 → 원래 메인으로 복귀). null이면 루트에서 뒤로가기 무시.
    onExit: (() -> Unit)? = null,
) {
    val backStack: SnapshotStateList<AuthDestination> =
        remember { mutableStateListOf(start) }
    var toast by remember { mutableStateOf<String?>(null) }

    fun navigate(destination: AuthDestination) = backStack.add(destination)
    fun back() { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) else onExit?.invoke() }
    fun resetTo(destination: AuthDestination) { backStack.clear(); backStack.add(destination) }

    // 시스템 뒤로가기: 온보딩 스택 pop, 루트에선 onExit(있으면)로 상위 플로우에 위임
    com.damoim.app.platform.PlatformBackHandler(enabled = backStack.size > 1 || onExit != null) { back() }

    Box(Modifier.fillMaxSize()) {
        when (val current = backStack.last()) {
            AuthDestination.Login -> LoginRoute(
                onLoggedIn = onLoggedIn,   // 판정(31/홈/32)은 RootNavHost가 담당
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
                onNavigateComplete = { club -> navigate(AuthDestination.JoinComplete(club)) }, // PENDING → 04
                onNavigateRejected = { club, reason -> navigate(AuthDestination.JoinRejected(club, reason)) }, // → 38
                onNavigateHome = { onEnterClub(ClubRole.MEMBER) },                             // APPROVED → 홈
            )

            is AuthDestination.JoinComplete -> JoinCompleteRoute(
                club = current.club,
                // 04는 승인 대기 화면 — 홈 진입 금지. 확인 시 시작 화면으로 복귀(승인되면 다음 진입 때 홈).
                onConfirm = { resetTo(AuthDestination.Start) },
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
