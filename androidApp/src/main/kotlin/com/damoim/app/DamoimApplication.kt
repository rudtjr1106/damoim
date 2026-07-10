package com.damoim.app

import android.app.Application
import com.kakao.sdk.common.KakaoSdk

/** 앱 초기화 — 카카오 SDK(키가 주입된 경우에만). */
class DamoimApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isNotBlank()) {
            KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        }
    }
}
