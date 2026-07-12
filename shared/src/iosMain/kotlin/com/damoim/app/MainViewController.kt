package com.damoim.app

import androidx.compose.ui.window.ComposeUIViewController
import com.damoim.app.data.remote.IosTokenStore
import com.damoim.app.data.remote.core.RemoteConfig
import com.damoim.app.data.remote.core.RemoteEnv
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    // 서버 통합 설정 주입 (iOS 시뮬레이터는 호스트와 네트워크 공유 → localhost).
    RemoteConfig.baseUrl = "http://localhost:8080"
    RemoteEnv.tokenStore = IosTokenStore()
    // AppGraph.useRemote는 기본 false — 서버 검증 시 여기서 true로. (iOS는 카카오 로그인 미구현이라 보류.)
    return ComposeUIViewController { App() }
}