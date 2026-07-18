package com.damoim.app.domain.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

/** 알림 레포지토리 (화면 37/74). 읽음 처리가 벨 배지 등에 실시간 반영된다. */
interface NotificationRepository {

    fun observeNotifications(): Flow<List<AppNotification>>

    /** 모두 읽음 처리(37). */
    suspend fun markAllRead(): DataResult<Unit>

    /** 단건 읽음 처리(49) — 알림을 터치했을 때 그 알림만. */
    suspend fun markRead(id: Long): DataResult<Unit>
}
