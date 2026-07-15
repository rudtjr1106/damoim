package com.damoim.app.presentation.board.list

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.BoardPost
import com.damoim.app.domain.model.RecruitStatus
import com.damoim.app.domain.usecase.GetBoardPostsUseCase
import kotlinx.coroutines.launch

/** 목록 정렬(11). */
enum class BoardSort { RECENT, POPULAR, COMMENTS }

data class BoardListUiState(
    val category: BoardCategory,
    val isLoading: Boolean = true,
    val posts: List<BoardPost> = emptyList(),
    val sort: BoardSort = BoardSort.RECENT,
    val recruitOpenOnly: Boolean = false,
) : UiState {
    /** 정렬·필터 적용된 표시 목록. */
    val displayed: List<BoardPost>
        get() {
            val filtered = if (category == BoardCategory.RECRUIT && recruitOpenOnly) {
                // '모집중만' = 마감(CLOSED)만 숨긴다. recruit 정보가 없는(null) 글은 상태 불명이라
                // 숨기지 않는다 — 예전처럼 == OPEN 으로 걸면 recruit가 비면 전부 사라져 필터가 깨졌음.
                posts.filter { it.recruit?.status != RecruitStatus.CLOSED }
            } else posts
            return when (sort) {
                BoardSort.RECENT -> filtered.sortedByDescending { it.createdAt }
                BoardSort.POPULAR -> filtered.sortedWith(compareByDescending<BoardPost> { it.likeCount }.thenByDescending { it.createdAt })
                BoardSort.COMMENTS -> filtered.sortedWith(compareByDescending<BoardPost> { it.commentCount }.thenByDescending { it.createdAt })
            }
        }
}

sealed interface BoardListSideEffect : UiSideEffect

/** 화면 11 자유 / 12 공지 / 13 모집 게시판 목록. 정렬 칩·모집중만 필터가 실제 동작한다. */
class BoardListViewModel(
    getBoardPosts: GetBoardPostsUseCase,
    category: BoardCategory,
) : BaseViewModel<BoardListUiState, BoardListSideEffect>(BoardListUiState(category = category)) {

    init {
        viewModelScope.launch {
            getBoardPosts(category).collect { list -> setState { copy(isLoading = false, posts = list) } }
        }
    }

    fun onSort(sort: BoardSort) = setState { copy(sort = sort) }
    fun onToggleOpenOnly() = setState { copy(recruitOpenOnly = !recruitOpenOnly) }
}
