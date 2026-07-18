package com.damoim.app.data.remote.resource

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.core.result.getOrNull
import com.damoim.app.core.result.map
import com.damoim.app.data.remote.core.ApiClient
import com.damoim.app.data.remote.core.ApiRoutes
import com.damoim.app.data.remote.core.DataTopic
import com.damoim.app.data.remote.core.ErrorCodes
import com.damoim.app.data.remote.core.RemoteBus
import com.damoim.app.data.remote.core.SharedFlows
import com.damoim.app.data.remote.core.reactiveFlow
import com.damoim.app.domain.model.ResourceDraft
import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.domain.model.ResourceFolder
import com.damoim.app.domain.repository.ResourceRepository
import com.damoim.app.domain.repository.StorageUsage
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow

/**
 * [ResourceRepository]의 서버 구현 (D 자료실). 변경은 [DataTopic.RESOURCE]만 무효화.
 *
 * 업로드는 3단계: POST /upload-url → [rawClient]로 S3 직접 PUT → POST / 등록.
 */
class RemoteResourceRepository(private val api: ApiClient) : ResourceRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val shared = SharedFlows(scope)

    /** presigned PUT 전용 — 인증/베이스URL/ContentNegotiation 없는 순수 클라이언트. */
    private val rawClient by lazy { HttpClient() }

    override fun observeResources(folder: ResourceFolder?): Flow<List<ResourceFile>> =
        shared.get("resources:${folder?.name}") {
            reactiveFlow(DataTopic.RESOURCE, fallback = emptyList()) {
                api.getData<List<ResourceResponseDto>>(
                    ApiRoutes.Resources.ROOT,
                    mapOf("folder" to folder?.name),
                ).getOrNull()?.toDomainList() ?: emptyList()
            }
        }

    override fun observeResourceDetail(resourceId: Long): Flow<ResourceFile?> =
        shared.get("resource:$resourceId") {
            reactiveFlow<ResourceFile?>(DataTopic.RESOURCE, fallback = null) {
                api.getData<ResourceResponseDto>(ApiRoutes.Resources.detail(resourceId))
                    .getOrNull()?.toDomain(orderKey = 0L)
            }
        }

    override fun observeStorage(): Flow<StorageUsage> = shared.get("storage") {
        reactiveFlow(DataTopic.RESOURCE, fallback = StorageUsage()) {
            val usage = api.getData<StorageUsageResponseDto>(ApiRoutes.Resources.STORAGE).getOrNull()
            // count는 서버 필드가 없어 전체 목록 크기에서 파생.
            val count = api.getData<List<ResourceResponseDto>>(ApiRoutes.Resources.ROOT).getOrNull()?.size ?: 0
            usage?.toDomain(count) ?: StorageUsage(count = count)
        }
    }

    override suspend fun uploadResource(draft: ResourceDraft): DataResult<Long> {
        val bytes = draft.bytes ?: ByteArray(0)
        val sizeBytes = draft.bytes?.size?.toLong()?.coerceAtLeast(1L)
            ?: parseSizeLabelToBytes(draft.sizeLabel)
        val ext = draft.fileName.substringAfterLast('.', "").uppercase().take(16)

        // 1) presigned URL 발급(폴더 권한·쿼터 사전 검증 포함)
        val presign = api.postData<UploadUrlResponseDto>(
            ApiRoutes.Resources.UPLOAD_URL,
            UploadUrlRequestDto(
                fileName = draft.fileName,
                contentType = draft.contentType,
                sizeBytes = sizeBytes,
                folder = draft.folder.name,
            ),
        )
        val upload = when (presign) {
            is DataResult.Success -> presign.data
            is DataResult.Failure -> return presign
        }

        // 2) S3에 바이트 직접 PUT (bare client — 앱 인증/JSON 없이)
        val putOk = runCatching {
            rawClient.put(upload.uploadUrl) {
                setBody(bytes)
                draft.contentType?.let { header(HttpHeaders.ContentType, it) }
            }.status.isSuccess()
        }.getOrDefault(false)
        if (!putOk) return DataResult.Failure(DataError(ErrorCodes.UPLOAD_FAILED, "파일 업로드에 실패했어요"))

        // 3) 등록
        return api.postData<ResourceResponseDto>(
            ApiRoutes.Resources.ROOT,
            CreateResourceRequestDto(
                title = draft.title,
                description = draft.description,
                folder = draft.folder.name,
                visibility = draft.visibility.name,
                cohortIds = draft.cohortIds,
                fileName = draft.fileName,
                ext = ext,
                sizeBytes = sizeBytes,
                storageKey = upload.storageKey,
            ),
        ).map { it.id }.also { RemoteBus.invalidate(DataTopic.RESOURCE) }
    }

    override suspend fun deleteResource(resourceId: Long): DataResult<Unit> =
        api.deleteUnit(ApiRoutes.Resources.detail(resourceId)).also { RemoteBus.invalidate(DataTopic.RESOURCE) }

    override suspend fun getDownloadUrl(resourceId: Long): DataResult<String> {
        // download-url 호출로 presigned URL을 받고(서버가 다운로드 카운트도 증가) URL을 그대로 돌려준다.
        val result = api.getData<DownloadUrlResponseDto>(ApiRoutes.Resources.downloadUrl(resourceId))
        RemoteBus.invalidate(DataTopic.RESOURCE)
        return result.map { it.downloadUrl }
    }
}
