package com.damoim.app.data.remote.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

/**
 * 리소스 flow 공유 캐시 — 같은 데이터를 여러 화면이 동시에 observe할 때 upstream fetch를 1회로 묶는다
 * (중복 API 호출 방지). key별로 [shareIn]된 flow를 캐시한다.
 *
 * - WhileSubscribed(5초): 마지막 구독 해제 후 5초 내 재구독은 재fetch 없이 캐시값 재사용(화면 왕복 시 유리).
 * - replay=1: 새 구독자는 마지막 값을 즉시 받는다(깜빡임 방지).
 *
 * observe*()는 매 호출 새 flow를 만들지 말고 반드시 이 캐시를 거쳐 같은 인스턴스를 반환해야 공유가 성립한다.
 */
class SharedFlows(private val scope: CoroutineScope) {
    private val cache = mutableMapOf<String, Flow<*>>()

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, create: () -> Flow<T>): Flow<T> =
        cache.getOrPut(key) {
            create().shareIn(scope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
        } as Flow<T>
}
