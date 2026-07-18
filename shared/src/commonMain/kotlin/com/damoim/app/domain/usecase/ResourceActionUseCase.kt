package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.repository.ResourceRepository

/** 68 자료 상세의 상호작용 묶음 — 다운로드(카운트 증가)/삭제. */
class ResourceActionUseCase(private val resourceRepository: ResourceRepository) {
    suspend fun download(resourceId: Long): DataResult<String> = resourceRepository.getDownloadUrl(resourceId)
    suspend fun delete(resourceId: Long): DataResult<Unit> = resourceRepository.deleteResource(resourceId)
}
