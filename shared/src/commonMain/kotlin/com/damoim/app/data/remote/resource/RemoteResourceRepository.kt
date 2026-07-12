package com.damoim.app.data.remote.resource

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.core.result.getOrNull
import com.damoim.app.core.result.map
import com.damoim.app.data.remote.core.ApiClient
import com.damoim.app.data.remote.core.ApiRoutes
import com.damoim.app.data.remote.core.ErrorCodes
import com.damoim.app.data.remote.core.RemoteBus
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
import kotlinx.coroutines.flow.Flow

/**
 * [ResourceRepository]의 서버 구현 (D 자료실).
 *
 * 업로드는 3단계: (1) POST /upload-url 로 presigned PUT URL 발급 → (2) [rawClient]로 S3에 바이트 직접 PUT
 * (앱 토큰/JSON 협상 없이 — 서명 훼손 방지) → (3) POST / 로 등록. [ResourceDraft.bytes]는 업로드 화면이
 * 문서 피커에서 읽어 실은 실제 파일 바이트.
 */
class RemoteResourceRepository(private val api: ApiClient) : ResourceRepository {

    /** presigned PUT 전용 — 인증/베이스URL/ContentNegotiation 없는 순수 클라이언트. */
    private val rawClient by lazy { HttpClient() }

    override fun observeResources(folder: ResourceFolder?): Flow<List<ResourceFile>> =
        reactiveFlow(emptyList()) {
            api.getData<List<ResourceResponseDto>>(
                ApiRoutes.Resources.ROOT,
                mapOf("folder" to folder?.name),
            ).getOrNull()?.toDomainList() ?: emptyList()
        }

    override fun observeResourceDetail(resourceId: Long): Flow<ResourceFile?> =
        reactiveFlow<ResourceFile?>(null) {
            api.getData<ResourceResponseDto>(ApiRoutes.Resources.detail(resourceId))
                .getOrNull()?.toDomain(orderKey = 0L)
        }

    override fun observeStorage(): Flow<StorageUsage> = reactiveFlow(StorageUsage()) {
        val usage = api.getData<StorageUsageResponseDto>(ApiRoutes.Resources.STORAGE).getOrNull()
        // count는 서버 필드가 없어 전체 목록 크기에서 파생.
        val count = api.getData<List<ResourceResponseDto>>(ApiRoutes.Resources.ROOT).getOrNull()?.size ?: 0
        usage?.toDomain(count) ?: StorageUsage(count = count)
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
        ).map { it.id }.also { RemoteBus.invalidate() }
    }

    override suspend fun deleteResource(resourceId: Long): DataResult<Unit> =
        api.deleteUnit(ApiRoutes.Resources.detail(resourceId)).also { RemoteBus.invalidate() }

    override suspend fun incrementDownload(resourceId: Long): DataResult<Unit> {
        // 전용 증가 엔드포인트가 없어 download-url 호출(서버가 카운트 증가) 후 URL은 폐기.
        val result = api.getData<DownloadUrlResponseDto>(ApiRoutes.Resources.downloadUrl(resourceId))
        RemoteBus.invalidate()
        return result.map { }
    }
}
