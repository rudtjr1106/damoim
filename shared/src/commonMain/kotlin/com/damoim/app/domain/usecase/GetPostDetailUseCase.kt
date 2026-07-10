package com.damoim.app.domain.usecase

import com.damoim.app.domain.model.PostDetail
import com.damoim.app.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow

/** 게시글 상세 관찰 (화면 14/36/84). 댓글/좋아요/투표 변경이 실시간 반영. 부재 시 null. */
class GetPostDetailUseCase(private val boardRepository: BoardRepository) {
    operator fun invoke(postId: Long): Flow<PostDetail?> =
        boardRepository.observePostDetail(postId)
}
