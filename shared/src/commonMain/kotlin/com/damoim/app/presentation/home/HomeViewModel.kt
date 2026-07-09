package com.damoim.app.presentation.home

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.HomeSummary
import com.damoim.app.domain.usecase.GetHomeSummaryUseCase
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val summary: HomeSummary? = null,
) : UiState

/** 홈은 네비게이션을 Route 콜백으로 처리하므로 사이드이펙트는 사용하지 않는다(빈 마커). */
sealed interface HomeSideEffect : UiSideEffect

/**
 * 화면 05/06 홈. role에 따라 동아리장/일반회원 요약을 불러온다.
 */
class HomeViewModel(
    private val getHomeSummary: GetHomeSummaryUseCase,
    private val role: ClubRole,
) : BaseViewModel<HomeUiState, HomeSideEffect>(HomeUiState()) {

    init { load() }

    private fun load() = viewModelScope.launch {
        setState { copy(isLoading = true) }
        val result = getHomeSummary(role)
        setState { copy(isLoading = false) }
        handleResult(result, onSuccess = { summary -> setState { copy(summary = summary) } })
    }
}
