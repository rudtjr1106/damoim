package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.AppNotification
import com.damoim.app.domain.repository.NotificationRepository

/** 알림 목록 (화면 37/74). */
class GetNotificationsUseCase(private val notificationRepository: NotificationRepository) {
    suspend operator fun invoke(): DataResult<List<AppNotification>> = notificationRepository.getNotifications()
}
