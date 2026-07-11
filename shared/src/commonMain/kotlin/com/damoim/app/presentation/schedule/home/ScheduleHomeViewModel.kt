package com.damoim.app.presentation.schedule.home

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.Schedule
import com.damoim.app.domain.usecase.GetSchedulesUseCase
import com.damoim.app.domain.usecase.ScheduleActionUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class ScheduleHomeUiState(
    val isLoading: Boolean = true,
    val listMode: Boolean = false,
    val schedules: List<Schedule> = emptyList(),
    val today: LocalDate = LocalDate(2025, 6, 4),
    val year: Int = 2025,
    val month: Int = 6,
    val selectedDate: LocalDate = LocalDate(2025, 6, 7),
) : UiState {
    val isEmpty: Boolean get() = !isLoading && schedules.isEmpty()
    val eventDays: Set<LocalDate> get() = schedules.map { it.date }.toSet()
    val selectedDaySchedules: List<Schedule>
        get() = schedules.filter { it.date == selectedDate }
    val nearestUpcomingId: Long?
        get() = schedules.filter { it.date >= today }.minByOrNull { it.date.toEpochDays() }?.id

    /** 22 목록 뷰: 시간 버킷별 그룹(빈 그룹 제외). */
    fun listGroups(): List<Pair<String, List<Schedule>>> {
        val t = today.toEpochDays()
        fun bucket(s: Schedule): Int {
            val d = s.date.toEpochDays() - t
            return when {
                d < 0 -> 3               // 지난
                d <= 6 -> 0              // 이번 주
                d <= 13 -> 1             // 다음 주
                else -> 2                // 이후
            }
        }
        val labels = listOf(
            DamoimStrings.SCHEDULE_GROUP_THIS_WEEK,
            DamoimStrings.SCHEDULE_GROUP_NEXT_WEEK,
            DamoimStrings.SCHEDULE_GROUP_LATER,
            DamoimStrings.SCHEDULE_GROUP_PAST,
        )
        return (0..3).mapNotNull { b ->
            val items = schedules.filter { bucket(it) == b }.sortedBy { it.date.toEpochDays() }
            if (items.isEmpty()) null else labels[b] to items
        }
    }
}

sealed interface ScheduleHomeSideEffect : UiSideEffect {
    data class Toast(val message: String) : ScheduleHomeSideEffect
}

/** 21 캘린더 · 22 목록 홈. 세그먼트로 두 뷰를 전환한다. */
class ScheduleHomeViewModel(
    getSchedules: GetSchedulesUseCase,
    private val scheduleAction: ScheduleActionUseCase,
) : BaseViewModel<ScheduleHomeUiState, ScheduleHomeSideEffect>(ScheduleHomeUiState()) {

    init {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        setState { copy(today = today, year = today.year, month = today.monthNumber, selectedDate = today) }
        viewModelScope.launch {
            getSchedules().collect { list -> setState { copy(isLoading = false, schedules = list) } }
        }
    }

    fun onSelectView(listMode: Boolean) = setState { copy(listMode = listMode) }
    fun onSelectDate(date: LocalDate) = setState { copy(selectedDate = date) }

    fun onPrevMonth() = setState {
        if (month == 1) copy(year = year - 1, month = 12) else copy(month = month - 1)
    }

    fun onNextMonth() = setState {
        if (month == 12) copy(year = year + 1, month = 1) else copy(month = month + 1)
    }

    fun onAddCalendar(scheduleId: Long) {
        viewModelScope.launch {
            val added = (scheduleAction.toggleMyCalendar(scheduleId) as? com.damoim.app.core.result.DataResult.Success)?.data ?: return@launch
            sendEffect(ScheduleHomeSideEffect.Toast(if (added) DamoimStrings.TOAST_CALENDAR_ADDED else DamoimStrings.TOAST_CALENDAR_REMOVED))
        }
    }
}
