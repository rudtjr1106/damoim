package com.damoim.app.data.remote.auth

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.core.result.map
import com.damoim.app.core.result.onSuccess
import com.damoim.app.core.social.SocialLogin
import com.damoim.app.data.remote.core.ApiClient
import com.damoim.app.data.remote.core.ApiRoutes
import com.damoim.app.data.remote.core.AuthTokens
import com.damoim.app.data.remote.core.DataTopic
import com.damoim.app.data.remote.core.ErrorCodes
import com.damoim.app.data.remote.core.RawHttp
import com.damoim.app.data.remote.core.RefreshRequestDto
import com.damoim.app.data.remote.core.RemoteBus
import com.damoim.app.data.remote.core.RemoteEnv
import com.damoim.app.domain.model.AuthUser
import com.damoim.app.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

/**
 * [AuthRepository]의 서버 구현.
 *
 * - 카카오 로그인: 네이티브 provider가 받은 카카오 access token을 서버로 전달(POST /api/auth/kakao) →
 *   서버가 카카오 재검증 후 자체 JWT 발급 → 토큰 저장.
 * - observeUser: 로컬 [userState] 캐시 + 구독 시 /api/me 동기화(로그인/프로필수정이 즉시 반영).
 */
class RemoteAuthRepository(private val api: ApiClient) : AuthRepository {

    private val userState = MutableStateFlow<AuthUser?>(null)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // 여러 화면이 observeUser를 구독해도 /api/me 동기화는 1회로 공유(중복 호출 방지).
    private val userFlow: Flow<AuthUser> by lazy {
        flow {
            runCatching { refreshMe() } // 구독 시작 시 서버 최신 프로필 동기화(토큰 있을 때만)
            emitAll(userState.map { it ?: GUEST })
        }.shareIn(scope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
    }

    override fun observeUser(): Flow<AuthUser> = userFlow

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
                RemoteBus.invalidateAll() // 로그인 → 전 도메인 데이터 준비
                user
            }
    }

    override suspend fun updateProfile(
        nickname: String,
        contact: String,
        profileImageUrl: String?,
        profileImageKey: String?,
    ): DataResult<AuthUser> {
        // 서버 검증: contact는 숫자 10~11자리(하이픈 X). 표시용 하이픈을 제거해 숫자만 전송.
        // 외부 URL은 http(s)만, 앱 업로드 사진은 profileImageKey(S3 키)로 전송.
        val body = UpdateProfileRequestDto(
            nickname = nickname,
            contact = contact.filter { it.isDigit() }.ifBlank { null },
            profileImageUrl = profileImageUrl?.takeIf {
                it.startsWith("http://") || it.startsWith("https://")
            },
            profileImageKey = profileImageKey?.takeIf { it.isNotBlank() },
        )
        return api.patchData<UserResponseDto>(ApiRoutes.Me.PROFILE, body).map { dto ->
            val user = dto.toDomain()
            setUser(user)
            // 이름/사진이 회원 명부·홈 인사말에 반영되므로 MEMBER·CLUB 갱신(observeUser는 즉시 반영됨).
            RemoteBus.invalidate(DataTopic.MEMBER, DataTopic.CLUB)
            user
        }
    }

    override suspend fun uploadProfileImage(bytes: ByteArray, contentType: String?): DataResult<String> {
        // 1) 업로드 URL 발급(상한 검증) → 2) S3에 직접 PUT → storageKey 반환.
        val presign = api.postData<ProfileImageUploadResponseDto>(
            ApiRoutes.Me.PROFILE_IMAGE,
            ProfileImageUploadRequestDto(contentType = contentType, sizeBytes = bytes.size.toLong()),
        )
        val upload = when (presign) {
            is DataResult.Success -> presign.data
            is DataResult.Failure -> return presign
        }
        return if (RawHttp.put(upload.uploadUrl, bytes, contentType)) {
            DataResult.Success(upload.storageKey)
        } else {
            DataResult.Failure(DataError(ErrorCodes.UPLOAD_FAILED, "사진 업로드에 실패했어요"))
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
        RemoteBus.invalidateAll() // 세션 종료 → 전 도메인 무효화
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
