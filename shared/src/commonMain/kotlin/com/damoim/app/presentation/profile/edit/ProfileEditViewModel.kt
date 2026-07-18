package com.damoim.app.presentation.profile.edit

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.usecase.GetAuthUserUseCase
import com.damoim.app.domain.usecase.UpdateProfileUseCase
import com.damoim.app.domain.usecase.UploadProfileImageUseCase
import kotlinx.coroutines.launch

data class ProfileEditUiState(
    val name: String = "",
    val contact: String = "",
    val profileImageUrl: String? = null, // 현재 저장된 프로필 사진(없으면 이니셜 아바타)
    val isSaving: Boolean = false,
) : UiState {
    // 이름만 필수 — 연락처 없이 사진/이름만 바꿔도 저장(업로드한 이미지 키 반영)이 되게 한다.
    val canSave: Boolean get() = name.isNotBlank() && !isSaving
}

sealed interface ProfileEditSideEffect : UiSideEffect {
    data object Saved : ProfileEditSideEffect
    data class Toast(val message: String) : ProfileEditSideEffect
}

/**
 * 화면 45 프로필 수정 — 현재 프로필을 프리필하고 저장한다. 사진을 새로 고르면 저장 시 S3에 업로드 후
 * key로 반영. 편집이 시작되면(dirty) 사용자 입력을 유지한다(수동 백스택에서 VM이 재사용되므로).
 */
class ProfileEditViewModel(
    getAuthUser: GetAuthUserUseCase,
    private val updateProfile: UpdateProfileUseCase,
    private val uploadProfileImage: UploadProfileImageUseCase,
) : BaseViewModel<ProfileEditUiState, ProfileEditSideEffect>(ProfileEditUiState()) {

    private var dirty = false

    // 새로 고른 사진 바이트(저장 시 업로드). null이면 사진 변경 없음.
    private var pickedBytes: ByteArray? = null
    private var pickedContentType: String? = null

    init {
        viewModelScope.launch {
            getAuthUser().collect { user ->
                if (!dirty) {
                    setState {
                        copy(
                            name = user.nickname,
                            contact = user.contact ?: "",
                            profileImageUrl = user.profileImageUrl,
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) { dirty = true; setState { copy(name = value.take(UpdateProfileUseCase.MAX_NICKNAME_LENGTH)) } }
    fun onContactChange(value: String) { dirty = true; setState { copy(contact = value.filter { it.isDigit() }.take(11)) } }

    /** 사진 선택 — 바이트를 보관(저장 시 업로드). */
    fun onPhotoPicked(bytes: ByteArray, contentType: String?) {
        dirty = true
        pickedBytes = bytes
        pickedContentType = contentType
    }

    /** 취소로 나갈 때 편집 폐기 — 다음 진입에서 최신 프로필로 다시 프리필되게 한다. */
    fun onCancel() { dirty = false; pickedBytes = null }

    fun onSave() {
        if (!currentState.canSave) return
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            // 새 사진이 있으면 먼저 S3 업로드 → key. 업로드 실패 시 저장 중단.
            var imageKey: String? = null
            val bytes = pickedBytes
            if (bytes != null) {
                when (val up = uploadProfileImage(bytes, pickedContentType)) {
                    is DataResult.Success -> imageKey = up.data
                    is DataResult.Failure -> {
                        setState { copy(isSaving = false) }
                        sendEffect(ProfileEditSideEffect.Toast(up.error.message))
                        return@launch
                    }
                }
            }
            val result = updateProfile(currentState.name.trim(), currentState.contact, null, imageKey)
            setState { copy(isSaving = false) }
            handleResult(
                result,
                onSuccess = { dirty = false; pickedBytes = null; sendEffect(ProfileEditSideEffect.Saved) },
                // 실패를 조용히 삼키면 "업로드만 되고 안 바뀜"으로 보인다 — 반드시 사용자에게 알린다.
                onFailure = { sendEffect(ProfileEditSideEffect.Toast(it.message)) },
            )
        }
    }
}
