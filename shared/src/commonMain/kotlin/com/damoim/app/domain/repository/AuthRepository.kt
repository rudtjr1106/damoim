package com.damoim.app.domain.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

/**
 * 인증/프로필 레포지토리. 구현체는 data 계층에 있으며 현재는 Mock.
 */
interface AuthRepository {

    /** 현재 사용자(프로필 설정 반영). 댓글 작성자·내 글 판정 등에 사용. */
    fun observeUser(): Flow<AuthUser>

    /** 카카오 로그인. 신규 사용자면 needsProfileSetup=true로 돌아온다. */
    suspend fun loginWithKakao(): DataResult<AuthUser>

    /**
     * 프로필(이름·연락처·사진) 설정/수정. 화면 31/45.
     * [profileImageUrl]=외부 http(s) URL(카카오 등), [profileImageKey]=앱에서 S3에 올린 사진의 키.
     */
    suspend fun updateProfile(
        nickname: String,
        contact: String,
        profileImageUrl: String? = null,
        profileImageKey: String? = null,
    ): DataResult<AuthUser>

    /** 프로필 사진 바이트를 S3에 업로드하고 storageKey를 반환한다(이후 updateProfile에 전달). */
    suspend fun uploadProfileImage(bytes: ByteArray, contentType: String?): DataResult<String>

    /** 로그인 여부(저장된 토큰 존재). 콜드스타트 초기 라우팅 결정에 사용. */
    fun isLoggedIn(): Boolean

    /** 로그아웃 — 리프레시 토큰 폐기(서버) + 로컬 토큰/신원 정리. 성공 시 미로그인 상태로 복귀. */
    suspend fun logout(): DataResult<Unit>
}
