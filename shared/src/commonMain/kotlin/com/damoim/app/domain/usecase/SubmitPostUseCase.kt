package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.PostDraft
import com.damoim.app.domain.repository.BoardRepository

/** 게시글 작성/수정 제출 + 임시저장 (화면 15/34/35/39/70). */
class SubmitPostUseCase(private val boardRepository: BoardRepository) {
    suspend fun create(draft: PostDraft): DataResult<Long> {
        val result = boardRepository.createPost(draft)
        if (result is DataResult.Success) boardRepository.clearDraft()
        return result
    }

    suspend fun update(postId: Long, draft: PostDraft): DataResult<Unit> = boardRepository.updatePost(postId, draft)

    suspend fun saveDraft(draft: PostDraft): DataResult<Unit> = boardRepository.saveDraft(draft)
    fun loadDraft(): PostDraft? = boardRepository.loadDraft()
}
