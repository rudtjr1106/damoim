package com.damoim.app.domain.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.AuthUser

/**
 * 인증/프로필 레포지토리. 구현체는 data 계층에 있으며 현재는 Mock.
 */
interface AuthRepository {

    /** 카카오 로그인. 신규 사용자면 needsProfileSetup=true로 돌아온다. */
    suspend fun loginWithKakao(): DataResult<AuthUser>

    /** 프로필(이름·연락처·사진) 설정/수정. 화면 31. */
    suspend fun updateProfile(
        nickname: String,
        contact: String,
        profileImageUrl: String?,
    ): DataResult<AuthUser>
}
