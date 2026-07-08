package com.damoim.app.domain.model

/**
 * 인증된 사용자. 카카오 로그인 결과 + 프로필 설정으로 채워진다.
 *
 * @param contact 연락처(전화번호). 프로필 설정(31)에서 입력.
 * @param needsProfileSetup 최초 가입 직후 true → 프로필 설정 화면(31)으로 유도
 */
data class AuthUser(
    val id: Long,
    val nickname: String,
    val email: String?,
    val profileImageUrl: String?,
    val contact: String? = null,
    val needsProfileSetup: Boolean = false,
)
