package com.damoim.app.data.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.data.mock.MockStore
import com.damoim.app.domain.model.AuthUser
import com.damoim.app.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/**
 * [AuthRepository]의 Mock 구현 — [MockStore]에 위임. 카카오 SDK/서버 도입 시 교체.
 * 프로필 설정 값이 세션 동안 유지되어 댓글 작성자·홈 인사말 등에 반영된다.
 */
class MockAuthRepository : AuthRepository {

    override fun observeUser(): Flow<AuthUser> = MockStore.user

    override suspend fun loginWithKakao(): DataResult<AuthUser> {
        delay(NETWORK_DELAY_MS)
        return DataResult.Success(MockStore.login())
    }

    override suspend fun updateProfile(
        nickname: String,
        contact: String,
        profileImageUrl: String?,
    ): DataResult<AuthUser> {
        delay(NETWORK_DELAY_MS)
        return DataResult.Success(MockStore.updateProfile(nickname, contact, profileImageUrl))
    }

    private companion object {
        const val NETWORK_DELAY_MS = 600L
    }
}
