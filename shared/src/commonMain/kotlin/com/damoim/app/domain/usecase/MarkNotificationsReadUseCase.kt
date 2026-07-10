package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.repository.NotificationRepository

/** 알림 모두 읽음 (화면 37). 홈 벨 배지에도 반영된다. */
class MarkNotificationsReadUseCase(private val notificationRepository: NotificationRepository) {
    suspend operator fun invoke(): DataResult<Unit> = notificationRepository.markAllRead()
}
