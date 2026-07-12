package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.ResourceDraft
import com.damoim.app.domain.repository.ResourceRepository

/** 69 자료 올리기. */
class UploadResourceUseCase(private val resourceRepository: ResourceRepository) {
    suspend operator fun invoke(draft: ResourceDraft): DataResult<Long> {
        val bytes = draft.bytes
        if (bytes != null && bytes.size > ResourceDraft.MAX_UPLOAD_BYTES) {
            return DataResult.Failure(DataError("TOO_LARGE", "파일은 25MB 이하만 올릴 수 있어요"))
        }
        return resourceRepository.uploadResource(draft)
    }
}
