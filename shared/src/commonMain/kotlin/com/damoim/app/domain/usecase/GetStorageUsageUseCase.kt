package com.damoim.app.domain.usecase

import com.damoim.app.domain.repository.ResourceRepository
import com.damoim.app.domain.repository.StorageUsage
import kotlinx.coroutines.flow.Flow

/** 67 저장공간 바 — 업로드/삭제 시 자동 재계산된다. */
class GetStorageUsageUseCase(private val resourceRepository: ResourceRepository) {
    operator fun invoke(): Flow<StorageUsage> = resourceRepository.observeStorage()
}
