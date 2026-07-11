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
                posts.filter { it.recruit?.status == RecruitStatus.OPEN }
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
