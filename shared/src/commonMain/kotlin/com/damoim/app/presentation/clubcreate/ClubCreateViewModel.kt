package com.damoim.app.presentation.clubcreate

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.usecase.CreateClubUseCase
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

/** 화면 07 동아리 생성. */
class ClubCreateViewModel(
    private val createClub: CreateClubUseCase,
) : BaseViewModel<ClubCreateUiState, ClubCreateSideEffect>(ClubCreateUiState()) {

    fun onNameChange(value: String) =
        setState { copy(name = value.take(CreateClubUseCase.MAX_NAME_LENGTH), errorMessage = null) }

    fun onIntroChange(value: String) = setState { copy(intro = value) }

    fun onCategorySelect(category: String) = setState { copy(category = category) }

    fun onSubmit() {
        if (!currentState.canSubmit) return
        setState { copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = createClub(currentState.name, currentState.intro, currentState.category)
            handleResult(
                result = result,
                onSuccess = {
                    setState { copy(isSaving = false) }
                    sendEffect(ClubCreateSideEffect.NavigateToHome)
                },
                onFailure = { setState { copy(isSaving = false, errorMessage = it.message) } },
            )
        }
    }
}
