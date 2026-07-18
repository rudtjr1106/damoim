package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.repository.NotificationRepository

/** 알림 단건 읽음(49) — 알림을 터치하면 그 알림만 읽음 처리, 홈 벨 배지에 반영. */
class MarkNotificationReadUseCase(private val notificationRepository: NotificationRepository) {
    suspend operator fun invoke(id: Long): DataResult<Unit> = notificationRepository.markRead(id)
}
