package com.damoim.app.presentation.joinmanage

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.JoinApplicant
import com.damoim.app.domain.usecase.DecideApplicantUseCase
import com.damoim.app.domain.usecase.GetJoinApplicantsUseCase
import kotlinx.coroutines.launch

data class JoinManageUiState(
    val isLoading: Boolean = true,
    val pending: List<JoinApplicant> = emptyList(),
    val doneCount: Int = 12,
) : UiState

sealed interface JoinManageSideEffect : UiSideEffect {
    data class Toast(val message: String) : JoinManageSideEffect
}

/** 화면 09 가입 신청 관리. */
class JoinManageViewModel(
    private val getApplicants: GetJoinApplicantsUseCase,
    private val decideApplicant: DecideApplicantUseCase,
) : BaseViewModel<JoinManageUiState, JoinManageSideEffect>(JoinManageUiState()) {

    init { load() }

    private fun load() = viewModelScope.launch {
        val result = getApplicants()
        setState { copy(isLoading = false) }
        handleResult(result, onSuccess = { list -> setState { copy(pending = list) } })
    }

    fun onDecide(applicant: JoinApplicant, approve: Boolean) = viewModelScope.launch {
        handleResult(decideApplicant(applicant.id, approve), onSuccess = {
            setState { copy(pending = pending.filterNot { it.id == applicant.id }, doneCount = doneCount + 1) }
            sendEffect(JoinManageSideEffect.Toast("${applicant.name}님을 ${if (approve) "승인" else "거절"}했어요"))
        })
    }
}
