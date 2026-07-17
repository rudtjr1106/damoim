package com.damoim.app.platform

import com.damoim.app.core.social.SocialLoginProvider
import com.damoim.app.core.social.SocialUser
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * 카카오 로그인(Swift) 구현 주입점. iosApp이 앱 시작 시 `IosKakaoRegistry.impl`을 등록하면 그걸 쓰고,
 * 없으면(= 네이티브 앱 키 미주입) provider가 null로 남아 로그인이 KAKAO_NOT_CONFIGURED 실패로 끝난다.
 * — 카카오 SDK 의존성이 shared에 들어오지 않도록 분리한 브릿지. [IosSubscriptionBilling]과 같은 구조.
 *
 * ⚠️ Swift가 Kotlin suspend 함수를 직접 구현하게 만들지 않는다 — 콜백으로 받고 여기서 suspend로 감싼다.
 */
interface IosKakaoLogin {
    /** 네이티브 앱 키가 Info.plist에 주입되어 실제 로그인이 가능한 상태인지. */
    val isConfigured: Boolean

    /** 로그인 수행. 취소/실패 시 null. 서버 로그인(POST /api/auth/kakao)용 accessToken을 반드시 채운다. */
    fun login(onResult: (SocialUser?) -> Unit)
}

object IosKakaoRegistry {
    var impl: IosKakaoLogin? = null
}

/**
 * [IosKakaoRegistry]에 등록된 Swift 구현을 commonMain 계약([SocialLoginProvider])으로 감싼 어댑터.
 * 매 호출마다 레지스트리를 다시 읽으므로 등록 시점에 의존하지 않는다.
 */
private class IosKakaoLoginProvider : SocialLoginProvider {

    override val isConfigured: Boolean
        get() = IosKakaoRegistry.impl?.isConfigured == true

    override suspend fun login(): SocialUser? {
        val impl = IosKakaoRegistry.impl ?: return null
        return suspendCancellableCoroutine { cont ->
            // ⚠️ 카카오 SDK 콜백은 1회 호출을 신뢰하지 않는다 — 두번째 호출은 조용히 무시한다.
            //    (이미 resume된 코루틴을 다시 resume하면 IllegalStateException으로 앱이 죽는다.)
            //    콜백은 모두 메인 스레드로 돌아오므로 별도 동기화는 불필요.
            var resumed = false
            impl.login { user ->
                if (!resumed && cont.isActive) {
                    resumed = true
                    cont.resume(user)
                }
            }
        }
    }
}

/**
 * MainViewController에서 [com.damoim.app.core.social.SocialLogin.provider]에 넣는다.
 * Swift 구현이 등록되지 않았으면 null — provider를 비워 둔다(로그인 시 KAKAO_NOT_CONFIGURED).
 */
internal fun iosKakaoLoginProvider(): SocialLoginProvider? =
    if (IosKakaoRegistry.impl != null) IosKakaoLoginProvider() else null
