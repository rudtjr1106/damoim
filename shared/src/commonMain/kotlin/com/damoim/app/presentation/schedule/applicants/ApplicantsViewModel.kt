package com.damoim.app.presentation.schedule.applicants

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.Schedule
import com.damoim.app.domain.usecase.GetScheduleDetailUseCase
import com.damoim.app.domain.usecase.ScheduleActionUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlin.time.Clock
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class ApplicantsUiState(
    val isLoading: Boolean = true,
    val schedule: Schedule? = null,
    val today: LocalDate = LocalDate(2025, 6, 4),
) : UiState

sealed interface ApplicantsSideEffect : UiSideEffect {
    data class Toast(val message: String) : ApplicantsSideEffect
}

/** 47 이벤트 신청자 관리(동아리장). */
class ApplicantsViewModel(
    private val scheduleId: Long,
    getDetail: GetScheduleDetailUseCase,
    private val action: ScheduleActionUseCase,
) : BaseViewModel<ApplicantsUiState, ApplicantsSideEffect>(ApplicantsUiState()) {

    init {
        setState { copy(today = Clock.System.todayIn(TimeZone.currentSystemDefault())) }
        viewModelScope.launch {
            getDetail(scheduleId).collect { setState { copy(isLoading = false, schedule = it) } }
        }
    }

    fun closeEarly() = viewModelScope.launch {
        action.closeEarly(scheduleId)
        sendEffect(ApplicantsSideEffect.Toast(DamoimStrings.TOAST_EVENT_CLOSED))
    }
}
