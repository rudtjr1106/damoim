package com.damoim.app.data.remote.core

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * 세션 만료 전역 신호. 서버가 토큰을 거부해 리프레시까지 실패(계정 삭제·DB 초기화·재사용 폐기 등)로
 * [RemoteEnv.tokenStore]가 폐기될 때 [notifyExpired]를 호출한다.
 *
 * 콜드스타트는 RootNavHost가 isLoggedIn 재확인으로 처리하고, **실행 중** 만료는 RootNavHost가 이
 * 신호를 관찰해 즉시 로그인 화면으로 되돌린다. replay=0 이라 로그인 성공 후 지난 만료 신호가 되살아나
 * 곧바로 로그아웃되는 일은 없다.
 */
object SessionEvents {
    private val _expired = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** 실행 중 세션 만료 이벤트. 구독자(RootNavHost)에게만 전달(과거 이벤트 replay 없음). */
    val expired: Flow<Unit> = _expired

    /** 토큰 폐기 시점에 호출 — 실행 중이면 로그인 화면으로 유도한다. */
    fun notifyExpired() {
        _expired.tryEmit(Unit)
    }
}
