package com.damoim.app.data.remote.core

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.coroutines.CancellationException

/**
 * 공통 봉투({success,data,error})를 벗겨 [DataResult]로 반환하는 얇은 래퍼.
 *
 * - 4xx/5xx도 예외를 던지지 않고(HttpClient expectSuccess=false) 에러 봉투를 파싱해 error.code로 매핑.
 * - 네트워크/파싱 예외는 code="NETWORK"로 변환(취소는 전파).
 * - reified 제네릭이 필요해 데이터 응답 메서드는 inline. 호출부는 같은 모듈(data 계층)이라 internal.
 *
 * 요청 바디는 구체 @Serializable DTO만 전달할 것(제네릭 컬렉션 X — setBody가 런타임 클래스로 직렬화).
 */
class ApiClient(@PublishedApi internal val http: HttpClient) {

    @PublishedApi
    internal fun url(path: String): String = RemoteConfig.baseUrl + path

    // ── 데이터 응답(reified) ──

    suspend inline fun <reified T> getData(
        path: String,
        params: Map<String, String?> = emptyMap(),
    ): DataResult<T> = try {
        http.get(url(path)) { applyParams(params) }.body<ApiEnvelope<T>>().unwrap()
    } catch (c: CancellationException) {
        throw c
    } catch (e: Throwable) {
        DataResult.Failure(networkError(e))
    }

    suspend inline fun <reified T> postData(
        path: String,
        body: Any? = null,
        params: Map<String, String?> = emptyMap(),
    ): DataResult<T> = try {
        http.post(url(path)) { applyParams(params); if (body != null) setBody(body) }
            .body<ApiEnvelope<T>>().unwrap()
    } catch (c: CancellationException) {
        throw c
    } catch (e: Throwable) {
        DataResult.Failure(networkError(e))
    }

    suspend inline fun <reified T> patchData(
        path: String,
        body: Any? = null,
    ): DataResult<T> = try {
        http.patch(url(path)) { if (body != null) setBody(body) }
            .body<ApiEnvelope<T>>().unwrap()
    } catch (c: CancellationException) {
        throw c
    } catch (e: Throwable) {
        DataResult.Failure(networkError(e))
    }

    suspend inline fun <reified T> putData(
        path: String,
        body: Any? = null,
    ): DataResult<T> = try {
        http.put(url(path)) { if (body != null) setBody(body) }
            .body<ApiEnvelope<T>>().unwrap()
    } catch (c: CancellationException) {
        throw c
    } catch (e: Throwable) {
        DataResult.Failure(networkError(e))
    }

    // ── Unit 응답(데이터 없음) ──

    suspend fun postUnit(
        path: String,
        body: Any? = null,
        params: Map<String, String?> = emptyMap(),
    ): DataResult<Unit> = statusOnly {
        http.post(url(path)) { applyParams(params); if (body != null) setBody(body) }.body()
    }

    suspend fun patchUnit(path: String, body: Any? = null): DataResult<Unit> = statusOnly {
        http.patch(url(path)) { if (body != null) setBody(body) }.body()
    }

    suspend fun putUnit(path: String, body: Any? = null): DataResult<Unit> = statusOnly {
        http.put(url(path)) { if (body != null) setBody(body) }.body()
    }

    suspend fun deleteUnit(
        path: String,
        params: Map<String, String?> = emptyMap(),
    ): DataResult<Unit> = statusOnly {
        http.delete(url(path)) { applyParams(params) }.body()
    }

    private inline fun statusOnly(fetch: () -> ApiStatus): DataResult<Unit> = try {
        fetch().unwrapUnit()
    } catch (c: CancellationException) {
        throw c
    } catch (e: Throwable) {
        DataResult.Failure(networkError(e))
    }
}

@PublishedApi
internal fun HttpRequestBuilder.applyParams(params: Map<String, String?>) {
    params.forEach { (k, v) -> if (v != null) parameter(k, v) }
}

@PublishedApi
internal fun networkError(e: Throwable): DataError =
    DataError(code = "NETWORK", message = "네트워크 오류가 발생했어요. 연결을 확인해주세요.", cause = e)
