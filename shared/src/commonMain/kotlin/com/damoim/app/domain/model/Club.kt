package com.damoim.app.domain.model

/**
 * 동아리. 가입 코드로 조회되거나 신청 결과에 포함된다.
 *
 * @param emblemColor 엠블럼 배경색(ARGB Long). 서버 연동 전 Mock에서 브랜드 팔레트 값을 넣는다.
 */
data class Club(
    val id: Long,
    val name: String,
    val category: String,
    val description: String,
    val memberCount: Int,
    val emblemColor: Long = 0xFF2F6DD3,
)
