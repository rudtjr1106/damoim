package com.damoim.app.domain.usecase

import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow

/** 68 자료 상세. 삭제되면 null을 방출한다. */
class GetResourceDetailUseCase(private val resourceRepository: ResourceRepository) {
    operator fun invoke(resourceId: Long): Flow<ResourceFile?> =
        resourceRepository.observeResourceDetail(resourceId)
}
