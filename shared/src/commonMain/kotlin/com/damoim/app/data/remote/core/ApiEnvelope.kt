package com.damoim.app.data.remote.core

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import kotlinx.serialization.Serializable

/**
 * 서버 공통 응답 봉투 {success, data, error}. 서버의 `common/ApiResponse`와 1:1.
 * 성공/실패가 같은 모양이라 클라는 이 하나로 파싱한 뒤 [unwrap]으로 DataResult에 매핑한다.
 * data가 없는(Unit) 엔드포인트는 [ApiStatus]로 파싱한다(ignoreUnknownKeys로 data 무시).
 */
@Serializable
class ApiEnvelope<T>(
    val success: Boolean = false,
    val data: T? = null,
    val error: ApiErrorDto? = null,
)

@Serializable
class ApiStatus(
    val success: Boolean = false,
    val error: ApiErrorDto? = null,
)

@Serializable
class ApiErrorDto(
    val code: String = "UNKNOWN",
    val message: String = "",
)

/** 리프레시 응답에서 토큰쌍만 필요 — user/expiresIn 등은 ignoreUnknownKeys로 무시. */
@Serializable
class TokenPairDto(
    val accessToken: String,
    val refreshToken: String,
)

@Serializable
class RefreshRequestDto(
    val refreshToken: String,
)

/** data가 있는 응답 봉투 → DataResult. (public inline API에서 참조되어 @PublishedApi) */
@PublishedApi
internal fun <T> ApiEnvelope<T>.unwrap(): DataResult<T> = when {
    success && data != null -> DataResult.Success(data)
    success -> DataResult.Failure(DataError("EMPTY_BODY", "응답 데이터가 비어있어요"))
    else -> DataResult.Failure(DataError(error?.code ?: "UNKNOWN", error?.message ?: "요청을 처리하지 못했어요"))
}

/** data 없는(Unit) 응답 봉투 → DataResult<Unit>. */
@PublishedApi
internal fun ApiStatus.unwrapUnit(): DataResult<Unit> =
    if (success) DataResult.Success(Unit)
    else DataResult.Failure(DataError(error?.code ?: "UNKNOWN", error?.message ?: "요청을 처리하지 못했어요"))
