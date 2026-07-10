package com.damoim.app.data.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.data.mock.MockStore
import com.damoim.app.domain.model.ResourceDraft
import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.domain.model.ResourceFolder
import com.damoim.app.domain.repository.ResourceRepository
import com.damoim.app.domain.repository.StorageUsage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/**
 * [ResourceRepository]의 Mock 구현 — [MockStore]에 위임. 서버 도입 시 Ktor 구현으로 교체.
 */
class MockResourceRepository : ResourceRepository {

    override fun observeResources(folder: ResourceFolder?): Flow<List<ResourceFile>> =
        MockStore.resourcesFlow(folder)

    override fun observeResourceDetail(resourceId: Long): Flow<ResourceFile?> =
        MockStore.resourceDetailFlow(resourceId)

    override fun observeStorage(): Flow<StorageUsage> = MockStore.storageFlow()

    override suspend fun uploadResource(draft: ResourceDraft): DataResult<Long> {
        delay(WRITE_DELAY_MS)
        return DataResult.Success(MockStore.uploadResource(draft))
    }

    override suspend fun deleteResource(resourceId: Long): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.deleteResource(resourceId)
        return DataResult.Success(Unit)
    }

    override suspend fun incrementDownload(resourceId: Long): DataResult<Unit> {
        MockStore.incrementDownload(resourceId)
        return DataResult.Success(Unit)
    }

    private companion object {
        const val WRITE_DELAY_MS = 350L
    }
}
