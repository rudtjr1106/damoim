package com.damoim.app.domain.usecase

import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.domain.model.ResourceFolder
import com.damoim.app.domain.repository.ResourceRepository
import kotlinx.coroutines.flow.Flow

/** 67 자료실 홈 목록. [folder]가 null이면 전체. */
class GetResourcesUseCase(private val resourceRepository: ResourceRepository) {
    operator fun invoke(folder: ResourceFolder? = null): Flow<List<ResourceFile>> =
        resourceRepository.observeResources(folder)
}
