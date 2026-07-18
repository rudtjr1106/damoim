package com.damoim.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.data.remote.core.DataTopic
import com.damoim.app.data.remote.core.RemoteBus
import com.damoim.app.presentation.club.ClubSwitchOverlay
import com.damoim.app.presentation.component.PullRefreshColumn
import com.damoim.app.domain.model.AlertKind
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.BoardPreview
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.HomeAlert
import com.damoim.app.domain.model.HomeStat
import com.damoim.app.domain.model.HomeSummary
import com.damoim.app.domain.model.UpcomingSchedule
import com.damoim.app.presentation.component.ArrowRightIcon
import com.damoim.app.presentation.component.BellIcon
import com.damoim.app.presentation.component.BottomNavBar
import com.damoim.app.presentation.component.CalendarIcon
import com.damoim.app.presentation.component.ChevronDownIcon
import com.damoim.app.presentation.component.CrownIcon
import com.damoim.app.presentation.component.MainTab
import com.damoim.app.presentation.component.MegaphoneIcon
import com.damoim.app.presentation.component.PersonPlusIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 05/06 홈 — Route. role별 데이터 로드 + 네비게이션 콜백.
 * 미구현 목적지(일정·회원·공지·내프로필·전체보기)는 [onComingSoon]으로 안내.
 */
@Composable
fun HomeRoute(
    role: ClubRole,
    viewModel: HomeViewModel = viewModel(key = "home_${role.name}") { HomeViewModel(AppGraph.getHomeSummaryUseCase) },
    onNavigateJoinManage: () -> Unit = {},
    onNavigateNotifications: () -> Unit = {},
    onOpenScheduleTab: () -> Unit = {},        // 일정 알림 터치 → 일정 탭
    onOpenSchedule: (Long) -> Unit = {},
    onOpenPost: (Long) -> Unit = {},
    onSwitched: () -> Unit = {},
    onJoinClub: () -> Unit = {},
    onAddClub: () -> Unit = {},
    onTabSelect: (MainTab) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    HomeScreen(
        state = state,
        onBellClick = onNavigateNotifications,
        onAlertClick = {
            when (state.summary?.alert?.kind) {
                AlertKind.JOIN_REQUEST -> onNavigateJoinManage()
                AlertKind.SCHEDULE -> onOpenScheduleTab()
                null -> {}
            }
        },
        onOpenSchedule = onOpenSchedule,
        onOpenPost = onOpenPost,
        onSwitched = onSwitched,
        onJoinClub = onJoinClub,
        onAddClub = onAddClub,
        onTabSelect = onTabSelect,
    )
}

/**
 * 화면 05/06 홈 — Screen(무상태). 헤더·알림·퀵액션·일정·게시판·하단탭.
 */
@Composable
fun HomeScreen(
    state: HomeUiState = HomeUiState(isLoading = false, summary = previewSummary(ClubRole.LEADER)),
    onBellClick: () -> Unit = {},
    onAlertClick: () -> Unit = {},
    onOpenSchedule: (Long) -> Unit = {},
    onOpenPost: (Long) -> Unit = {},
    onSwitched: () -> Unit = {},
    onJoinClub: () -> Unit = {},
    onAddClub: () -> Unit = {},
    onTabSelect: (MainTab) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    val summary = state.summary
    var showSwitch by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(colors.surface)) {
            PullRefreshColumn(
                onRefresh = { RemoteBus.invalidate(DataTopic.CLUB, DataTopic.MEMBER, DataTopic.NOTIFICATION, DataTopic.SCHEDULE, DataTopic.BOARD) },
                modifier = Modifier.weight(1f),
            ) {
                if (summary != null) {
                    HomeHeader(summary, onBellClick, onClubNameClick = { showSwitch = true })
                    Column(modifier = Modifier.offset(y = (-36).dp)) {
                        // 알림 카드는 있을 때만(예: 신청 0건이면 숨김)
                        summary.alert?.let { AlertCard(it, onAlertClick) }
                        if (summary.schedules.isNotEmpty()) {
                            ScheduleSection(summary.schedules, onOpenSchedule)
                        }
                        if (summary.boardPreviews.isNotEmpty()) {
                            BoardSection(summary.boardPreviews, onOpenPost)
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            BottomNavBar(selected = MainTab.HOME, onSelect = onTabSelect)
        }
        // 46 동아리 전환 — 동아리명 탭 시 공유 시트(설정 42와 동일).
        ClubSwitchOverlay(
            visible = showSwitch,
            onDismiss = { showSwitch = false },
            onSwitched = onSwitched,
            onJoin = onJoinClub,
            onCreate = onAddClub,
        )
    }
}

@Composable
private fun HomeHeader(summary: HomeSummary, onBellClick: () -> Unit, onClubNameClick: () -> Unit) {
    val colors = DamoimTheme.colors
    val onP = colors.onPrimary
    Column(
        // 배경(primary)은 상태바 밑까지 꽉 채우되(엣지투엣지), 내용은 상태바로부터 16dp 아래에 둔다
        modifier = Modifier.fillMaxWidth().background(colors.primary)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 64.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                if (summary.role == ClubRole.LEADER) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(DamoimStrings.HOME_MY_CLUB, style = DamoimTheme.typography.bodySmall, color = onP.copy(alpha = 0.75f))
                        Spacer(Modifier.width(6.dp))
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(onP.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CrownIcon(tint = onP, modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(DamoimStrings.HOME_ROLE_LEADER, style = DamoimTheme.typography.labelSmall, color = onP)
                        }
                    }
                } else {
                    Text(DamoimStrings.homeGreeting(summary.memberName), style = DamoimTheme.typography.bodySmall, color = onP.copy(alpha = 0.75f))
                }
                Spacer(Modifier.height(4.dp))
                // 46 동아리명 탭 → 전환 시트. 아래 화살표로 전환 가능함을 표시.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClubNameClick),
                ) {
                    Text(summary.clubName, style = DamoimTheme.typography.titleLarge.copy(fontSize = 22.sp), color = onP, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    Spacer(Modifier.width(6.dp))
                    ChevronDownIcon(tint = onP, modifier = Modifier.size(18.dp))
                }
            }
            // 알림 벨
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(onP.copy(alpha = 0.18f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onBellClick),
                contentAlignment = Alignment.Center,
            ) {
                BellIcon(tint = onP, modifier = Modifier.size(20.dp))
                if (summary.hasUnreadNotification) {
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 9.dp, end = 9.dp)
                            .size(7.dp).clip(CircleShape).background(colors.error),
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            summary.stats.forEach { StatItem(it, onP) }
        }
    }
}

@Composable
private fun StatItem(stat: HomeStat, onPrimary: Color) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(stat.value, style = DamoimTheme.typography.titleLarge, color = onPrimary)
        Spacer(Modifier.width(5.dp))
        Text(stat.label, style = DamoimTheme.typography.caption, color = onPrimary.copy(alpha = 0.75f), modifier = Modifier.padding(bottom = 2.dp))
    }
}

@Composable
private fun AlertCard(alert: HomeAlert, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    val clickable = alert.kind == AlertKind.JOIN_REQUEST
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            .shadow(10.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp)).background(colors.surface)
            .then(if (clickable) Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            if (alert.kind == AlertKind.JOIN_REQUEST) PersonPlusIcon(tint = colors.primary, modifier = Modifier.size(20.dp))
            else CalendarIcon(tint = colors.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(alert.title, style = DamoimTheme.typography.bodyStrong, color = colors.textPrimary)
            Text(alert.subtitle, style = DamoimTheme.typography.caption, color = colors.textMuted)
        }
        Spacer(Modifier.width(10.dp))
        if (alert.kind == AlertKind.JOIN_REQUEST) {
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape).background(colors.primary),
                contentAlignment = Alignment.Center,
            ) { ArrowRightIcon(tint = colors.onPrimary, modifier = Modifier.size(14.dp)) }
        } else {
            Text(
                alert.badge.orEmpty(),
                style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                color = colors.primaryDark,
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.primaryContainer).padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
private fun ScheduleSection(schedules: List<UpcomingSchedule>, onOpen: (Long) -> Unit) {
    SectionHeader(DamoimStrings.HOME_SECTION_SCHEDULE, Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp))
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        schedules.forEach { s -> ScheduleCard(s) { onOpen(s.id) } }
    }
}

@Composable
private fun ScheduleCard(s: UpcomingSchedule, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    val onDark = s.primary
    val bg = if (onDark) colors.onDarkNavy else colors.primaryContainer
    val titleColor = if (onDark) colors.onPrimary else colors.textPrimary
    val subColor = if (onDark) colors.onPrimary.copy(alpha = 0.6f) else colors.textMuted
    Column(
        modifier = Modifier.width(210.dp).clip(RoundedCornerShape(18.dp)).background(bg).noRippleClick(onClick).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                s.dday, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                color = if (onDark) colors.accentSky else colors.primaryDark,
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(if (onDark) colors.onPrimary.copy(alpha = 0.1f) else colors.surface).padding(horizontal = 9.dp, vertical = 4.dp),
            )
            Text(s.date, style = DamoimTheme.typography.label, color = if (onDark) colors.onPrimary.copy(alpha = 0.55f) else colors.textDisabled)
        }
        Column {
            Text(s.title, style = DamoimTheme.typography.titleMedium, color = titleColor)
            Spacer(Modifier.height(3.dp))
            Text(s.subtitle, style = DamoimTheme.typography.caption, color = subColor)
        }
    }
}

@Composable
private fun BoardSection(previews: List<BoardPreview>, onOpenPost: (Long) -> Unit) {
    SectionHeader(DamoimStrings.HOME_SECTION_BOARD, Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp))
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 필독 먼저(홈 우선순위) — 서버 정렬 + 클라에서도 안전하게 보장
        previews.sortedByDescending { it.isPinned }.forEach { p -> BoardRow(p) { onOpenPost(p.id) } }
    }
}

@Composable
private fun BoardRow(preview: BoardPreview, onClick: () -> Unit = {}) {
    val colors = DamoimTheme.colors
    val isNotice = preview.category == BoardCategory.NOTICE
    val pinned = preview.isPinned
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            // 필독은 특별하게: 강조 배경 + primary 테두리(일반 글은 옅은 divider 테두리)
            .then(
                if (pinned) Modifier.background(colors.primaryContainer).border(1.5.dp, colors.primary, RoundedCornerShape(14.dp))
                else Modifier.border(1.dp, colors.divider, RoundedCornerShape(14.dp)),
            )
            .noRippleClick(onClick).padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (pinned) {
            MegaphoneIcon(colors.primaryDark, Modifier.size(15.dp))
            Spacer(Modifier.width(7.dp))
            Text(
                DamoimStrings.BOARD_PINNED,
                style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold),
                color = colors.onPrimary,
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.primary).padding(horizontal = 9.dp, vertical = 4.dp),
            )
        } else {
            Text(
                categoryLabel(preview.category),
                style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                color = if (isNotice) colors.onPrimary else colors.primaryDark,
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(if (isNotice) colors.primary else colors.primaryContainer).padding(horizontal = 9.dp, vertical = 4.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(preview.title, style = DamoimTheme.typography.bodySmall.copy(fontWeight = if (pinned) FontWeight.Bold else FontWeight.Normal), color = colors.textPrimary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.width(10.dp))
        Text(preview.commentCount.toString(), style = DamoimTheme.typography.label, color = colors.textDisabled)
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(title, style = DamoimTheme.typography.titleMedium, color = DamoimTheme.colors.textPrimary, modifier = modifier.fillMaxWidth())
}

private fun categoryLabel(c: BoardCategory) = when (c) {
    BoardCategory.NOTICE -> DamoimStrings.BOARD_NOTICE
    BoardCategory.FREE -> DamoimStrings.BOARD_FREE
    BoardCategory.RECRUIT -> DamoimStrings.BOARD_RECRUIT
}

/** 프리뷰/기본값용 샘플 요약 (presentation이 data.mock에 의존하지 않도록 여기 둔다). */
internal fun previewSummary(role: ClubRole) = HomeSummary(
    role = role,
    clubName = "코딩하는 사람들",
    memberName = "서연",
    stats = if (role == ClubRole.LEADER)
        listOf(HomeStat("38", "회원"), HomeStat("3", "신청 대기"), HomeStat("2", "이번 주"))
    else listOf(HomeStat("24기", "내 기수"), HomeStat("2", "이번 주 일정"), HomeStat("7", "새 글")),
    alert = if (role == ClubRole.LEADER)
        HomeAlert("가입 신청 3건이 기다려요", "탭해서 승인/거절 처리", AlertKind.JOIN_REQUEST)
    else HomeAlert("정기 월례회의가 3일 남았어요", "6.07 토 오전 10:00 · 동아리방", AlertKind.SCHEDULE, "D-3"),
    schedules = listOf(
        UpcomingSchedule(1, "D-3", "6.07 토", "정기 월례회의", "오전 10:00 · 동아리방", true),
        UpcomingSchedule(2, "D-10", "6.14 토", "신입 환영 MT", "1박 2일 · 가평", false),
    ),
    boardPreviews = buildList {
        add(BoardPreview(101, BoardCategory.NOTICE, "신입 회원 환영 OT 일정 안내", 5, isPinned = true))
        add(BoardPreview(102, BoardCategory.FREE, "동아리 MT 후기 공유해요", 12))
        if (role == ClubRole.LEADER) add(BoardPreview(103, BoardCategory.RECRUIT, "2025 하반기 신입 부원 모집", 3))
    },
    hasUnreadNotification = role == ClubRole.LEADER,
)

@Preview
@Composable
private fun HomeScreenLeaderPreview() {
    DamoimTheme { HomeScreen(state = HomeUiState(isLoading = false, summary = previewSummary(ClubRole.LEADER))) }
}

@Preview
@Composable
private fun HomeScreenMemberPreview() {
    DamoimTheme { HomeScreen(state = HomeUiState(isLoading = false, summary = previewSummary(ClubRole.MEMBER))) }
}
