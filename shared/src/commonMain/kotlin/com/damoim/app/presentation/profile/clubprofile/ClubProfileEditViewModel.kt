package com.damoim.app.presentation.profile.clubprofile

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.usecase.GetMyMemberUseCase
import com.damoim.app.domain.usecase.UpdateClubProfileUseCase
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ClubProfileEditUiState(
    val displayName: String = "",
    val loaded: Boolean = false,
    val saving: Boolean = false,
) : UiState {
    val canSave: Boolean get() = displayName.isNotBlank() && !saving
}

sealed interface ClubProfileEditSideEffect : UiSideEffect {
    data object Saved : ClubProfileEditSideEffect
    data class Failed(val message: String) : ClubProfileEditSideEffect
}

/** 44 동아리별 프로필 수정 — 현재 동아리에서의 표시 이름만 바꾼다. 전역 프로필(45)과 별개. */
class ClubProfileEditViewModel(
    getMyMember: GetMyMemberUseCase,
    private val updateClubProfile: UpdateClubProfileUseCase,
) : BaseViewModel<ClubProfileEditUiState, ClubProfileEditSideEffect>(ClubProfileEditUiState()) {

    init {
        viewModelScope.launch {
            // 현재 동아리 표시 이름으로 프리필(오버라이드 없으면 전역 닉네임).
            val me = getMyMember().filterNotNull().first()
            setState { copy(displayName = me.name, loaded = true) }
        }
    }

    fun onNameChange(value: String) = setState { copy(displayName = value) }

    fun onSave() {
        if (!currentState.canSave) return
        setState { copy(saving = true) }
        viewModelScope.launch {
            val result = updateClubProfile(currentState.displayName.trim())
            setState { copy(saving = false) }
            when (result) {
                is DataResult.Success -> sendEffect(ClubProfileEditSideEffect.Saved)
                is DataResult.Failure -> sendEffect(ClubProfileEditSideEffect.Failed(result.error.message))
            }
        }
    }
}
