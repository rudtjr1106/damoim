package com.damoim.app.presentation.clubsettings

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.usecase.GetClubInfoUseCase
import com.damoim.app.domain.usecase.RegenerateJoinCodeUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.launch

data class ClubSettingsUiState(
    val isLoading: Boolean = true,
    val clubName: String = "",
    val intro: String = "",
    val clubInitial: String = "",
    val joinCode: String = "",
    val showShareSheet: Boolean = false,
) : UiState

sealed interface ClubSettingsSideEffect : UiSideEffect {
    data class Toast(val message: String) : ClubSettingsSideEffect
}

/** 화면 08 동아리 정보 설정 · 가입 코드 발급 (+ 59 공유 시트). */
class ClubSettingsViewModel(
    private val getClubInfo: GetClubInfoUseCase,
    private val regenerateJoinCode: RegenerateJoinCodeUseCase,
) : BaseViewModel<ClubSettingsUiState, ClubSettingsSideEffect>(ClubSettingsUiState()) {

    init { load() }

    private fun load() = viewModelScope.launch {
        val result = getClubInfo()
        setState { copy(isLoading = false) }
        handleResult(result, onSuccess = { club ->
            setState {
                copy(
                    clubName = club.name,
                    intro = club.description,
                    clubInitial = club.name.take(1),
                    joinCode = club.joinCode,
                )
            }
        })
    }

    fun onOpenShare() = setState { copy(showShareSheet = true) }
    fun onCloseShare() = setState { copy(showShareSheet = false) }

    fun onRegenerate() = viewModelScope.launch {
        handleResult(regenerateJoinCode(), onSuccess = { newCode ->
            setState { copy(joinCode = newCode) }
            sendEffect(ClubSettingsSideEffect.Toast(DamoimStrings.TOAST_CODE_REGENERATED))
        })
    }

    fun onCopyCode() = sendEffect(ClubSettingsSideEffect.Toast(DamoimStrings.TOAST_CODE_COPIED))
    fun onDisable() = sendEffect(ClubSettingsSideEffect.Toast(DamoimStrings.TOAST_CODE_DISABLED))
    fun onSave() = sendEffect(ClubSettingsSideEffect.Toast(DamoimStrings.COMMON_SAVE))
    fun onKakaoShare() = sendEffect(ClubSettingsSideEffect.Toast(DamoimStrings.TOAST_KAKAO_SHARE))
    fun onCopyLink() = sendEffect(ClubSettingsSideEffect.Toast(DamoimStrings.TOAST_LINK_COPIED))
}
