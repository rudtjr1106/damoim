package com.damoim.app.presentation.member.cohort

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.Cohort
import com.damoim.app.domain.usecase.CohortActionUseCase
import com.damoim.app.domain.usecase.GetCohortsUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.launch

data class CohortManageUiState(
    val isLoading: Boolean = true,
    val cohorts: List<Cohort> = emptyList(),
) : UiState

sealed interface CohortManageSideEffect : UiSideEffect {
    data class Toast(val message: String) : CohortManageSideEffect
}

/** 화면 19 기수 관리 — 추가·이름 변경이 스토어에 실제 반영된다. */
class CohortManageViewModel(
    getCohorts: GetCohortsUseCase,
    private val cohortAction: CohortActionUseCase,
) : BaseViewModel<CohortManageUiState, CohortManageSideEffect>(CohortManageUiState()) {

    init {
        viewModelScope.launch {
            getCohorts().collect { list -> setState { copy(isLoading = false, cohorts = list) } }
        }
    }

    fun onAdd(shortLabel: String, displayName: String) = viewModelScope.launch {
        handleResult(cohortAction.add(shortLabel, displayName), onSuccess = { sendEffect(CohortManageSideEffect.Toast(DamoimStrings.TOAST_COHORT_ADDED)) })
    }

    fun onRename(cohortId: Long, shortLabel: String, displayName: String) = viewModelScope.launch {
        handleResult(cohortAction.rename(cohortId, shortLabel, displayName), onSuccess = { sendEffect(CohortManageSideEffect.Toast(DamoimStrings.TOAST_COHORT_RENAMED)) })
    }
}
