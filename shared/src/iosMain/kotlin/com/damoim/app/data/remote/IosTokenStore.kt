package com.damoim.app.data.remote

import com.damoim.app.data.remote.core.AuthTokens
import com.damoim.app.data.remote.core.TokenStore
import platform.Foundation.NSUserDefaults

/**
 * iOS 영속 토큰 저장소(NSUserDefaults). MainViewController에서 RemoteEnv.tokenStore로 주입.
 * (출시 전 하드닝: Keychain으로 강화 검토.)
 */
class IosTokenStore : TokenStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun load(): AuthTokens? {
        val access = defaults.stringForKey(KEY_ACCESS) ?: return null
        val refresh = defaults.stringForKey(KEY_REFRESH) ?: return null
        return AuthTokens(access, refresh)
    }

    override fun save(tokens: AuthTokens) {
        defaults.setObject(tokens.accessToken, KEY_ACCESS)
        defaults.setObject(tokens.refreshToken, KEY_REFRESH)
    }

    override fun clear() {
        defaults.removeObjectForKey(KEY_ACCESS)
        defaults.removeObjectForKey(KEY_REFRESH)
    }

    private companion object {
        const val KEY_ACCESS = "damoim_access_token"
        const val KEY_REFRESH = "damoim_refresh_token"
    }
}
