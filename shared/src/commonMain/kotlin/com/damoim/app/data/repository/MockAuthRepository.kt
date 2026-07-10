package com.damoim.app.data.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.core.social.SocialLogin
import com.damoim.app.data.mock.MockStore
import com.damoim.app.domain.model.AuthUser
import com.damoim.app.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/**
 * [AuthRepository]의 Mock 구현 — [MockStore]에 위임.
 * 카카오 로그인은 [SocialLogin.provider](androidApp이 주입, 키 필요)가 설정되어 있으면
 * 실제 SDK로 수행하고, 없으면 Mock 사용자로 폴백한다. 서버 도입 시 이 클래스만 교체.
 */
class MockAuthRepository : AuthRepository {

    override fun observeUser(): Flow<AuthUser> = MockStore.user

    override suspend fun loginWithKakao(): DataResult<AuthUser> {
        val provider = SocialLogin.provider
        if (provider != null && provider.isConfigured) {
            val social = runCatching { provider.login() }.getOrNull()
            if (social != null) return DataResult.Success(MockStore.loginWithSocial(social))
            // 실 로그인 취소/실패 → 로그인 화면 유지 대신 Mock 폴백하지 않고 실패 처리
            return DataResult.Failure(com.damoim.app.core.result.DataError(code = "KAKAO_CANCELLED", message = "카카오 로그인이 취소되었어요"))
        }
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
