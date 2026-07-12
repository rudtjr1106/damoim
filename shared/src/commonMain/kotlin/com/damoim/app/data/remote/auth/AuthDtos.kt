package com.damoim.app.data.remote.auth

import com.damoim.app.domain.model.AuthUser
import kotlinx.serialization.Serializable

/**
 * A 인증 그룹 DTO. 서버 `auth/AuthDtos.kt`·`user/UserDtos.kt`와 JSON 계약 1:1.
 * 서버가 진실원이므로 필드명/타입을 정확히 맞춘다(ignoreUnknownKeys로 여유는 있음).
 */

/** POST /api/auth/kakao 요청 — 클라가 카카오 SDK로 받은 access token. */
@Serializable
data class KakaoLoginRequestDto(
    val accessToken: String,
)

/** POST /api/me/profile(PATCH) 요청. contact/profileImageUrl은 서버 검증(빈문자/숫자10~11, http(s)). */
@Serializable
data class UpdateProfileRequestDto(
    val nickname: String,
    val contact: String? = null,
    val profileImageUrl: String? = null,
)

/** 로그인/재발급 응답. accessToken=Bearer, refreshToken=안전저장. */
@Serializable
data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long = 0,
    val user: UserResponseDto,
)

/** 사용자 응답(로그인·/api/me 공용). */
@Serializable
data class UserResponseDto(
    val id: Long,
    val nickname: String,
    val email: String? = null,
    val profileImageUrl: String? = null,
    val contact: String? = null,
    val needsProfileSetup: Boolean = false,
)

fun UserResponseDto.toDomain(): AuthUser = AuthUser(
    id = id,
    nickname = nickname,
    email = email,
    profileImageUrl = profileImageUrl,
    contact = contact,
    needsProfileSetup = needsProfileSetup,
)
