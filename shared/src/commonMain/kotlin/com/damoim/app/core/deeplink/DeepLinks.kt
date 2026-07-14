package com.damoim.app.core.deeplink

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 플랫폼(Android intent 등)이 수신한 딥링크를 공통 코드로 전달하는 홀더.
 * StateFlow라 Main 플로우가 뜨기 전(콜드스타트/로그인 중)에 수신해도 보류됐다가
 * MainNavHost가 소비한다. 공유 링크: https://damoim.app/post/{id} · damoim://post/{id}
 */
object DeepLinks {
    private val _pendingPostId = MutableStateFlow<Long?>(null)
    val pendingPostId: StateFlow<Long?> = _pendingPostId

    fun openPost(id: Long) { _pendingPostId.value = id }
    fun consumePost() { _pendingPostId.value = null }
}
