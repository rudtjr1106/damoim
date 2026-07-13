package com.damoim.app.presentation.schedule.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.EventInfo
import com.damoim.app.domain.model.Schedule
import com.damoim.app.domain.model.ScheduleAccent
import com.damoim.app.domain.model.ScheduleType
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.BottomNavBar
import com.damoim.app.presentation.component.CalendarIcon
import com.damoim.app.presentation.component.ChevronRightIcon
import com.damoim.app.presentation.component.MainTab
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.schedule.ScheduleBarCard
import com.damoim.app.presentation.schedule.ScheduleCalendar
import com.damoim.app.presentation.schedule.ScheduleDateRow
import com.damoim.app.presentation.schedule.ScheduleEmphasisCard
import com.damoim.app.presentation.schedule.ScheduleFab
import com.damoim.app.presentation.schedule.ScheduleSegment
import com.damoim.app.presentation.schedule.schFullDate
import com.damoim.app.presentation.schedule.schMonthTitle
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme
import kotlinx.datetime.LocalDate

@Composable
fun ScheduleHomeRoute(
    isLeader: Boolean,
    viewModel: ScheduleHomeViewModel = viewModel(key = "scheduleHome") {
        ScheduleHomeViewModel(AppGraph.getSchedulesUseCase, AppGraph.scheduleActionUseCase)
    },
    onOpenSchedule: (Long) -> Unit = {},
    onRegister: () -> Unit = {},
    onOpenMyApps: () -> Unit = {},
    onTabSelect: (MainTab) -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { if (it is ScheduleHomeSideEffect.Toast) onToast(it.message) }
    }
    ScheduleHomeScreen(
        state = state,
        isLeader = isLeader,
        onSelectView = viewModel::onSelectView,
        onSelectDate = viewModel::onSelectDate,
        onPrevMonth = viewModel::onPrevMonth,
        onNextMonth = viewModel::onNextMonth,
        onAddCalendar = viewModel::onAddCalendar,
        onOpenSchedule = onOpenSchedule,
        onRegister = onRegister,
        onOpenMyApps = onOpenMyApps,
        onTabSelect = onTabSelect,
    )
}

@Composable
fun ScheduleHomeScreen(
    state: ScheduleHomeUiState = ScheduleHomeUiState(isLoading = false),
    isLeader: Boolean = true,
    onSelectView: (Boolean) -> Unit = {},
    onSelectDate: (LocalDate) -> Unit = {},
    onPrevMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
    onAddCalendar: (Long) -> Unit = {},
    onOpenSchedule: (Long) -> Unit = {},
    onRegister: () -> Unit = {},
    onOpenMyApps: () -> Unit = {},
    onTabSelect: (MainTab) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surface)) {
        // 헤더
        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(DamoimStrings.SCHEDULE_TITLE, style = DamoimTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
            Text(
                DamoimStrings.SCHEDULE_MY_APPS_ACTION,
                style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                color = colors.primary,
                modifier = Modifier.noRippleClick(onOpenMyApps).padding(end = 10.dp, top = 4.dp, bottom = 4.dp),
            )
            ScheduleSegment(listMode = state.listMode, onSelect = onSelectView)
        }

        Box(Modifier.weight(1f)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                when {
                    state.isEmpty -> EmptyState(isLeader = isLeader, onRegister = onRegister)
                    state.listMode -> ListView(state, onOpenSchedule, onAddCalendar)
                    else -> CalendarView(state, onSelectDate, onPrevMonth, onNextMonth, onOpenSchedule, onAddCalendar)
                }
                Spacer(Modifier.height(96.dp))
            }
            // 리더는 빈 목록에서도 첫 일정을 만들 수 있어야 한다(빈 상태에서 FAB가 사라지면 생성 진입점이 없어짐).
            if (isLeader) {
                ScheduleFab(onRegister, Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 24.dp))
            }
        }

        BottomNavBar(selected = MainTab.SCHEDULE, onSelect = onTabSelect)
    }
}

@Composable
private fun CalendarView(
    state: ScheduleHomeUiState,
    onSelectDate: (LocalDate) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onOpenSchedule: (Long) -> Unit,
    onAddCalendar: (Long) -> Unit,
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        // 월 네비
        Row(Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(schMonthTitle(state.year, state.month), style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(colors.surfaceVariant).noRippleClick(onPrevMonth), contentAlignment = Alignment.Center) { BackChevronIcon(colors.textTertiary, Modifier.size(15.dp)) }
                Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(colors.surfaceVariant).noRippleClick(onNextMonth), contentAlignment = Alignment.Center) { ChevronRightIcon(colors.textTertiary, Modifier.size(15.dp)) }
            }
        }
        ScheduleCalendar(state.year, state.month, state.selectedDate, state.today, state.eventDays, onSelectDate)
    }
    Box(Modifier.fillMaxWidth().height(8.dp).background(colors.surfaceInput).padding(top = 12.dp))
    Spacer(Modifier.height(8.dp))
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(schFullDate(state.selectedDate), style = DamoimTheme.typography.bodyStrong.copy(fontWeight = FontWeight.ExtraBold, fontSize = 14.sp), color = colors.textPrimary)
        val daySchedules = state.selectedDaySchedules
        if (daySchedules.isEmpty()) {
            Text(DamoimStrings.SCHEDULE_DAY_EMPTY, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textDisabled, modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        } else {
            daySchedules.forEach { s ->
                ScheduleBarCard(s, onClick = { if (s.type == ScheduleType.EVENT) onOpenSchedule(s.id) }, onAddCalendar = { onAddCalendar(s.id) })
            }
        }
    }
}

@Composable
private fun ListView(state: ScheduleHomeUiState, onOpenSchedule: (Long) -> Unit, onAddCalendar: (Long) -> Unit) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        state.listGroups().forEach { (header, items) ->
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(header, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp), color = colors.textMuted)
                items.forEach { s ->
                    if (s.id == state.nearestUpcomingId) {
                        ScheduleEmphasisCard(s, state.today, onClick = { onOpenSchedule(s.id) }, onAddCalendar = { onAddCalendar(s.id) })
                    } else {
                        ScheduleDateRow(s, state.today, onClick = { onOpenSchedule(s.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(isLeader: Boolean = false, onRegister: () -> Unit = {}) {
    val colors = DamoimTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(top = 120.dp, start = 40.dp, end = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(colors.surfaceVariant), contentAlignment = Alignment.Center) {
            CalendarIcon(colors.textDisabled, Modifier.size(30.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(DamoimStrings.SCHEDULE_EMPTY_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
        Spacer(Modifier.height(6.dp))
        Text(DamoimStrings.SCHEDULE_EMPTY_SUBTITLE, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        // 리더에게는 빈 상태에서도 첫 일정 생성 CTA를 노출한다.
        if (isLeader) {
            Spacer(Modifier.height(22.dp))
            Box(
                Modifier.clip(RoundedCornerShape(12.dp)).background(colors.primary)
                    .noRippleClick(onRegister).padding(horizontal = 22.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(DamoimStrings.SCHEDULE_REGISTER_TITLE, style = DamoimTheme.typography.button, color = colors.onPrimary)
            }
        }
    }
}

// ── Preview ──
internal fun previewSchedules(): List<Schedule> = listOf(
    Schedule(1, ScheduleType.SCHEDULE, "정기 월례회의", LocalDate(2025, 6, 7), "오전 10:00", location = "동아리방", accent = ScheduleAccent.PRIMARY),
    Schedule(
        2, ScheduleType.EVENT, "신입 환영 MT", LocalDate(2025, 6, 14), "오전 9:00", location = "가평", accent = ScheduleAccent.SKY,
        event = EventInfo(20, 12, LocalDate(2025, 6, 12), "6.12 (목) 자정", meta = "1박 2일 · 가평", dday = "D-10"),
    ),
)

@Preview
@Composable
private fun ScheduleHomeCalendarPreview() {
    DamoimTheme {
        ScheduleHomeScreen(state = ScheduleHomeUiState(isLoading = false, schedules = previewSchedules(), today = LocalDate(2025, 6, 4), selectedDate = LocalDate(2025, 6, 7)))
    }
}

@Preview
@Composable
private fun ScheduleHomeListPreview() {
    DamoimTheme {
        ScheduleHomeScreen(state = ScheduleHomeUiState(isLoading = false, listMode = true, schedules = previewSchedules(), today = LocalDate(2025, 6, 4)))
    }
}
