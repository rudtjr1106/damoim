package com.damoim.app.data.remote.notification

import com.damoim.app.domain.model.AppNotification
import com.damoim.app.domain.model.NotificationType
import kotlinx.serialization.Serializable

/** 알림(37) DTO. 서버 notification/NotificationDtos.kt와 1:1. */
@Serializable
data class NotificationResponseDto(
    val id: Long,
    val type: String,
    val text: String,
    val timeAgo: String = "",
    val isUnread: Boolean = false,
)

internal fun notifType(s: String): NotificationType =
    runCatching { NotificationType.valueOf(s) }.getOrDefault(NotificationType.NOTICE)

internal fun NotificationResponseDto.toDomain(): AppNotification = AppNotification(
    id = id,
    type = notifType(type),
    text = text,
    timeAgo = timeAgo,
    isUnread = isUnread,
)
