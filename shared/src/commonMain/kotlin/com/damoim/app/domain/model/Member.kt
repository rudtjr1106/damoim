package com.damoim.app.domain.model

/**
 * 동아리 회원(16/17/18). [ClubRole](세션 역할 LEADER/MEMBER)과 별개로, 명부에는
 * 운영진(STAFF)이라는 중간 등급이 있어 [MemberRole]로 따로 표현한다. ClubRole은 건드리지 않는다.
 */
enum class MemberRole { LEADER, STAFF, MEMBER }   // 동아리장 / 운영진 / 일반 회원

enum class MemberStatus { ACTIVE, DORMANT }       // 활동 / 휴면

data class Member(
    val id: Long,
    val name: String,
    val initials: String,
    val cohortId: Long,                // Cohort.id — 항상 해석 가능한 FK
    val role: MemberRole,
    val status: MemberStatus = MemberStatus.ACTIVE,
    val joinedLabel: String = "",      // "2024.09.15" (사전 계산 문자열)
    val isMe: Boolean = false,
    val profileImageUrl: String? = null,  // 프로필 사진(없으면 이니셜 아바타)
)

/** 18 회원 상세 — 명부 회원 + 활동 요약. */
data class MemberDetail(
    val member: Member,
    val cohortLabel: String,           // "2024학년 1기 (24기)" — cohortId에서 해석
    val postCount: Int,
    val eventCount: Int,
    val lastActiveLabel: String,       // "1시간 전"
)
