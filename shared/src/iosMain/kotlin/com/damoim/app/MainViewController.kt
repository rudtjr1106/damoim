package com.damoim.app

import androidx.compose.ui.window.ComposeUIViewController
import com.damoim.app.data.remote.IosTokenStore
import com.damoim.app.data.remote.core.RemoteConfig
import com.damoim.app.data.remote.core.RemoteEnv
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    // 서버 통합 설정 주입 — local.properties의 server.base.url이 단일 출처(안드로이드 BuildConfig와 동일).
    // ⚠️ iOS ATS가 cleartext http를 막으므로 그 값은 https 주소여야 한다.
    RemoteConfig.baseUrl = IosBuildConfig.SERVER_BASE_URL
    RemoteEnv.tokenStore = IosTokenStore()
    return ComposeUIViewController { App() }
}