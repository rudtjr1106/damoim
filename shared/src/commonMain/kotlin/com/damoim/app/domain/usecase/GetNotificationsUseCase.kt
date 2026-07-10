package com.damoim.app.domain.usecase

import com.damoim.app.domain.model.AppNotification
import com.damoim.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

/** 알림 목록 관찰 (화면 37/74). */
class GetNotificationsUseCase(private val notificationRepository: NotificationRepository) {
    operator fun invoke(): Flow<List<AppNotification>> = notificationRepository.observeNotifications()
}
