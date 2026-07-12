package com.damoim.app.presentation.auth.profile

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.usecase.ObserveMyContextUseCase
import com.damoim.app.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ProfileSetupUiState(
    // 카카오가 닉네임을 제공하면 프리필, 없으면 빈칸(사용자가 직접 입력)
    val nickname: String = "",
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
    observeMyContext: ObserveMyContextUseCase,
) : BaseViewModel<ProfileSetupUiState, ProfileSetupSideEffect>(ProfileSetupUiState()) {

    init {
        // 카카오 로그인 결과(닉네임)로 프리필 — 없으면 빈칸(직접 입력)
        viewModelScope.launch {
            val name = observeMyContext().first().name
            setState { copy(nickname = name.take(UpdateProfileUseCase.MAX_NICKNAME_LENGTH)) }
        }
    }

    fun onNicknameChange(value: String) {
        // 최대 글자수를 넘겨 입력되지 않도록 캡 (카운터 n/10과 일치)
        setState { copy(nickname = value.take(UpdateProfileUseCase.MAX_NICKNAME_LENGTH), errorMessage = null) }
    }

    fun onContactChange(value: String) {
        // 상태는 숫자만(최대 11자리) 보관 — 하이픈은 VisualTransformation이 표시 시에만 붙인다
        // (상태를 재포맷하면 커서 위치가 꼬여 입력이 섞이는 문제가 있음)
        val digits = value.filter { it.isDigit() }.take(MAX_PHONE_DIGITS)
        setState { copy(contact = digits, errorMessage = null) }
    }

    /** 저장용 하이픈 포맷 (010-1234-5678). */
    private fun formattedContact(): String {
        val d = currentState.contact
        return when {
            d.length <= 3 -> d
            d.length <= 7 -> "${d.take(3)}-${d.drop(3)}"
            else -> "${d.take(3)}-${d.substring(3, 7)}-${d.drop(7)}"
        }
    }

    fun onSubmit() {
        if (!currentState.canSubmit) return
        setState { copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = updateProfile(currentState.nickname, formattedContact())
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

    private companion object {
        const val MAX_PHONE_DIGITS = 11
    }
}
