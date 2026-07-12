package com.damoim.app.data.remote.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart

/** 데이터 도메인 — 무효화 범위 단위. 변경은 영향받은 도메인만 무효화해 관련 observe만 재조회한다. */
enum class DataTopic { CLUB, MEMBER, NOTIFICATION, BOARD, RESOURCE, SCHEDULE, SETTINGS }

/**
 * 도메인별 무효화 버스.
 *
 * REST에는 push가 없으므로 "변경 → 관련 화면 재조회" 시맨틱을 이 버스로 재현한다. 단, 전역이 아니라
 * **변경된 도메인(topic)만** 무효화한다 — 예: 댓글 작성은 BOARD만 무효화하므로 일정/알림/회원 화면은
 * 재조회하지 않는다(불필요한 API 호출 방지). observe는 [reactiveFlow]로 자기 topic만 구독한다.
 */
object RemoteBus {
    private val signal = MutableSharedFlow<Set<DataTopic>>(
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** 변경된 도메인만 무효화 → 해당 topic을 구독하는 observe만 재조회. */
    fun invalidate(vararg topics: DataTopic) {
        if (topics.isNotEmpty()) signal.tryEmit(topics.toSet())
    }

    /** 전 도메인 무효화(로그인·동아리 전환·탈퇴 등 전체 데이터가 바뀔 때만). */
    fun invalidateAll() {
        signal.tryEmit(DataTopic.entries.toSet())
    }

    internal fun signalFor(topics: Set<DataTopic>): Flow<Unit> =
        signal.filter { changed -> changed.any(topics::contains) }.map { }
}

/**
 * observe*() 계약 헬퍼. 구독 시작 시 1회 + 구독 [topics] 중 하나가 무효화될 때마다 [fetch] 재실행.
 * fetch 실패(네트워크/파싱)는 [fallback]으로 흡수 → collector 크래시 없음(취소는 전파).
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> reactiveFlow(vararg topics: DataTopic, fallback: T, fetch: suspend () -> T): Flow<T> =
    RemoteBus.signalFor(topics.toSet())
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
