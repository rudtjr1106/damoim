package com.damoim.app.domain.model

/** 가입 신청자 (화면 09 가입 신청 관리). */
data class JoinApplicant(
    val id: Long,
    val name: String,
    val initial: String,             // 아바타 이니셜
    val desiredGisu: String,         // "25기 희망"
    val appliedDate: String,         // "6.03 신청"
    val timeAgo: String,             // "방금 전"
    val message: String? = null,     // 신청 메시지 (없을 수 있음)
)
