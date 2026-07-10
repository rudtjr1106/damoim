package com.damoim.app

import android.content.Context
import com.damoim.app.core.social.SocialLoginProvider
import com.damoim.app.core.social.SocialUser
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * 카카오 로그인 실제 구현 (Android). 네이티브 앱 키가 local.properties에 있을 때만 활성화되고,
 * 없으면 shared의 Mock 로그인으로 폴백된다.
 *
 * 흐름: 카카오톡 설치 시 카카오톡으로 → 취소 외 실패 시 카카오계정(웹)으로 재시도 → 프로필 조회.
 */
class KakaoLoginProvider(private val context: Context) : SocialLoginProvider {

    override val isConfigured: Boolean
        get() = BuildConfig.KAKAO_NATIVE_APP_KEY.isNotBlank()

    override suspend fun login(): SocialUser? {
        val token = loginForToken() ?: return null
        return fetchMe().also { if (it == null) return null }
    }

    private suspend fun loginForToken(): OAuthToken? = suspendCancellableCoroutine { cont ->
        val accountCallback: (OAuthToken?, Throwable?) -> Unit = { token, _ ->
            if (cont.isActive) cont.resume(token)
        }
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                when {
                    token != null -> if (cont.isActive) cont.resume(token)
                    // 사용자가 명시적으로 취소한 경우 외에는 카카오계정 로그인으로 폴백
                    error is ClientError && error.reason == ClientErrorCause.Cancelled ->
                        if (cont.isActive) cont.resume(null)
                    else -> UserApiClient.instance.loginWithKakaoAccount(context, callback = accountCallback)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = accountCallback)
        }
    }

    private suspend fun fetchMe(): SocialUser? = suspendCancellableCoroutine { cont ->
        UserApiClient.instance.me { user, _ ->
            val social = user?.let {
                SocialUser(
                    id = it.id ?: 0L,
                    nickname = it.kakaoAccount?.profile?.nickname.orEmpty(),
                    email = it.kakaoAccount?.email,
                    profileImageUrl = it.kakaoAccount?.profile?.profileImageUrl,
                )
            }
            if (cont.isActive) cont.resume(social)
        }
    }
}
