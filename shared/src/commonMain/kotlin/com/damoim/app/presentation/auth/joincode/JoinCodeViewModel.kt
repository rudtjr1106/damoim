package com.damoim.app.presentation.auth.joincode

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.model.JoinStatus
import com.damoim.app.domain.usecase.SubmitJoinCodeUseCase
import kotlinx.coroutines.launch

data class JoinCodeUiState(
    val code: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) : UiState {
    val canSubmit: Boolean get() = code.length == SubmitJoinCodeUseCase.CODE_LENGTH && !isSubmitting
}

sealed interface JoinCodeSideEffect : UiSideEffect {
    data class NavigateToComplete(val club: Club) : JoinCodeSideEffect            // PENDING → 04 대기
    data class NavigateToRejected(val club: Club, val reason: String) : JoinCodeSideEffect  // REJECTED → 38
    data object NavigateToHome : JoinCodeSideEffect                               // APPROVED → 홈(MEMBER)
}

/**
 * 화면 03 가입 코드 입력. 6자리 입력 → 제출 → 상태(PENDING/REJECTED)로 분기.
 */
class JoinCodeViewModel(
    private val submitJoinCode: SubmitJoinCodeUseCase,
) : BaseViewModel<JoinCodeUiState, JoinCodeSideEffect>(JoinCodeUiState()) {

    fun onCodeChange(value: String) {
        val normalized = value.filter { it.isLetterOrDigit() }
            .take(SubmitJoinCodeUseCase.CODE_LENGTH)
            .uppercase()
        setState { copy(code = normalized, errorMessage = null) }
    }

    fun onSubmit() {
        if (!currentState.canSubmit) return
        setState { copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val result = submitJoinCode(currentState.code)
            setState { copy(isSubmitting = false) }
            handleResult(
                result = result,
                onSuccess = { joinResult ->
                    when (joinResult.status) {
                        JoinStatus.REJECTED -> sendEffect(
                            JoinCodeSideEffect.NavigateToRejected(
                                club = joinResult.club,
                                reason = joinResult.rejectionReason ?: "가입이 거절되었어요",
                            ),
                        )
                        JoinStatus.APPROVED -> sendEffect(JoinCodeSideEffect.NavigateToHome)
                        JoinStatus.PENDING -> sendEffect(JoinCodeSideEffect.NavigateToComplete(joinResult.club))
                    }
                },
                onFailure = { error ->
                    setState { copy(errorMessage = error.message) }
                },
            )
        }
    }
}
