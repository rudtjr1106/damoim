package com.damoim.app.core.social

/**
 * 소셜 로그인 브릿지. 플랫폼 앱 모듈(androidApp·iosApp)이 실제 SDK 구현을 [SocialLogin.provider]에
 * 주입한다 — 카카오 SDK 의존성이 shared에 들어오지 않도록 분리.
 *
 * provider가 없거나 미설정(키 없음)이면 로그인은 KAKAO_NOT_CONFIGURED 실패로 끝난다
 * (Mock 폴백은 서버 연결 전환 때 제거됐다 — RemoteAuthRepository.loginWithKakao 참고).
 */
data class SocialUser(
    val id: Long,
    val nickname: String,
    val email: String?,
    val profileImageUrl: String?,
    /** 소셜 access token — 서버 로그인(POST /api/auth/kakao)에 필요. Mock 경로에선 미사용. */
    val accessToken: String? = null,
)

interface SocialLoginProvider {
    /** 네이티브 앱 키가 주입되어 실제 로그인이 가능한 상태인지. */
    val isConfigured: Boolean

    /** 로그인 수행. 취소/실패 시 null. */
    suspend fun login(): SocialUser?
}

object SocialLogin {
    var provider: SocialLoginProvider? = null
}
