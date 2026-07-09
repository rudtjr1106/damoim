package com.damoim.app.domain.model

/** 알림 (화면 37). type으로 아이콘을 고르고, isUnread면 강조 배경 + 점. */
data class AppNotification(
    val id: Long,
    val type: NotificationType,
    val text: String,
    val timeAgo: String,
    val isUnread: Boolean,
)

enum class NotificationType {
    JOIN_APPROVED,   // 가입 승인 (person+)
    NOTICE,          // 공지 (megaphone)
    COMMENT,         // 댓글 (kakao/말풍선)
    SCHEDULE,        // 일정 (calendar)
    VOTE,            // 투표 (chart)
}
