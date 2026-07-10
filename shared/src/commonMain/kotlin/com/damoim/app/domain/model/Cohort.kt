package com.damoim.app.domain.model

/**
 * 동아리 기수. (디자인 19 기수 관리 · 42 기수 변경 시트 기준)
 *
 * [label]은 목록/시트용 정식 표기("2024학년 1기 (24기)"), [short]는 요약 표기("24기").
 */
data class Cohort(
    val id: Long,
    val label: String,
    val short: String,
    val memberCount: Int,
)
