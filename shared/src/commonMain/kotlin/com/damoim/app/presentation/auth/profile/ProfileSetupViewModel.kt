package com.damoim.app.presentation.auth.profile

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.launch

data class ProfileSetupUiState(
    // 카카오에서 받은 이름으로 프리필 (Mock 기준 "서연")
    val nickname: String = "서연",
    val contact: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) : UiState {
    val canSubmit: Boolean get() = nickname.isNotBlank() && contact.isNotBlank() && !isSaving
}

sealed interface ProfileSetupSideEffect : UiSideEffect {
    data object NavigateToStart : ProfileSetupSideEffect  // 31 → 32
}

/**
 * 화면 31 프로필 설정(가입 직후). 이름·연락처 입력 → 저장 → 시작하기 화면으로.
 */
class ProfileSetupViewModel(
    private val updateProfile: UpdateProfileUseCase,
) : BaseViewModel<ProfileSetupUiState, ProfileSetupSideEffect>(ProfileSetupUiState()) {

    fun onNicknameChange(value: String) {
        // 최대 글자수를 넘겨 입력되지 않도록 캡 (카운터 n/10과 일치)
        setState { copy(nickname = value.take(UpdateProfileUseCase.MAX_NICKNAME_LENGTH), errorMessage = null) }
    }

    fun onContactChange(value: String) {
        // 숫자·하이픈만 허용, 010-0000-0000 길이(13자)까지
        val filtered = value.filter { it.isDigit() || it == '-' }.take(13)
        setState { copy(contact = filtered, errorMessage = null) }
    }

    fun onSubmit() {
        if (!currentState.canSubmit) return
        setState { copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = updateProfile(currentState.nickname, currentState.contact)
            handleResult(
                result = result,
                onSuccess = {
                    setState { copy(isSaving = false) }
                    sendEffect(ProfileSetupSideEffect.NavigateToStart)
                },
                onFailure = { error ->
                    setState { copy(isSaving = false, errorMessage = error.message) }
                },
            )
        }
    }
}
