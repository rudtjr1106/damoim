package com.damoim.app.domain.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.ResourceDraft
import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.domain.model.ResourceFolder
import kotlinx.coroutines.flow.Flow

/** 자료실(D 그룹) 저장소. 조회는 Flow, 변경은 suspend + DataResult. */
interface ResourceRepository {
    fun observeResources(folder: ResourceFolder?): Flow<List<ResourceFile>>
    fun observeResourceDetail(resourceId: Long): Flow<ResourceFile?>
    fun observeStorage(): Flow<StorageUsage>

    suspend fun uploadResource(draft: ResourceDraft): DataResult<Long>
    suspend fun deleteResource(resourceId: Long): DataResult<Unit>
    /** 실제 다운로드용 presigned URL 조회(서버가 다운로드 카운트도 증가). */
    suspend fun getDownloadUrl(resourceId: Long): DataResult<String>
}

/** 67 저장공간 바. [usedBytes]는 실제 파일 크기 합계. */
data class StorageUsage(
    val usedBytes: Long = 0,
    val totalBytes: Long = 0,
    val usedLabel: String = "0KB",
    val totalLabel: String = "5GB",
    val count: Int = 0,
) {
    val fraction: Float
        get() = if (totalBytes <= 0L) 0f else (usedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
}
