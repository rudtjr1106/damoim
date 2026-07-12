package com.damoim.app.data.remote.auth

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.core.result.map
import com.damoim.app.core.result.onSuccess
import com.damoim.app.core.social.SocialLogin
import com.damoim.app.data.remote.core.ApiClient
import com.damoim.app.data.remote.core.ApiRoutes
import com.damoim.app.data.remote.core.AuthTokens
import com.damoim.app.data.remote.core.ErrorCodes
import com.damoim.app.data.remote.core.RefreshRequestDto
import com.damoim.app.data.remote.core.RemoteBus
import com.damoim.app.data.remote.core.RemoteEnv
import com.damoim.app.domain.model.AuthUser
import com.damoim.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * [AuthRepository]의 서버 구현.
 *
 * - 카카오 로그인: 네이티브 provider가 받은 카카오 access token을 서버로 전달(POST /api/auth/kakao) →
 *   서버가 카카오 재검증 후 자체 JWT 발급 → 토큰 저장.
 * - observeUser: 로컬 [userState] 캐시 + 구독 시 /api/me 동기화(로그인/프로필수정이 즉시 반영).
 */
class RemoteAuthRepository(private val api: ApiClient) : AuthRepository {

    private val userState = MutableStateFlow<AuthUser?>(null)

    override fun observeUser(): Flow<AuthUser> = flow {
        // 구독 시작 시 서버 최신 프로필 동기화(토큰 있을 때만).
        runCatching { refreshMe() }
        emitAll(userState.map { it ?: GUEST })
    }

    override suspend fun loginWithKakao(): DataResult<AuthUser> {
        val provider = SocialLogin.provider
        if (provider == null || !provider.isConfigured) {
            return DataResult.Failure(
                DataError(ErrorCodes.KAKAO_NOT_CONFIGURED, "카카오 로그인이 설정되지 않았어요"),
            )
        }
        val social = runCatching { provider.login() }.getOrNull()
            ?: return DataResult.Failure(DataError(ErrorCodes.KAKAO_CANCELLED, "카카오 로그인이 취소되었어요"))
        val kakaoToken = social.accessToken
            ?: return DataResult.Failure(DataError(ErrorCodes.KAKAO_NO_TOKEN, "카카오 토큰을 가져오지 못했어요"))

        return api.postData<TokenResponseDto>(ApiRoutes.Auth.KAKAO, KakaoLoginRequestDto(kakaoToken))
            .map { token ->
                RemoteEnv.tokenStore.save(AuthTokens(token.accessToken, token.refreshToken))
                val user = token.user.toDomain()
                setUser(user)
                RemoteBus.invalidate() // 로그인 → 전 화면 데이터 준비
                user
            }
    }

    override suspend fun updateProfile(
        nickname: String,
        contact: String,
        profileImageUrl: String?,
    ): DataResult<AuthUser> {
        // 서버 검증: contact는 숫자 10~11자리(하이픈 X). 표시용 하이픈을 제거해 숫자만 전송.
        // 이미지 URL은 http(s)만 — 로컬 URI는 전송하지 않는다.
        val body = UpdateProfileRequestDto(
            nickname = nickname,
            contact = contact.filter { it.isDigit() }.ifBlank { null },
            profileImageUrl = profileImageUrl?.takeIf {
                it.startsWith("http://") || it.startsWith("https://")
            },
        )
        return api.patchData<UserResponseDto>(ApiRoutes.Me.PROFILE, body).map { dto ->
            val user = dto.toDomain()
            setUser(user)
            RemoteBus.invalidate()
            user
        }
    }

    override fun isLoggedIn(): Boolean = RemoteEnv.tokenStore.load() != null

    override suspend fun logout(): DataResult<Unit> {
        val refresh = RemoteEnv.tokenStore.load()?.refreshToken
        // 로컬은 항상 즉시 정리(서버 폐기 실패해도 클라 세션 종료 보장).
        RemoteEnv.tokenStore.clear()
        RemoteEnv.currentUserId = 0L
        userState.value = null
        val result = if (refresh != null) {
            runCatching { api.postUnit(ApiRoutes.Auth.LOGOUT, RefreshRequestDto(refresh)) }
                .getOrDefault(DataResult.Success(Unit))
        } else {
            DataResult.Success(Unit)
        }
        RemoteBus.invalidate()
        return result
    }

    private suspend fun refreshMe() {
        if (RemoteEnv.tokenStore.load() == null) return // 미로그인
        api.getData<UserResponseDto>(ApiRoutes.Me.ROOT).onSuccess { setUser(it.toDomain()) }
    }

    private fun setUser(user: AuthUser) {
        userState.value = user
        RemoteEnv.currentUserId = user.id // 세션 신원(게시글 상세 authorId 파생 등)
    }

    private companion object {
        /** 미로그인 시 observeUser 기본값(라우팅은 RootNavHost가 별도로 Auth에서 시작). */
        val GUEST = AuthUser(id = 0, nickname = "", email = null, profileImageUrl = null)
    }
}
