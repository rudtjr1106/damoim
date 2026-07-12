package com.damoim.app.data.remote.resource

import com.damoim.app.data.remote.core.RemoteEnv
import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.domain.model.ResourceFolder
import com.damoim.app.domain.model.ResourceVisibility
import com.damoim.app.domain.repository.StorageUsage
import kotlinx.serialization.Serializable

/** D 자료실 그룹 DTO. 서버 resource/ResourceDtos와 JSON 계약 1:1. */

// ── 요청 ──
@Serializable
data class UploadUrlRequestDto(
    val fileName: String,
    val contentType: String? = null,
    val sizeBytes: Long,
    val folder: String,
)

@Serializable
data class CreateResourceRequestDto(
    val title: String,
    val description: String = "",
    val folder: String,
    val visibility: String = "ALL_MEMBERS",
    val cohortIds: List<Long> = emptyList(),
    val fileName: String,
    val ext: String = "",
    val sizeBytes: Long,
    val storageKey: String,
    val pageCount: Int? = null,
)

// ── 응답 ──
@Serializable
data class UploadUrlResponseDto(
    val uploadUrl: String,
    val storageKey: String,
    val expiresInSeconds: Long = 600,
)

@Serializable
data class DownloadUrlResponseDto(val downloadUrl: String, val fileName: String)

@Serializable
data class ResourceResponseDto(
    val id: Long,
    val title: String,
    val fileName: String,
    val ext: String = "",
    val description: String = "",
    val folder: String,
    val sizeLabel: String = "",
    val sizeBytes: Long = 0,
    val uploaderName: String = "",
    val uploaderIsLeader: Boolean = false,
    val uploadedLabel: String = "",
    val downloadCount: Int = 0,
    val visibility: String = "ALL_MEMBERS",
    val cohortIds: List<Long> = emptyList(),
    val pageCount: Int? = null,
    val isMine: Boolean = false,
)

@Serializable
data class StorageUsageResponseDto(
    val usedBytes: Long = 0,
    val usedLabel: String = "0KB",
    val quotaBytes: Long = 5_368_709_120,
    val quotaLabel: String = "5GB",
    val percent: Int = 0,
)

// ── 매퍼 ──
internal fun resourceFolderOf(s: String): ResourceFolder =
    runCatching { ResourceFolder.valueOf(s) }.getOrDefault(ResourceFolder.DOCS)

internal fun resourceVisibilityOf(s: String): ResourceVisibility =
    runCatching { ResourceVisibility.valueOf(s) }.getOrDefault(ResourceVisibility.ALL_MEMBERS)

/** 라벨→바이트 역파싱(문서 피커가 바이트를 안 주는 경우 신고용 근사값 — S3는 HeadObject로 실측 교정). */
internal fun parseSizeLabelToBytes(label: String): Long {
    val t = label.trim().uppercase()
    val num = t.takeWhile { it.isDigit() || it == '.' }.toDoubleOrNull() ?: return 1L
    val bytes = when {
        t.endsWith("GB") -> num * 1_073_741_824.0
        t.endsWith("MB") -> num * 1_048_576.0
        t.endsWith("KB") -> num * 1024.0
        else -> num
    }
    return bytes.toLong().coerceAtLeast(1L)
}

/** [orderKey]는 서버 정렬(최신순)을 보존하기 위한 합성 내림차순 값. */
internal fun ResourceResponseDto.toDomain(orderKey: Long): ResourceFile = ResourceFile(
    id = id,
    title = title,
    fileName = fileName,
    ext = ext,
    description = description,
    folder = resourceFolderOf(folder),
    sizeLabel = sizeLabel,
    sizeBytes = sizeBytes,
    uploaderId = if (isMine) RemoteEnv.currentUserId else 0L, // 서버가 uploaderId 미전송 → isMine으로 파생
    uploaderName = uploaderName,
    uploaderIsLeader = uploaderIsLeader,
    uploadedLabel = uploadedLabel,
    downloadCount = downloadCount,
    visibility = resourceVisibilityOf(visibility),
    cohortIds = cohortIds,
    pageCount = pageCount,
    createdAt = orderKey,
)

internal fun List<ResourceResponseDto>.toDomainList(): List<ResourceFile> =
    mapIndexed { index, dto -> dto.toDomain(orderKey = (size - index).toLong()) }

internal fun StorageUsageResponseDto.toDomain(count: Int): StorageUsage = StorageUsage(
    usedBytes = usedBytes,
    totalBytes = quotaBytes,
    usedLabel = usedLabel,
    totalLabel = quotaLabel,
    count = count,
)
