package com.damoim.app.domain.model

/** 알림 (화면 37). type으로 아이콘을 고르고, isUnread면 강조 배경 + 점. target*이 있으면 탭 시 해당 화면으로 이동. */
data class AppNotification(
    val id: Long,
    val type: NotificationType,
    val text: String,
    val timeAgo: String,
    val isUnread: Boolean,
    val targetType: NotificationTargetType? = null,  // null = 이동 대상 없음(가입 승인 등)
    val targetId: Long? = null,                      // 대상 화면의 식별자
)

enum class NotificationType {
    JOIN_APPROVED,   // 가입 승인 (person+)
    NOTICE,          // 공지 (megaphone)
    COMMENT,         // 댓글 (kakao/말풍선)
    SCHEDULE,        // 일정 (calendar)
    VOTE,            // 투표 (chart)
}

/** 알림 탭 시 이동할 대상. 서버 targetType과 1:1(그 외/미지원 값은 null 처리). */
enum class NotificationTargetType {
    POST,       // 게시글 상세 14/36/84
    SCHEDULE,   // 일정 상세 24
}
