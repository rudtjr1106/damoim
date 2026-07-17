package com.damoim.app

import androidx.compose.ui.window.ComposeUIViewController
import com.damoim.app.core.social.SocialLogin
import com.damoim.app.data.remote.IosTokenStore
import com.damoim.app.data.remote.core.RemoteConfig
import com.damoim.app.data.remote.core.RemoteEnv
import com.damoim.app.platform.iosKakaoLoginProvider
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    // 서버 통합 설정 주입 — local.properties의 server.base.url이 단일 출처(안드로이드 BuildConfig와 동일).
    // ⚠️ iOS ATS가 cleartext http를 막으므로 그 값은 https 주소여야 한다.
    RemoteConfig.baseUrl = IosBuildConfig.SERVER_BASE_URL
    RemoteEnv.tokenStore = IosTokenStore()
    // 카카오 로그인 — iosApp(Swift)이 IosKakaoRegistry.impl을 등록했을 때만 실제 provider가 붙는다.
    // 미등록(= 네이티브 앱 키 없음)이면 provider가 비어 로그인이 KAKAO_NOT_CONFIGURED로 실패한다.
    iosKakaoLoginProvider()?.let { SocialLogin.provider = it }
    return ComposeUIViewController { App() }
}
