package com.damoim.app.presentation.auth.kakao

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.usecase.LoginWithKakaoUseCase
import kotlinx.coroutines.launch

data class KakaoConsentUiState(
    val isLoading: Boolean = false,
) : UiState

sealed interface KakaoConsentSideEffect : UiSideEffect {
    data object NavigateToProfileSetup : KakaoConsentSideEffect  // 신규 → 31
    data object NavigateToStart : KakaoConsentSideEffect         // 기존 → 32
    data class ShowError(val message: String) : KakaoConsentSideEffect
}

/**
 * 화면 02 카카오 로그인 동의. "동의하고 계속하기" → 로그인 → 프로필 설정 필요 여부로 분기.
 */
class KakaoConsentViewModel(
    private val loginWithKakao: LoginWithKakaoUseCase,
) : BaseViewModel<KakaoConsentUiState, KakaoConsentSideEffect>(KakaoConsentUiState()) {

    fun onAgree() {
        if (currentState.isLoading) return
        setState { copy(isLoading = true) }
        viewModelScope.launch {
            val result = loginWithKakao()
            setState { copy(isLoading = false) }
            handleResult(
                result = result,
                onSuccess = { user ->
                    if (user.needsProfileSetup) {
                        sendEffect(KakaoConsentSideEffect.NavigateToProfileSetup)
                    } else {
                        sendEffect(KakaoConsentSideEffect.NavigateToStart)
                    }
                },
                onFailure = { sendEffect(KakaoConsentSideEffect.ShowError(it.message)) },
            )
        }
    }
}
