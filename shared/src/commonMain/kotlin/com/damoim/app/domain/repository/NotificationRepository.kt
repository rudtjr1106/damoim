package com.damoim.app.domain.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.AppNotification

/** 알림 레포지토리 (화면 37/74). 구현체는 data 계층, 현재 Mock. */
interface NotificationRepository {
    suspend fun getNotifications(): DataResult<List<AppNotification>>
}
