package com.damoim.app.presentation.schedule.detail

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.QuestionAnswer
import com.damoim.app.domain.model.Schedule
import com.damoim.app.domain.usecase.EventApplicationUseCase
import com.damoim.app.domain.usecase.GetScheduleDetailUseCase
import com.damoim.app.domain.usecase.ScheduleActionUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlin.time.Clock
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class EventDetailUiState(
    val isLoading: Boolean = true,
    val schedule: Schedule? = null,
    val submitting: Boolean = false,
    val today: kotlinx.datetime.LocalDate = kotlinx.datetime.LocalDate(2025, 6, 4),
) : UiState

sealed interface EventDetailSideEffect : UiSideEffect {
    data class Toast(val message: String) : EventDetailSideEffect
    data object Deleted : EventDetailSideEffect
    data object Applied : EventDetailSideEffect
}

/** 24 이벤트 상세 + 운영 액션(62 메뉴)·참여 신청(25)·취소/마감(63). */
class EventDetailViewModel(
    private val scheduleId: Long,
    getDetail: GetScheduleDetailUseCase,
    private val application: EventApplicationUseCase,
    private val action: ScheduleActionUseCase,
) : BaseViewModel<EventDetailUiState, EventDetailSideEffect>(EventDetailUiState()) {

    init {
        setState { copy(today = Clock.System.todayIn(TimeZone.currentSystemDefault())) }
        viewModelScope.launch {
            getDetail(scheduleId).collect { setState { copy(isLoading = false, schedule = it) } }
        }
    }

    fun apply(answers: List<QuestionAnswer>) = submit(edit = false, answers)
    fun updateApplication(answers: List<QuestionAnswer>) = submit(edit = true, answers)

    private fun submit(edit: Boolean, answers: List<QuestionAnswer>) {
        viewModelScope.launch {
            setState { copy(submitting = true) }
            val result = if (edit) application.updateAnswers(scheduleId, answers) else application.apply(scheduleId, answers)
            setState { copy(submitting = false) }
            handleResult(
                result,
                onSuccess = {
                    sendEffect(EventDetailSideEffect.Applied)
                    sendEffect(EventDetailSideEffect.Toast(if (edit) DamoimStrings.TOAST_APPLICATION_UPDATED else DamoimStrings.TOAST_EVENT_APPLIED))
                },
                onFailure = { sendEffect(EventDetailSideEffect.Toast(it.message.ifBlank { DamoimStrings.TOAST_EVENT_APPLY_FAIL })) },
            )
        }
    }

    fun announce() = viewModelScope.launch {
        action.announce(scheduleId)
        sendEffect(EventDetailSideEffect.Toast(DamoimStrings.TOAST_EVENT_ANNOUNCED))
    }

    fun closeEarly() = viewModelScope.launch {
        action.closeEarly(scheduleId)
        sendEffect(EventDetailSideEffect.Toast(DamoimStrings.TOAST_EVENT_CLOSED))
    }

    fun cancelEvent() = viewModelScope.launch {
        action.delete(scheduleId)
        sendEffect(EventDetailSideEffect.Deleted)
    }
}
