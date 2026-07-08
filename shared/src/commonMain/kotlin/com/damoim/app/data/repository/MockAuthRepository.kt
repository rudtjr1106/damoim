package com.damoim.app.data.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.data.mock.MockData
import com.damoim.app.domain.model.AuthUser
import com.damoim.app.domain.repository.AuthRepository
import kotlinx.coroutines.delay

/**
 * [AuthRepository]의 Mock 구현. 네트워크 지연을 흉내 내기 위해 delay를 준다.
 * 서버가 붙으면 이 클래스를 Ktor 기반 구현으로 교체한다(인터페이스는 그대로).
 */
class MockAuthRepository : AuthRepository {

    override suspend fun loginWithKakao(): DataResult<AuthUser> {
        delay(NETWORK_DELAY_MS)
        return DataResult.Success(MockData.kakaoUser)
    }

    override suspend fun updateProfile(
        nickname: String,
        contact: String,
        profileImageUrl: String?,
    ): DataResult<AuthUser> {
        delay(NETWORK_DELAY_MS)
        return DataResult.Success(
            MockData.kakaoUser.copy(
                nickname = nickname,
                contact = contact,
                profileImageUrl = profileImageUrl,
                needsProfileSetup = false,
            ),
        )
    }

    private companion object {
        const val NETWORK_DELAY_MS = 600L
    }
}
