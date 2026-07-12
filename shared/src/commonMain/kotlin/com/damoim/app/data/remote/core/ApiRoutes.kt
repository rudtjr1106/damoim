package com.damoim.app.data.remote.core

/**
 * 서버 REST 경로 단일 출처. 하드코딩 문자열 대신 여기 상수/헬퍼를 참조한다.
 * 정적 경로는 `const val`, 경로 변수가 있는 것은 상수 기반 헬퍼 함수로 조립한다.
 */
object ApiRoutes {

    object Auth {
        const val KAKAO = "/api/auth/kakao"
        const val REFRESH = "/api/auth/refresh"
        const val LOGOUT = "/api/auth/logout"
    }

    object Me {
        const val ROOT = "/api/me"
        const val PROFILE = "/api/me/profile"
        const val NOTIFICATIONS = "/api/me/notifications"
        const val NOTIFICATIONS_READ_ALL = "/api/me/notifications/read-all"
        const val NOTIFICATION_SETTINGS = "/api/me/notification-settings"
    }

    object Clubs {
        const val ROOT = "/api/clubs"
        const val JOIN = "/api/clubs/join"
        const val ME = "/api/clubs/me"
        const val ME_HOME = "/api/clubs/me/home"
        const val ME_COHORTS = "/api/clubs/me/cohorts"
        const val ME_APPLICANTS = "/api/clubs/me/applicants"
        const val ME_LEAVE = "/api/clubs/me/leave"
        const val JOIN_CODE_REGENERATE = "/api/clubs/me/join-code/regenerate"
        const val JOIN_CODE_DISABLE = "/api/clubs/me/join-code/disable"
        const val JOINED = "/api/clubs/joined"
        const val SWITCH = "/api/clubs/switch"
        fun cohort(cohortId: Long) = "$ME_COHORTS/$cohortId"
        fun decide(applicationId: Long) = "$ME_APPLICANTS/$applicationId/decide"
    }

    object Members {
        const val ROOT = "/api/members"
        const val ME = "/api/members/me"
        fun detail(memberId: Long) = "$ROOT/$memberId"
        fun cohort(memberId: Long) = "$ROOT/$memberId/cohort"
        fun role(memberId: Long) = "$ROOT/$memberId/role"
    }

    object Board {
        const val HOME = "/api/board/home"
        const val POSTS = "/api/board/posts"
        const val SEARCH = "/api/board/search"
        const val SEARCH_SUGGESTIONS = "/api/board/search/suggestions"
        const val SEARCH_RECENT = "/api/board/search/recent"
        const val DRAFT = "/api/board/draft"
        fun post(postId: Long) = "$POSTS/$postId"
        fun pin(postId: Long) = "$POSTS/$postId/pin"
        fun like(postId: Long) = "$POSTS/$postId/like"
        fun pollVote(postId: Long) = "$POSTS/$postId/poll/vote"
        fun recruitApply(postId: Long) = "$POSTS/$postId/recruit/apply"
        fun comments(postId: Long) = "$POSTS/$postId/comments"
    }

    object Resources {
        const val ROOT = "/api/resources"
        const val STORAGE = "/api/resources/storage"
        const val UPLOAD_URL = "/api/resources/upload-url"
        fun detail(resourceId: Long) = "$ROOT/$resourceId"
        fun downloadUrl(resourceId: Long) = "$ROOT/$resourceId/download-url"
    }

    object Schedules {
        const val ROOT = "/api/schedules"
        const val MY_APPLICATIONS = "/api/schedules/my-applications"
        fun detail(scheduleId: Long) = "$ROOT/$scheduleId"
        fun apply(scheduleId: Long) = "$ROOT/$scheduleId/apply"
        fun myApplication(scheduleId: Long) = "$ROOT/$scheduleId/my-application"
        fun close(scheduleId: Long) = "$ROOT/$scheduleId/close"
        fun calendarToggle(scheduleId: Long) = "$ROOT/$scheduleId/calendar/toggle"
        fun announce(scheduleId: Long) = "$ROOT/$scheduleId/announce"
    }

    object Subscription {
        const val ROOT = "/api/subscription"
        const val PLANS = "/api/subscription/plans"
        const val SUBSCRIBE = "/api/subscription/subscribe"
        const val CANCEL = "/api/subscription/cancel"
    }

    object Admins {
        const val ROOT = "/api/admins"
        const val ASSIGNABLE = "/api/admins/assignable"
        fun permissionsToggle(userId: Long) = "$ROOT/$userId/permissions/toggle"
        fun title(userId: Long) = "$ROOT/$userId/title"
        fun admin(userId: Long) = "$ROOT/$userId"
    }

    object Blocked {
        const val ROOT = "/api/blocked"
        fun byId(blockedId: Long) = "$ROOT/$blockedId"
    }
}

/** 클라가 분기에 사용하는 서버 에러코드(머신 판독용). 서버 common과 계약 일치. */
object ErrorCodes {
    const val NETWORK = "NETWORK"
    const val UNAUTHORIZED = "UNAUTHORIZED"
    const val RECRUIT_CLOSED = "RECRUIT_CLOSED"
    const val ALREADY_APPLIED = "ALREADY_APPLIED"
    const val KAKAO_NOT_CONFIGURED = "KAKAO_NOT_CONFIGURED"
    const val KAKAO_CANCELLED = "KAKAO_CANCELLED"
    const val KAKAO_NO_TOKEN = "KAKAO_NO_TOKEN"
    const val UPLOAD_FAILED = "UPLOAD_FAILED"
}

/** 게시글 첨부 타입 문자열(서버 AttachmentInput.type 계약). */
object AttachmentTypes {
    const val IMAGE = "IMAGE"
    const val FILE_DOC = "FILE_DOC"
    const val LINK = "LINK"
}

/** 모집 방식 라벨(서버 Recruit.method 계약 문자열). */
object RecruitMethods {
    const val FIRST_COME = "선착순"
    const val APPROVAL = "승인제"
}
