package com.damoim.app.data.remote.core

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** 앱 전역 JSON 설정. 서버 계약이 클라보다 필드가 많을 수 있어 ignoreUnknownKeys 필수. */
val AppJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
    encodeDefaults = true
}

/** 응답 로그용 — JSON을 들여쓰기해 예쁘게 출력. 파싱 실패(비JSON)면 원문 그대로. */
private val PrettyJson: Json = Json { prettyPrint = true }

private fun prettyJson(raw: String): String =
    if (raw.isBlank()) raw
    else runCatching { PrettyJson.encodeToString(PrettyJson.parseToJsonElement(raw)) }.getOrDefault(raw)

/**
 * 서버 통합용 HttpClient. 엔진은 명시하지 않아 소스셋별로 올라온 엔진(Android=OkHttp, iOS=Darwin)이
 * 자동 선택된다. 4xx/5xx도 예외로 던지지 않고(expectSuccess=false) 에러 봉투를 파싱하도록 둔다.
 */
fun buildHttpClient(): HttpClient = HttpClient {
    expectSuccess = false

    install(ContentNegotiation) { json(AppJson) }

    install(DefaultRequest) {
        contentType(ContentType.Application.Json)
    }

    if (RemoteConfig.enableLogging) {
        // 요청(메서드+URL) + 응답(상태 + 예쁘게 정렬한 JSON 바디)만 깔끔하게 찍는다.
        // println → Android=logcat(System.out)·iOS=Xcode 콘솔. logcat에서 "[API]"로 필터.
        // ⚠️ 응답 바디엔 토큰·PII도 있으므로 릴리즈에선 RemoteConfig.enableLogging=false 로 끌 것.
        install(ResponseObserver) {
            onResponse { response ->
                val req = response.call.request
                val body = runCatching { response.bodyAsText() }.getOrDefault("")
                println("[API] → ${req.method.value} ${req.url}")
                println("[API] ← ${response.status.value}\n${prettyJson(body)}")
            }
        }
    }

    // JWT Bearer + 401 자동 리프레시(회전). loadTokens/refreshTokens는 RemoteEnv.tokenStore를 사용.
    install(Auth) {
        bearer {
            // 매 요청 프로액티브 첨부(로그인 등 토큰 없는 요청은 null → 헤더 없음).
            sendWithoutRequest { true }

            loadTokens {
                RemoteEnv.tokenStore.load()?.let { BearerTokens(it.accessToken, it.refreshToken) }
            }

            refreshTokens {
                val refresh = RemoteEnv.tokenStore.load()?.refreshToken
                if (refresh.isNullOrBlank()) {
                    RemoteEnv.tokenStore.clear()
                    return@refreshTokens null
                }
                val pair = runCatching {
                    client.post("${RemoteConfig.baseUrl}${ApiRoutes.Auth.REFRESH}") {
                        markAsRefreshTokenRequest()
                        contentType(ContentType.Application.Json)
                        setBody(RefreshRequestDto(refresh))
                    }.body<ApiEnvelope<TokenPairDto>>()
                }.getOrNull()

                val data = pair?.data
                if (pair?.success == true && data != null) {
                    val tokens = AuthTokens(data.accessToken, data.refreshToken)
                    RemoteEnv.tokenStore.save(tokens)
                    BearerTokens(tokens.accessToken, tokens.refreshToken)
                } else {
                    // 리프레시 실패(만료/재사용 탐지 폐기) → 토큰 폐기. 이후 요청은 401 → 재로그인 유도.
                    RemoteEnv.tokenStore.clear()
                    null
                }
            }
        }
    }
}
