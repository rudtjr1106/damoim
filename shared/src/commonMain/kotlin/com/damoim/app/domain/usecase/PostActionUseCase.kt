package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.repository.BoardRepository

/**
 * 게시글 상호작용 묶음 (화면 14/36/84 + 54/55/56 오버레이).
 * 좋아요/투표/모집 신청/댓글/삭제/상단 고정.
 */
class PostActionUseCase(private val boardRepository: BoardRepository) {
    suspend fun toggleLike(postId: Long): DataResult<Unit> = boardRepository.toggleLike(postId)
    suspend fun vote(postId: Long, optionIndex: Int): DataResult<Unit> = boardRepository.votePoll(postId, optionIndex)
    suspend fun clearVote(postId: Long): DataResult<Unit> = boardRepository.clearPollVote(postId)
    suspend fun applyRecruit(postId: Long): DataResult<Boolean> = boardRepository.applyRecruit(postId)
    suspend fun addComment(postId: Long, content: String, parentId: Long?): DataResult<Unit> =
        boardRepository.addComment(postId, content, parentId)
    suspend fun delete(postId: Long): DataResult<Unit> = boardRepository.deletePost(postId)
    suspend fun togglePin(postId: Long): DataResult<Boolean> = boardRepository.togglePin(postId)
}
