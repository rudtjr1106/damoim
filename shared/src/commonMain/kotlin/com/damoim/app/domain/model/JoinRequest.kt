package com.damoim.app.domain.model

/** 가입 신청 상태. 화면 04(완료)/38(거절)/홈 진입 분기의 기준. */
enum class JoinStatus {
    PENDING,   // 신청 완료, 승인 대기 (화면 04)
    APPROVED,  // 승인됨 → 홈 진입
    REJECTED,  // 거절됨 (화면 38)
}

/**
 * 가입 코드 제출 결과. 어떤 동아리에 신청했고 현재 상태가 무엇인지 담는다.
 *
 * @param rejectionReason 거절 시 안내 문구 (status == REJECTED일 때만 존재)
 */
data class JoinRequestResult(
    val club: Club,
    val status: JoinStatus,
    val rejectionReason: String? = null,
)
