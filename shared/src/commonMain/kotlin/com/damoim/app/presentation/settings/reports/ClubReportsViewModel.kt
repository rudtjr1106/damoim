package com.damoim.app.presentation.settings.reports

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.ClubReport
import com.damoim.app.domain.usecase.ClubReportsUseCase
import kotlinx.coroutines.launch

data class ClubReportsUiState(
    val isLoading: Boolean = true,
    val reports: List<ClubReport> = emptyList(),
) : UiState {
    val isEmpty: Boolean get() = !isLoading && reports.isEmpty()
}

sealed interface ClubReportsSideEffect : UiSideEffect

/** 35 운영진 — 동아리 신고 목록(조회 전용 모더레이션 뷰). */
class ClubReportsViewModel(
    private val clubReports: ClubReportsUseCase,
) : BaseViewModel<ClubReportsUiState, ClubReportsSideEffect>(ClubReportsUiState()) {

    init {
        viewModelScope.launch {
            clubReports.observe().collect { setState { copy(isLoading = false, reports = it) } }
        }
    }
}
