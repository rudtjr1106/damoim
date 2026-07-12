package com.damoim.app.data.remote.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart

/**
 * 전역 무효화 버스.
 *
 * REST에는 서버 push가 없으므로, "어떤 변경이든 모든 화면에 자동 재방출"되는 리액티브 시맨틱을
 * 이 버스로 재현한다:
 * observe*() 플로우는 [reactiveFlow]로 이 신호를 구독하고, 변경(mutation) 성공 시 [invalidate]가
 * 활성 화면들의 재조회를 유발한다.
 */
object RemoteBus {
    val signal = MutableSharedFlow<Unit>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** 모든 활성 observe 플로우 재조회 트리거. 변경 성공 후 호출. */
    fun invalidate() {
        signal.tryEmit(Unit)
    }
}

/**
 * observe*() 계약 헬퍼. 구독 시작 시 즉시 1회, 이후 [RemoteBus] 신호마다 [fetch]를 재실행한다.
 * - fetch 실패(네트워크/파싱)는 [fallback]으로 흡수 → collector(ViewModel)가 크래시하지 않는다.
 * - 취소(CancellationException)는 그대로 전파(구조적 동시성 보존).
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> reactiveFlow(fallback: T, fetch: suspend () -> T): Flow<T> =
    RemoteBus.signal
        .onStart { emit(Unit) }
        .mapLatest {
            try {
                fetch()
            } catch (c: CancellationException) {
                throw c
            } catch (_: Throwable) {
                fallback
            }
        }
