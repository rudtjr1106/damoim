package com.damoim.app.presentation.board.home

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.repository.BoardHomeData
import com.damoim.app.domain.usecase.GetBoardHomeUseCase
import kotlinx.coroutines.launch

data class BoardHomeUiState(
    val isLoading: Boolean = true,
    val home: BoardHomeData? = null,
) : UiState {
    /** 고정·최근 글이 모두 없으면 빈 상태(41). */
    val isEmpty: Boolean get() = home?.isEmpty == true
}

/** 게시판 홈은 네비게이션을 Route 콜백으로 처리하므로 사이드이펙트 미사용(빈 마커). */
sealed interface BoardHomeSideEffect : UiSideEffect

/** 화면 10 게시판 홈. 스토어를 구독해 작성/삭제/고정이 실시간 반영된다. */
class BoardHomeViewModel(
    getBoardHome: GetBoardHomeUseCase,
) : BaseViewModel<BoardHomeUiState, BoardHomeSideEffect>(BoardHomeUiState()) {

    init {
        viewModelScope.launch {
            getBoardHome().collect { data -> setState { copy(isLoading = false, home = data) } }
        }
    }
}
