package com.damoim.app.presentation.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.AppNotification
import com.damoim.app.domain.model.NotificationType
import com.damoim.app.presentation.component.BellIcon
import com.damoim.app.presentation.component.CalendarIcon
import com.damoim.app.presentation.component.ChartIcon
import com.damoim.app.presentation.component.KakaoBubbleIcon
import com.damoim.app.presentation.component.MegaphoneIcon
import com.damoim.app.presentation.component.PersonPlusIcon
import com.damoim.app.presentation.component.TitleTopBar
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 화면 37/74 알림 — Route. */
@Composable
fun NotificationRoute(
    viewModel: NotificationViewModel = viewModel { NotificationViewModel(AppGraph.getNotificationsUseCase) },
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    NotificationScreen(state = state, onBack = onBack, onMarkAllRead = viewModel::onMarkAllRead)
}

@Composable
fun NotificationScreen(
    state: NotificationUiState = NotificationUiState(isLoading = false, notifications = previewNotifications()),
    onBack: () -> Unit = {},
    onMarkAllRead: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    val empty = state.notifications.isEmpty() && !state.isLoading
    Column(Modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        TitleTopBar(
            DamoimStrings.NOTIFICATION_TITLE, onBack,
            actionText = if (!empty) DamoimStrings.NOTIFICATION_MARK_ALL else null,
            actionColor = colors.textMuted,
            onAction = onMarkAllRead,
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))

        if (empty) {
            EmptyNotifications(Modifier.weight(1f))
        } else {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                state.notifications.forEach { NotificationRow(it) }
            }
        }
    }
}

@Composable
private fun NotificationRow(n: AppNotification) {
    val colors = DamoimTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().background(if (n.isUnread) colors.surfaceVariant else colors.surface).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(if (n.isUnread) colors.primaryContainer else colors.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) { NotifIcon(n.type, if (n.isUnread) colors.primary else colors.textMuted) }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(n.text, style = DamoimTheme.typography.body, color = if (n.isUnread) colors.textPrimary else colors.textTertiary)
                Spacer(Modifier.height(4.dp))
                Text(n.timeAgo, style = DamoimTheme.typography.label, color = colors.textDisabled)
            }
            if (n.isUnread) {
                Spacer(Modifier.size(8.dp))
                Box(Modifier.padding(top = 6.dp).size(8.dp).clip(CircleShape).background(colors.primary))
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
    }
}

@Composable
private fun NotifIcon(type: NotificationType, tint: Color) {
    val m = Modifier.size(18.dp)
    when (type) {
        NotificationType.JOIN_APPROVED -> PersonPlusIcon(tint, m)
        NotificationType.NOTICE -> MegaphoneIcon(tint, m)
        NotificationType.COMMENT -> KakaoBubbleIcon(tint, m)
        NotificationType.SCHEDULE -> CalendarIcon(tint, m)
        NotificationType.VOTE -> ChartIcon(tint, m)
    }
}

@Composable
private fun EmptyNotifications(modifier: Modifier) {
    val colors = DamoimTheme.colors
    Column(
        modifier = modifier.fillMaxWidth().padding(start = 40.dp, end = 40.dp, bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(80.dp).clip(CircleShape).background(colors.surfaceVariant), contentAlignment = Alignment.Center) {
            BellIcon(tint = colors.textDisabled, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text(DamoimStrings.NOTIFICATION_EMPTY_TITLE, style = DamoimTheme.typography.titleMedium, color = colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text(DamoimStrings.NOTIFICATION_EMPTY_SUBTITLE, style = DamoimTheme.typography.bodySmall, color = colors.textMuted, textAlign = TextAlign.Center)
    }
}

internal fun previewNotifications() = listOf(
    AppNotification(1, NotificationType.JOIN_APPROVED, "가입 신청이 승인되었어요. 코딩하는 사람들에 오신 것을 환영합니다! 🎉", "방금 전", true),
    AppNotification(2, NotificationType.NOTICE, "새 공지: 신입 회원 환영 OT 일정 안내", "1시간 전", true),
    AppNotification(3, NotificationType.COMMENT, "박준혁님이 회원님의 글에 댓글을 남겼어요: \"혹시 온라인 참여도...\"", "어제", false),
    AppNotification(4, NotificationType.SCHEDULE, "정기 월례회의가 내일 오전 10시에 시작돼요", "어제", false),
    AppNotification(5, NotificationType.VOTE, "MT 날짜 투표가 곧 마감돼요 (D-2)", "2일 전", false),
)

@Preview
@Composable
private fun NotificationScreenPreview() {
    DamoimTheme { NotificationScreen() }
}

@Preview
@Composable
private fun NotificationEmptyPreview() {
    DamoimTheme { NotificationScreen(state = NotificationUiState(isLoading = false, notifications = emptyList())) }
}
