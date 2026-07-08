package com.damoim.app.domain.model

/**
 * 인증된 사용자. 카카오 로그인 결과 + 프로필 설정으로 채워진다.
 *
 * @param needsProfileSetup 최초 가입 직후 true → 프로필 설정 화면(31)으로 유도
 */
data class AuthUser(
    val id: Long,
    val nickname: String,
    val email: String?,
    val profileImageUrl: String?,
    val needsProfileSetup: Boolean = false,
)
