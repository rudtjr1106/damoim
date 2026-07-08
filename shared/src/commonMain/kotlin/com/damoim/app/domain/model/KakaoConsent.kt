package com.damoim.app.domain.model

/**
 * 카카오 로그인 동의 항목 (화면 02). 서버/SDK 연동 전까지 UI 표시용 정적 데이터.
 */
data class KakaoConsentItem(
    val label: String,
    val required: Boolean,
)

val DefaultKakaoConsentItems: List<KakaoConsentItem> = listOf(
    KakaoConsentItem(label = "프로필 정보 (닉네임·프로필 사진)", required = true),
    KakaoConsentItem(label = "카카오계정 이메일", required = false),
)
