package com.damoim.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.damoim.app.core.deeplink.DeepLinks
import com.damoim.app.core.social.SocialLogin
import com.damoim.app.data.remote.AndroidTokenStore
import com.damoim.app.data.remote.core.RemoteConfig
import com.damoim.app.data.remote.core.RemoteEnv

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 카카오 로그인 브릿지 주입 (키 없으면 isConfigured=false → Mock 폴백)
        SocialLogin.provider = KakaoLoginProvider(this)

        // 서버 통합 설정 주입 (local.properties → BuildConfig). setContent 전에 반드시 세팅.
        RemoteConfig.baseUrl = BuildConfig.SERVER_BASE_URL
        RemoteEnv.tokenStore = AndroidTokenStore(this)

        handleDeepLink(intent) // 콜드스타트 딥링크 — setContent 전에 보류값 세팅

        setContent {
            App()
        }
    }

    // singleTask라 앱 실행 중 링크 탭은 onNewIntent로 들어온다.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    /** https://damoim.app/post/{id} · damoim://post/{id} → 게시글 딥링크 보류값 세팅. */
    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        val postId = when {
            uri.scheme == "damoim" && uri.host == "post" ->
                uri.lastPathSegment?.toLongOrNull()
            uri.scheme == "https" && uri.host == "damoim.app" && uri.pathSegments.firstOrNull() == "post" ->
                uri.pathSegments.getOrNull(1)?.toLongOrNull()
            else -> null
        }
        if (postId != null) DeepLinks.openPost(postId)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}