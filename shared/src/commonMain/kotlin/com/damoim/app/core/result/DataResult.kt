package com.damoim.app.core.result

/**
 * 데이터 계층 결과 래퍼.
 *
 * UMC-Product의 `ApiState<T>`에 대응하는 CMP 버전. 서버가 붙기 전까지 Mock 레포지토리가
 * 이 타입으로 성공/실패를 표현하고, ViewModel은 [onSuccess]/[onFailure]로 분기한다.
 */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Failure(val error: DataError) : DataResult<Nothing>
}

/** 실패 정보. code는 추후 서버 에러코드(JWT 만료 등) 분기에 사용. */
data class DataError(
    val code: String = "",
    val message: String = "",
    val cause: Throwable? = null,
)

inline fun <T> DataResult<T>.onSuccess(block: (T) -> Unit): DataResult<T> {
    if (this is DataResult.Success) block(data)
    return this
}

inline fun <T> DataResult<T>.onFailure(block: (DataError) -> Unit): DataResult<T> {
    if (this is DataResult.Failure) block(error)
    return this
}

fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> = when (this) {
    is DataResult.Success -> DataResult.Success(transform(data))
    is DataResult.Failure -> this
}

fun <T> DataResult<T>.getOrNull(): T? = (this as? DataResult.Success)?.data
