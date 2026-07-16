package com.damoim.app.data.remote.core

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.readRawBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

/**
 * presigned S3 URL 전용 순수 HTTP 클라이언트 — 앱 인증(Bearer)/JSON 협상/베이스URL을 붙이지 않는다.
 * S3에 바이트를 직접 PUT(업로드)하거나 GET(이미지 렌더)할 때 사용.
 * 엔진은 소스셋별 자동 선택(Android=OkHttp, iOS=Darwin).
 */
object RawHttp {
    private val client by lazy { HttpClient() }

    /** presigned PUT — 바이트를 S3에 직접 업로드. 성공 여부만 반환. */
    suspend fun put(url: String, bytes: ByteArray, contentType: String?): Boolean =
        runCatching {
            client.put(url) {
                setBody(bytes)
                contentType?.let { header(HttpHeaders.ContentType, it) }
            }.status.isSuccess()
        }.getOrDefault(false)

    /** presigned GET — 이미지 바이트를 내려받는다(렌더용). 실패 시 null. */
    suspend fun getBytes(url: String): ByteArray? =
        runCatching {
            // Ktor는 expectSuccess=false가 기본이라 404도 예외가 아니다 — 상태를 직접 봐야
            // 오류 본문을 이미지 바이트로 착각하지 않는다(키가 소실된 옛 이미지 = 404).
            val response = client.get(url)
            if (response.status.isSuccess()) response.readRawBytes() else null
        }.getOrNull()
}
