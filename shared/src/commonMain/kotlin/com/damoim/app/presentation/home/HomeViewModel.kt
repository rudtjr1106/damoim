package com.damoim.app.presentation.home

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
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
 * 화면 05/06 홈. 스토어를 구독해 회원 수·신청 대기·게시판 미리보기·알림 배지가
 * 실시간 반영된다(승인/글 작성/읽음 처리 즉시 갱신).
 */
class HomeViewModel(
    getHomeSummary: GetHomeSummaryUseCase,
) : BaseViewModel<HomeUiState, HomeSideEffect>(HomeUiState()) {

    init {
        viewModelScope.launch {
            getHomeSummary().collect { summary -> setState { copy(isLoading = false, summary = summary) } }
        }
    }
}
