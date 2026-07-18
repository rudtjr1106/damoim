package com.damoim.app.domain.model

/** 신고 대상 유형 — 서버 post_reports는 게시글/댓글만 지원한다. */
enum class ReportTargetType { POST, COMMENT }

/** 34 내가 신고한 내역. [reason] 라벨은 DamoimStrings.reportReasonLabel로 매핑. */
data class MyReport(
    val id: Long,
    val targetType: ReportTargetType,
    val targetPreview: String,          // 게시글 제목 / 댓글 일부
    val reason: ReportReason,
    val reportedUserName: String,       // 피신고자(작성자)
    val reportedUserImageUrl: String? = null,
    val createdLabel: String,           // "2026.07.18"
)
