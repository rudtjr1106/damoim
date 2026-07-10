package com.damoim.app.domain.usecase

import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.BoardPost
import com.damoim.app.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow

/** 카테고리별 게시글 목록 관찰 (화면 11/12/13). category=null이면 전체. */
class GetBoardPostsUseCase(private val boardRepository: BoardRepository) {
    operator fun invoke(category: BoardCategory?): Flow<List<BoardPost>> =
        boardRepository.observePosts(category)
}
