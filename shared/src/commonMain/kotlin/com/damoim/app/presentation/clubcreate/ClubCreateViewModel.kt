package com.damoim.app.presentation.clubcreate

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.usecase.CreateClubUseCase
import com.damoim.app.domain.usecase.UpdateClubUseCase
import com.damoim.app.domain.usecase.UploadClubImageUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.launch

data class ClubCreateUiState(
    val name: String = "",
    val intro: String = "",
    val category: String = DamoimStrings.CREATE_CATEGORIES.first(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) : UiState {
    val canSubmit: Boolean get() = name.isNotBlank() && !isSaving
}

sealed interface ClubCreateSideEffect : UiSideEffect {
    data object NavigateToHome : ClubCreateSideEffect   // 생성 완료 → 05 홈(동아리장)
    data class ShowError(val message: String) : ClubCreateSideEffect
}

/** 화면 07 동아리 생성. 로고를 고르면 생성 직후 S3 업로드 → 대표 이미지로 반영(프로필과 동일). */
class ClubCreateViewModel(
    private val createClub: CreateClubUseCase,
    private val uploadClubImage: UploadClubImageUseCase,
    private val updateClub: UpdateClubUseCase,
) : BaseViewModel<ClubCreateUiState, ClubCreateSideEffect>(ClubCreateUiState()) {

    // 새로 고른 로고 바이트(생성 후 업로드). null이면 이미지 없음.
    private var pickedBytes: ByteArray? = null
    private var pickedContentType: String? = null

    fun onNameChange(value: String) =
        setState { copy(name = value.take(CreateClubUseCase.MAX_NAME_LENGTH), errorMessage = null) }

    fun onIntroChange(value: String) = setState { copy(intro = value) }

    fun onCategorySelect(category: String) = setState { copy(category = category) }

    /** 로고 선택 — 바이트 보관(생성 성공 후 S3 업로드). */
    fun onPhotoPicked(bytes: ByteArray, contentType: String?) {
        pickedBytes = bytes
        pickedContentType = contentType
    }

    fun onSubmit() {
        if (!currentState.canSubmit) return
        setState { copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val created = createClub(currentState.name, currentState.intro, currentState.category)) {
                is DataResult.Success -> {
                    // 생성 성공 → 세션이 새 동아리(LEADER)로 전환됨. 로고를 골랐으면 업로드 후 반영.
                    val bytes = pickedBytes
                    if (bytes != null) {
                        val up = uploadClubImage(bytes, pickedContentType)
                        if (up is DataResult.Success) updateClub(imageKey = up.data)
                        // 업로드 실패 시: 동아리는 이미 생성됨 → 이미지만 건너뛰고 홈으로 진행.
                    }
                    setState { copy(isSaving = false) }
                    sendEffect(ClubCreateSideEffect.NavigateToHome)
                }
                is DataResult.Failure ->
                    setState { copy(isSaving = false, errorMessage = created.error.message) }
            }
        }
    }
}
