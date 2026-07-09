package com.damoim.app.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.presentation.clubsettings.ClubSettingsRoute
import com.damoim.app.presentation.component.DamoimToastHost
import com.damoim.app.presentation.component.MainTab
import com.damoim.app.presentation.home.HomeRoute
import com.damoim.app.presentation.joinmanage.JoinManageRoute
import com.damoim.app.presentation.notification.NotificationRoute
import com.damoim.app.presentation.theme.DamoimStrings

/** 메인(로그인 이후) 플로우 목적지. */
private sealed interface MainDestination {
    data object Home : MainDestination
    data object ClubSettings : MainDestination
    data object JoinManage : MainDestination
    data object Notification : MainDestination
}

/**
 * 메인(홈) 플로우 호스트. 홈(05/06) + B 서브화면(08/59·09·37/74).
 * 하단 탭·퀵액션 중 미구현(그룹 C~G)은 토스트로 안내한다.
 */
@Composable
fun MainNavHost(role: ClubRole) {
    val backStack: SnapshotStateList<MainDestination> =
        remember { mutableStateListOf(MainDestination.Home) }
    var toast by remember { mutableStateOf<String?>(null) }

    fun navigate(d: MainDestination) = backStack.add(d)
    fun back() { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }

    Box(Modifier.fillMaxSize()) {
        when (backStack.last()) {
            MainDestination.Home -> HomeRoute(
                role = role,
                onNavigateJoinManage = { navigate(MainDestination.JoinManage) },
                onNavigateNotifications = { navigate(MainDestination.Notification) },
                onNavigateClubSettings = { navigate(MainDestination.ClubSettings) },
                onComingSoon = { toast = DamoimStrings.TOAST_COMING_SOON },
                onTabSelect = { tab -> if (tab != MainTab.HOME) toast = DamoimStrings.TOAST_COMING_SOON },
            )

            MainDestination.ClubSettings -> ClubSettingsRoute(
                onBack = { back() },
                onToast = { toast = it },
            )

            MainDestination.JoinManage -> JoinManageRoute(
                onBack = { back() },
                onToast = { toast = it },
            )

            MainDestination.Notification -> NotificationRoute(
                onBack = { back() },
            )
        }

        DamoimToastHost(message = toast, onDismiss = { toast = null })
    }
}
