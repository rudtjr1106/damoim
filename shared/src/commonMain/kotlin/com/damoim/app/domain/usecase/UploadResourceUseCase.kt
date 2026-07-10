package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.ResourceDraft
import com.damoim.app.domain.repository.ResourceRepository

/** 69 자료 올리기. */
class UploadResourceUseCase(private val resourceRepository: ResourceRepository) {
    suspend operator fun invoke(draft: ResourceDraft): DataResult<Long> =
        resourceRepository.uploadResource(draft)
}
