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

    /** 프로필(이름·연락처·사진) 설정/수정. 화면 31. 저장된 값이 세션 동안 유지된다. */
    suspend fun updateProfile(
        nickname: String,
        contact: String,
        profileImageUrl: String?,
    ): DataResult<AuthUser>
}
