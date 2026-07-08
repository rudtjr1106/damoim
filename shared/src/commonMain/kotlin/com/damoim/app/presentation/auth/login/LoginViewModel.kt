package com.damoim.app.presentation.auth.login

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.usecase.LoginWithKakaoUseCase
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
) : UiState

sealed interface LoginSideEffect : UiSideEffect {
    data object NavigateToProfileSetup : LoginSideEffect  // 신규 → 31
    data object NavigateToStart : LoginSideEffect         // 기존 → 32
    data class ShowError(val message: String) : LoginSideEffect
}

/**
 * 화면 01 로그인. 카카오 버튼 → 로그인 수행 → 프로필 설정 필요 여부로 분기.
 * (기존 02 카카오 동의 화면을 제거하고 로그인 로직을 여기로 통합)
 */
class LoginViewModel(
    private val loginWithKakao: LoginWithKakaoUseCase,
) : BaseViewModel<LoginUiState, LoginSideEffect>(LoginUiState()) {

    fun onKakaoLogin() {
        if (currentState.isLoading) return
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            val result = loginWithKakao()
            setState { copy(isLoading = false) }
            handleResult(
                result = result,
                onSuccess = { user ->
                    if (user.needsProfileSetup) {
                        sendEffect(LoginSideEffect.NavigateToProfileSetup)
                    } else {
                        sendEffect(LoginSideEffect.NavigateToStart)
                    }
                },
                onFailure = { sendEffect(LoginSideEffect.ShowError(it.message)) },
            )
        }
    }
}
