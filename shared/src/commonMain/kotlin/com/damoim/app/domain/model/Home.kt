package com.damoim.app.domain.model

/** 홈 화면(05/06) 요약 데이터. role에 따라 통계·알림·퀵액션 구성이 달라진다. */
data class HomeSummary(
    val role: ClubRole,
    val clubName: String,
    val memberName: String,          // 일반회원 인사말용 ("안녕하세요, 서연님")
    val stats: List<HomeStat>,       // 헤더 통계 3개
    val alert: HomeAlert?,           // 헤더 아래 겹치는 알림 카드 (없으면 숨김 — 예: 신청 0건)
    val schedules: List<UpcomingSchedule>,
    val boardPreviews: List<BoardPreview>,
    val hasUnreadNotification: Boolean,
)

/** 헤더 통계 (숫자 + 라벨). 예: 38 회원 / 24기 내 기수 */
data class HomeStat(val value: String, val label: String)

/** 홈 알림 카드. 동아리장=가입 신청(탭→09), 일반회원=다가오는 일정(D-badge). */
data class HomeAlert(
    val title: String,
    val subtitle: String,
    val kind: AlertKind,
    val badge: String? = null,       // "D-3" (SCHEDULE일 때)
)

enum class AlertKind { JOIN_REQUEST, SCHEDULE }

/** 다가오는 일정 카드 (캐러셀). primary=true면 진한 네이비 카드. [id]로 상세(24) 진입. */
data class UpcomingSchedule(
    val id: Long,                    // 일정 id (탭 → 24 상세)
    val dday: String,                // "D-3"
    val date: String,                // "6.07 토"
    val title: String,
    val subtitle: String,            // "오전 10:00 · 동아리방"
    val primary: Boolean,
)

/** 게시판 미리보기 행. [id]로 상세(14) 진입. */
data class BoardPreview(
    val id: Long,
    val category: BoardCategory,
    val title: String,
    val commentCount: Int,
)

enum class BoardCategory { NOTICE, FREE, RECRUIT }
