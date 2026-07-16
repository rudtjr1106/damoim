package com.damoim.app.data.remote.notification

import com.damoim.app.domain.model.AppNotification
import com.damoim.app.domain.model.NotificationTargetType
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
    val targetType: String? = null,   // "POST" | "SCHEDULE" | null
    val targetId: Long? = null,
)

internal fun notifType(s: String): NotificationType =
    runCatching { NotificationType.valueOf(s) }.getOrDefault(NotificationType.NOTICE)

/** 미지원/신규 targetType은 null(=이동 안 함)로 폴백. */
internal fun notifTargetType(s: String?): NotificationTargetType? =
    s?.let { runCatching { NotificationTargetType.valueOf(it) }.getOrNull() }

internal fun NotificationResponseDto.toDomain(): AppNotification = AppNotification(
    id = id,
    type = notifType(type),
    text = text,
    timeAgo = timeAgo,
    isUnread = isUnread,
    targetType = notifTargetType(targetType),
    targetId = targetId,
)
