package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.PostDraft
import com.damoim.app.domain.repository.BoardRepository

/** 게시글 작성/수정 제출 (화면 15/34/35/39/70). */
class SubmitPostUseCase(private val boardRepository: BoardRepository) {
    suspend fun create(draft: PostDraft): DataResult<Long> = boardRepository.createPost(draft)
    suspend fun update(postId: Long, draft: PostDraft): DataResult<Unit> = boardRepository.updatePost(postId, draft)
}
