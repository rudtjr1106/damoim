package com.damoim.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
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

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}