package com.damoim.app.domain.model

/**
 * 동아리. 가입 코드로 조회되거나 신청 결과에 포함되고, 홈/설정에서 표시된다.
 *
 * @param description 한 줄~여러 줄 소개
 * @param joinCode 현재 가입 코드 (동아리장 설정/공유용)
 * @param emblemColor 엠블럼 배경색(ARGB Long)
 */
data class Club(
    val id: Long,
    val name: String,
    val category: String,
    val description: String,
    val memberCount: Int,
    val joinCode: String = "",
    val emblemColor: Long = 0xFF2F6DD3,
)
