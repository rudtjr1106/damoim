package com.damoim.app.presentation.schedule.register

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.FormQuestion
import com.damoim.app.domain.model.Schedule
import com.damoim.app.domain.model.ScheduleDraft
import com.damoim.app.domain.usecase.GetScheduleDetailUseCase
import com.damoim.app.domain.usecase.SubmitScheduleUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class ScheduleRegisterUiState(
    val editId: Long? = null,
    val title: String = "",
    val hasStart: Boolean = false,
    val startDate: LocalDate? = null,
    val startHour: Int = 14,
    val startMinute: Int = 0,
    val hasEnd: Boolean = false,
    val endDate: LocalDate? = null,
    val endHour: Int = 16,
    val endMinute: Int = 0,
    val location: String = "",
    val memo: String = "",
    val isEvent: Boolean = false,
    val capacity: String = "",
    val hasDeadline: Boolean = false,
    val deadlineDate: LocalDate? = null,
    val deadlineHour: Int = 23,
    val deadlineMinute: Int = 59,
    val form: List<FormQuestion> = emptyList(),
    val submitting: Boolean = false,
) : UiState {
    val isEdit: Boolean get() = editId != null
    val canSave: Boolean get() = title.isNotBlank() && hasStart && !submitting

    fun toDraft() = ScheduleDraft(
        editId = editId, title = title.trim(),
        startDate = startDate, startHour = startHour, startMinute = startMinute,
        hasEnd = hasEnd, endDate = endDate, endHour = endHour, endMinute = endMinute,
        location = location.trim(), memo = memo.trim(),
        isEvent = isEvent, capacity = capacity,
        deadlineDate = deadlineDate, deadlineHour = deadlineHour, deadlineMinute = deadlineMinute,
        form = form,
    )
}

sealed interface ScheduleRegisterSideEffect : UiSideEffect {
    data class Saved(val edited: Boolean) : ScheduleRegisterSideEffect
}

/** 23 일정/이벤트 등록·수정. 신청 양식(46/51)은 이 화면의 전체화면 오버레이로 편집한다. */
class ScheduleRegisterViewModel(
    private val editId: Long?,
    getDetail: GetScheduleDetailUseCase,
    private val submit: SubmitScheduleUseCase,
) : BaseViewModel<ScheduleRegisterUiState, ScheduleRegisterSideEffect>(ScheduleRegisterUiState(editId = editId)) {

    init {
        if (editId != null) {
            viewModelScope.launch {
                val s = getDetail(editId).first() ?: return@launch
                prefill(s)
            }
        }
    }

    private fun prefill(s: Schedule) = setState {
        copy(
            title = s.title, hasStart = true, startDate = s.date, startHour = s.startHour, startMinute = s.startMinute,
            hasEnd = s.endDate != null, endDate = s.endDate, endHour = s.endHour, endMinute = s.endMinute,
            location = s.location, memo = s.memo,
            isEvent = s.event != null, capacity = s.event?.capacity?.toString() ?: "",
            hasDeadline = s.event != null, deadlineDate = s.event?.deadlineDate,
            form = s.event?.form.orEmpty(),
        )
    }

    fun setTitle(v: String) = setState { copy(title = v) }
    fun setLocation(v: String) = setState { copy(location = v) }
    fun setMemo(v: String) = setState { copy(memo = v) }
    fun setCapacity(v: String) = setState { copy(capacity = v.filter { it.isDigit() }.take(4)) }
    fun toggleEvent() = setState { copy(isEvent = !isEvent) }
    fun setForm(list: List<FormQuestion>) = setState { copy(form = list) }

    fun setStart(date: LocalDate, hour: Int, minute: Int) = setState { copy(hasStart = true, startDate = date, startHour = hour, startMinute = minute) }
    fun setEnd(date: LocalDate, hour: Int, minute: Int) = setState { copy(hasEnd = true, endDate = date, endHour = hour, endMinute = minute) }
    fun setDeadline(date: LocalDate, hour: Int, minute: Int) = setState { copy(hasDeadline = true, deadlineDate = date, deadlineHour = hour, deadlineMinute = minute) }

    fun save() {
        if (!currentState.canSave) return
        viewModelScope.launch {
            setState { copy(submitting = true) }
            val draft = currentState.toDraft()
            if (currentState.isEdit) submit.update(draft) else submit.create(draft)
            setState { copy(submitting = false) }
            sendEffect(ScheduleRegisterSideEffect.Saved(edited = currentState.isEdit))
        }
    }
}
