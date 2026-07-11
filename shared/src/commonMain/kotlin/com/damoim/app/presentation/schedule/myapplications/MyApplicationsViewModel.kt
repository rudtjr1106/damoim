package com.damoim.app.presentation.schedule.myapplications

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.MyApplication
import com.damoim.app.domain.model.QuestionAnswer
import com.damoim.app.domain.model.Schedule
import com.damoim.app.domain.usecase.EventApplicationUseCase
import com.damoim.app.domain.usecase.GetMyApplicationsUseCase
import com.damoim.app.domain.usecase.GetSchedulesUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MyApplicationsUiState(
    val isLoading: Boolean = true,
    val applications: List<MyApplication> = emptyList(),
    val schedulesById: Map<Long, Schedule> = emptyMap(),
) : UiState {
    val isEmpty: Boolean get() = !isLoading && applications.isEmpty()
}

sealed interface MyApplicationsSideEffect : UiSideEffect {
    data class Toast(val message: String) : MyApplicationsSideEffect
}

/** 48 내 신청 내역(+75 빈 상태). */
class MyApplicationsViewModel(
    getMyApps: GetMyApplicationsUseCase,
    getSchedules: GetSchedulesUseCase,
    private val application: EventApplicationUseCase,
) : BaseViewModel<MyApplicationsUiState, MyApplicationsSideEffect>(MyApplicationsUiState()) {

    init {
        viewModelScope.launch {
            combine(getMyApps(), getSchedules()) { apps, schedules -> apps to schedules.associateBy { it.id } }
                .collect { (apps, byId) -> setState { copy(isLoading = false, applications = apps, schedulesById = byId) } }
        }
    }

    fun cancel(eventId: Long) = viewModelScope.launch {
        application.cancel(eventId)
        sendEffect(MyApplicationsSideEffect.Toast(DamoimStrings.TOAST_APPLICATION_CANCELED))
    }

    fun updateAnswers(eventId: Long, answers: List<QuestionAnswer>) = viewModelScope.launch {
        application.updateAnswers(eventId, answers)
        sendEffect(MyApplicationsSideEffect.Toast(DamoimStrings.TOAST_APPLICATION_UPDATED))
    }
}
