package com.damoim.app.data.remote.report

import com.damoim.app.domain.model.MyReport
import com.damoim.app.domain.model.ReportReason
import com.damoim.app.domain.model.ReportTargetType
import kotlinx.serialization.Serializable

@Serializable
data class SubmitReportRequestDto(
    val targetType: String,
    val targetId: Long,
    val reason: String,
    val detail: String? = null,
)

@Serializable
data class MyReportResponseDto(
    val id: Long,
    val targetType: String,
    val targetPreview: String,
    val reason: String,
    val reportedUserName: String,
    val reportedUserImageUrl: String? = null,
    val createdLabel: String,
)

/** 서버가 모르는 값을 보내도 안전하게 매핑(기본 POST/ETC). */
internal fun reportTargetTypeOf(raw: String): ReportTargetType =
    if (raw == "COMMENT") ReportTargetType.COMMENT else ReportTargetType.POST

internal fun reportReasonOf(raw: String): ReportReason =
    runCatching { ReportReason.valueOf(raw) }.getOrDefault(ReportReason.ETC)

internal fun MyReportResponseDto.toDomain() = MyReport(
    id = id,
    targetType = reportTargetTypeOf(targetType),
    targetPreview = targetPreview,
    reason = reportReasonOf(reason),
    reportedUserName = reportedUserName,
    reportedUserImageUrl = reportedUserImageUrl,
    createdLabel = createdLabel,
)
