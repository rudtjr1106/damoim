package com.damoim.app.presentation.clubsettings

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.usecase.DisableJoinCodeUseCase
import com.damoim.app.domain.usecase.GetClubInfoUseCase
import com.damoim.app.domain.usecase.RegenerateJoinCodeUseCase
import com.damoim.app.domain.usecase.UpdateClubUseCase
import com.damoim.app.domain.usecase.UploadClubImageUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.launch

data class ClubSettingsUiState(
    val isLoading: Boolean = true,
    val clubName: String = "",
    val intro: String = "",
    val clubInitial: String = "",
    val imageUrl: String? = null,        // 저장된 대표 이미지(없으면 이니셜 로고)
    val joinCode: String = "",           // 비활성화 상태면 빈 문자열
    val showShareSheet: Boolean = false,
    val isSaving: Boolean = false,
) : UiState {
    val codeDisabled: Boolean get() = !isLoading && joinCode.isEmpty()
}

sealed interface ClubSettingsSideEffect : UiSideEffect {
    data class Toast(val message: String) : ClubSettingsSideEffect
}

/** 화면 08 동아리 정보 설정 · 가입 코드 발급 (+ 59 공유 시트). 대표 이미지는 S3 업로드 후 서버에 저장. */
class ClubSettingsViewModel(
    getClubInfo: GetClubInfoUseCase,
    private val regenerateJoinCode: RegenerateJoinCodeUseCase,
    private val disableJoinCode: DisableJoinCodeUseCase,
    private val uploadClubImage: UploadClubImageUseCase,
    private val updateClub: UpdateClubUseCase,
) : BaseViewModel<ClubSettingsUiState, ClubSettingsSideEffect>(ClubSettingsUiState()) {

    // 새로 고른 대표 이미지 바이트(저장 시 업로드). null이면 이미지 변경 없음.
    private var pickedBytes: ByteArray? = null
    private var pickedContentType: String? = null

    init {
        viewModelScope.launch {
            getClubInfo().collect { club ->
                club ?: return@collect
                setState {
                    copy(
                        isLoading = false,
                        clubName = club.name,
                        intro = club.description,
                        clubInitial = club.name.take(1),
                        imageUrl = club.imageUrl,
                        joinCode = club.joinCode,
                    )
                }
            }
        }
    }

    /** 대표 이미지 선택 — 바이트를 보관(저장 시 업로드). */
    fun onPhotoPicked(bytes: ByteArray, contentType: String?) {
        pickedBytes = bytes
        pickedContentType = contentType
    }

    fun onOpenShare() = setState { copy(showShareSheet = true) }
    fun onCloseShare() = setState { copy(showShareSheet = false) }

    fun onRegenerate() = viewModelScope.launch {
        handleResult(regenerateJoinCode(), onSuccess = {
            sendEffect(ClubSettingsSideEffect.Toast(DamoimStrings.TOAST_CODE_REGENERATED))
        })
    }

    fun onDisable() = viewModelScope.launch {
        handleResult(disableJoinCode(), onSuccess = {
            sendEffect(ClubSettingsSideEffect.Toast(DamoimStrings.TOAST_CODE_DISABLED))
        })
    }

    fun onCopyCode() = sendEffect(ClubSettingsSideEffect.Toast(DamoimStrings.TOAST_CODE_COPIED))

    /** 저장 — 새로 고른 이미지가 있으면 S3 업로드 후 서버에 반영. 없으면 변경 없음 안내. */
    fun onSave() {
        if (currentState.isSaving) return
        val bytes = pickedBytes
        if (bytes == null) {
            sendEffect(ClubSettingsSideEffect.Toast(DamoimStrings.COMMON_SAVE))
            return
        }
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            val imageKey = when (val up = uploadClubImage(bytes, pickedContentType)) {
                is DataResult.Success -> up.data
                is DataResult.Failure -> {
                    setState { copy(isSaving = false) }
                    sendEffect(ClubSettingsSideEffect.Toast(up.error.message))
                    return@launch
                }
            }
            val result = updateClub(imageKey = imageKey)
            setState { copy(isSaving = false) }
            handleResult(
                result,
                onSuccess = { club ->
                    pickedBytes = null
                    setState { copy(imageUrl = club.imageUrl) }
                    sendEffect(ClubSettingsSideEffect.Toast(DamoimStrings.COMMON_SAVE))
                },
                onFailure = { sendEffect(ClubSettingsSideEffect.Toast(it.message)) },
            )
        }
    }

    fun onKakaoShare() = sendEffect(ClubSettingsSideEffect.Toast(DamoimStrings.TOAST_KAKAO_SHARE))
    fun onCopyLink() = sendEffect(ClubSettingsSideEffect.Toast(DamoimStrings.TOAST_LINK_COPIED))
}
