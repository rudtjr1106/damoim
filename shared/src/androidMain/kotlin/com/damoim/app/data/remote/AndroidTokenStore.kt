package com.damoim.app.data.remote

import android.content.Context
import com.damoim.app.data.remote.core.AuthTokens
import com.damoim.app.data.remote.core.TokenStore

/**
 * Android 영속 토큰 저장소(SharedPreferences). MainActivity에서 RemoteEnv.tokenStore로 주입.
 * (출시 전 하드닝: EncryptedSharedPreferences/Keystore로 강화 검토.)
 */
class AndroidTokenStore(context: Context) : TokenStore {
    private val prefs = context.applicationContext
        .getSharedPreferences("damoim_auth", Context.MODE_PRIVATE)

    override fun load(): AuthTokens? {
        val access = prefs.getString(KEY_ACCESS, null) ?: return null
        val refresh = prefs.getString(KEY_REFRESH, null) ?: return null
        return AuthTokens(access, refresh)
    }

    override fun save(tokens: AuthTokens) {
        prefs.edit()
            .putString(KEY_ACCESS, tokens.accessToken)
            .putString(KEY_REFRESH, tokens.refreshToken)
            .apply()
    }

    override fun clear() {
        prefs.edit().remove(KEY_ACCESS).remove(KEY_REFRESH).apply()
    }

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
    }
}
