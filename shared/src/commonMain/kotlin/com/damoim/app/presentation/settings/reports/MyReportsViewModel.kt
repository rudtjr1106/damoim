package com.damoim.app.presentation.settings.reports

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.MyReport
import com.damoim.app.domain.usecase.MyReportsUseCase
import kotlinx.coroutines.launch

data class MyReportsUiState(
    val isLoading: Boolean = true,
    val reports: List<MyReport> = emptyList(),
) : UiState {
    val isEmpty: Boolean get() = !isLoading && reports.isEmpty()
}

sealed interface MyReportsSideEffect : UiSideEffect

/** 34 내가 신고한 내역(조회 전용). */
class MyReportsViewModel(
    private val myReports: MyReportsUseCase,
) : BaseViewModel<MyReportsUiState, MyReportsSideEffect>(MyReportsUiState()) {

    init {
        viewModelScope.launch {
            myReports.observe().collect { setState { copy(isLoading = false, reports = it) } }
        }
    }
}
