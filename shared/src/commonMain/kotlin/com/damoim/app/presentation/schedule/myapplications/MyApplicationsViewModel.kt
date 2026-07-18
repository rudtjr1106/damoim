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
import com.damoim.app.domain.usecase.GetScheduleDetailUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class MyApplicationsUiState(
    val isLoading: Boolean = true,
    val applications: List<MyApplication> = emptyList(),
    // 응답 수정 대상 + 그 이벤트의 전체 상세(폼 문항 포함). 상세는 편집 진입 시 조회한다.
    val editingApp: MyApplication? = null,
    val editingSchedule: Schedule? = null,
) : UiState {
    val isEmpty: Boolean get() = !isLoading && applications.isEmpty()
}

sealed interface MyApplicationsSideEffect : UiSideEffect {
    data class Toast(val message: String) : MyApplicationsSideEffect
}

/** 48 내 신청 내역(+75 빈 상태). */
class MyApplicationsViewModel(
    getMyApps: GetMyApplicationsUseCase,
    private val getScheduleDetail: GetScheduleDetailUseCase,
    private val application: EventApplicationUseCase,
) : BaseViewModel<MyApplicationsUiState, MyApplicationsSideEffect>(MyApplicationsUiState()) {

    init {
        viewModelScope.launch {
            getMyApps().collect { apps -> setState { copy(isLoading = false, applications = apps) } }
        }
    }

    /** 응답 수정 진입 — 폼 문항이 담긴 전체 상세를 조회한다(신청 목록엔 문항이 없어 수정 폼을 못 그리던 문제). */
    fun startEdit(app: MyApplication) {
        setState { copy(editingApp = app, editingSchedule = null) }
        viewModelScope.launch {
            val schedule = getScheduleDetail(app.eventId).filterNotNull().first()
            // 그 사이 편집 대상이 바뀌지 않았을 때만 반영
            if (currentState.editingApp?.eventId == app.eventId) setState { copy(editingSchedule = schedule) }
        }
    }

    fun clearEdit() = setState { copy(editingApp = null, editingSchedule = null) }

    fun cancel(eventId: Long) = viewModelScope.launch {
        handleResult(application.cancel(eventId), onSuccess = {
            sendEffect(MyApplicationsSideEffect.Toast(DamoimStrings.TOAST_APPLICATION_CANCELED))
        })
    }

    fun updateAnswers(eventId: Long, answers: List<QuestionAnswer>) = viewModelScope.launch {
        // 성공/실패를 확인해 반영(기존엔 무조건 성공 토스트만 띄워 실패해도 수정된 것처럼 보였다).
        handleResult(application.updateAnswers(eventId, answers), onSuccess = {
            setState { copy(editingApp = null, editingSchedule = null) }
            sendEffect(MyApplicationsSideEffect.Toast(DamoimStrings.TOAST_APPLICATION_UPDATED))
        })
    }
}
