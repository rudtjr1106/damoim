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
    data object LoggedIn : LoginSideEffect                // 로그인 성공 → 상위(RootNavHost)가 31/홈/32 판정
    data class ShowError(val message: String) : LoginSideEffect
}

/**
 * 화면 01 로그인. 카카오 버튼 → 로그인 수행 → 성공 시 상위 플로우 재판정.
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
            // 성공/실패 모두 로딩을 반드시 해제한다 — 이 VM은 앱 전역 스토어에 보존되므로(수동 백스택)
            // 로딩을 켜둔 채 떠나면 로그아웃 후 01에 돌아왔을 때 버튼이 스피너로 굳어 재로그인이 막힌다.
            // (판정 동안의 대기 화면은 상위 RootNavHost의 스플래시가 덮는다)
            setState { copy(isLoading = false) }
            handleResult(
                result = result,
                onSuccess = { sendEffect(LoginSideEffect.LoggedIn) },
                onFailure = { sendEffect(LoginSideEffect.ShowError(it.message)) },
            )
        }
    }
}
