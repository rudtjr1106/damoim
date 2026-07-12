package com.damoim.app.data.remote.core

/** access/refresh 토큰 쌍. */
data class AuthTokens(val accessToken: String, val refreshToken: String)

/**
 * 토큰 영속 저장소. 기본은 인메모리(세션 유지, 콜드스타트 시 재로그인).
 * 플랫폼 진입점에서 영속 구현(Android=SharedPreferences, iOS=NSUserDefaults)으로 교체한다.
 */
interface TokenStore {
    fun load(): AuthTokens?
    fun save(tokens: AuthTokens)
    fun clear()
}

class InMemoryTokenStore : TokenStore {
    private var tokens: AuthTokens? = null
    override fun load(): AuthTokens? = tokens
    override fun save(tokens: AuthTokens) { this.tokens = tokens }
    override fun clear() { tokens = null }
}
