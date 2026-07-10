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
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.presentation.board.BoardHomeRoute
import com.damoim.app.presentation.board.BoardListRoute
import com.damoim.app.presentation.board.PostDetailRoute
import com.damoim.app.presentation.board.PostWriteRoute
import com.damoim.app.presentation.board.SearchRoute
import com.damoim.app.presentation.clubsettings.ClubSettingsRoute
import com.damoim.app.presentation.component.DamoimToastHost
import com.damoim.app.presentation.component.MainTab
import com.damoim.app.presentation.home.HomeRoute
import com.damoim.app.presentation.joinmanage.JoinManageRoute
import com.damoim.app.presentation.notification.NotificationRoute
import com.damoim.app.presentation.theme.DamoimStrings

/** 메인(로그인 이후) 플로우 목적지. */
private sealed interface MainDestination {
    // 탭 루트(하단 탭바 표시)
    data object Home : MainDestination                                  // 05/06
    data object BoardHome : MainDestination                            // 10 (게시판 탭)
    // 게시판 서브(푸시)
    data class BoardList(val category: BoardCategory) : MainDestination // 11/12/13
    data class PostDetail(val postId: Long) : MainDestination          // 14/36/79/84
    data class PostWrite(val category: BoardCategory, val editPostId: Long? = null) : MainDestination // 15/34/35/39/70 (+수정)
    data object Search : MainDestination                               // 85/40/76
    // B 서브
    data object ClubSettings : MainDestination
    data object JoinManage : MainDestination
    data object Notification : MainDestination
}

/**
 * 메인(홈) 플로우 호스트. 홈(05/06) + 게시판(C: 10·11/12/13·14/36·15) + B 서브화면.
 * 하단 탭 중 홈·게시판만 실제 화면이 있고 일정·회원·설정은 토스트로 안내한다.
 */
@Composable
fun MainNavHost(role: ClubRole) {
    val backStack: SnapshotStateList<MainDestination> =
        remember { mutableStateListOf<MainDestination>(MainDestination.Home) }
    var toast by remember { mutableStateOf<String?>(null) }

    fun navigate(d: MainDestination) = backStack.add(d)
    fun back() { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
    fun resetTo(d: MainDestination) { backStack.clear(); backStack.add(d) }
    fun onTab(tab: MainTab) = when (tab) {
        MainTab.HOME -> resetTo(MainDestination.Home)
        MainTab.BOARD -> resetTo(MainDestination.BoardHome)
        else -> { toast = DamoimStrings.TOAST_COMING_SOON }
    }

    Box(Modifier.fillMaxSize()) {
        when (val current = backStack.last()) {
            MainDestination.Home -> HomeRoute(
                role = role,
                onNavigateJoinManage = { navigate(MainDestination.JoinManage) },
                onNavigateNotifications = { navigate(MainDestination.Notification) },
                onNavigateClubSettings = { navigate(MainDestination.ClubSettings) },
                onComingSoon = { label ->
                    // 홈 퀵액션 '게시판' → 게시판 탭으로, 그 외는 준비중 토스트
                    if (label == DamoimStrings.QA_BOARD) resetTo(MainDestination.BoardHome)
                    else toast = DamoimStrings.TOAST_COMING_SOON
                },
                onTabSelect = { tab -> onTab(tab) },
            )

            MainDestination.BoardHome -> BoardHomeRoute(
                onOpenPost = { id -> navigate(MainDestination.PostDetail(id)) },
                onOpenCategory = { category -> navigate(MainDestination.BoardList(category)) },
                onSearch = { navigate(MainDestination.Search) },
                onWrite = { navigate(MainDestination.PostWrite(BoardCategory.FREE)) },
                onTabSelect = { tab -> onTab(tab) },
            )

            is MainDestination.BoardList -> BoardListRoute(
                category = current.category,
                onBack = { back() },
                onOpenPost = { id -> navigate(MainDestination.PostDetail(id)) },
                onSearch = { navigate(MainDestination.Search) },
            )

            MainDestination.Search -> SearchRoute(
                onBack = { back() },
                onOpenPost = { id -> navigate(MainDestination.PostDetail(id)) },
                onComingSoon = { toast = DamoimStrings.TOAST_COMING_SOON },
            )

            is MainDestination.PostDetail -> PostDetailRoute(
                postId = current.postId,
                onBack = { back() },
                onEdit = { post -> navigate(MainDestination.PostWrite(post.category, editPostId = post.id)) },
                onToast = { toast = it },
            )

            is MainDestination.PostWrite -> PostWriteRoute(
                initialCategory = current.category,
                editPostId = current.editPostId,
                onCancel = { back() },
                onDone = { edited ->
                    back()
                    toast = if (edited) DamoimStrings.TOAST_POST_UPDATED else DamoimStrings.TOAST_POST_SUBMITTED
                },
                onToast = { toast = it },
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
