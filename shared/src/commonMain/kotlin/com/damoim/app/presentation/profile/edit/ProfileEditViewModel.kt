package com.damoim.app.presentation.profile.edit

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.usecase.GetAuthUserUseCase
import com.damoim.app.domain.usecase.UpdateProfileUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.launch

data class ProfileEditUiState(
    val name: String = "",
    val contact: String = "",
    val email: String = "",
    val bio: String = "",          // 한 줄 소개 — 현재는 표시용(AuthUser 스키마 미확장, 서버 도입 시 저장)
    val isSaving: Boolean = false,
) : UiState {
    val canSave: Boolean get() = name.isNotBlank() && !isSaving
}

sealed interface ProfileEditSideEffect : UiSideEffect {
    data object Saved : ProfileEditSideEffect
}

/**
 * 화면 45 프로필 수정 — 현재 프로필을 프리필하고 저장한다. 편집 시작 전에는 항상 최신 프로필로 갱신,
 * 편집이 시작되면(dirty) 사용자 입력을 유지한다(수동 백스택에서 VM이 재사용되므로).
 */
class ProfileEditViewModel(
    getAuthUser: GetAuthUserUseCase,
    private val updateProfile: UpdateProfileUseCase,
) : BaseViewModel<ProfileEditUiState, ProfileEditSideEffect>(ProfileEditUiState()) {

    private var dirty = false

    init {
        viewModelScope.launch {
            getAuthUser().collect { user ->
                if (!dirty) setState { copy(name = user.nickname, contact = user.contact ?: "", email = user.email ?: "") }
            }
        }
    }

    fun onNameChange(value: String) { dirty = true; setState { copy(name = value.take(UpdateProfileUseCase.MAX_NICKNAME_LENGTH)) } }
    fun onContactChange(value: String) { dirty = true; setState { copy(contact = value.filter { it.isDigit() }.take(11)) } }
    fun onBioChange(value: String) { dirty = true; setState { copy(bio = value.take(40)) } }

    /** 취소로 나갈 때 편집 폐기 — 다음 진입에서 최신 프로필로 다시 프리필되게 한다. */
    fun onCancel() { dirty = false }

    fun onSave() {
        if (!currentState.canSave) return
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            val result = updateProfile(currentState.name.trim(), currentState.contact, null)
            setState { copy(isSaving = false) }
            handleResult(result, onSuccess = { dirty = false; sendEffect(ProfileEditSideEffect.Saved) })
        }
    }
}
