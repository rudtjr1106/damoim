package com.damoim.app.data.mock

import com.damoim.app.domain.model.AlertKind
import com.damoim.app.domain.model.AppNotification
import com.damoim.app.domain.model.AuthUser
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.BoardPreview
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.HomeAlert
import com.damoim.app.domain.model.HomeStat
import com.damoim.app.domain.model.HomeSummary
import com.damoim.app.domain.model.JoinApplicant
import com.damoim.app.domain.model.JoinRequestResult
import com.damoim.app.domain.model.JoinStatus
import com.damoim.app.domain.model.NotificationType
import com.damoim.app.domain.model.UpcomingSchedule

/**
 * 서버 연동 전 임시 목 데이터 저장소. 실제 API/DataSource로 교체할 때 이 파일만 대체하면 된다.
 */
object MockData {

    val kakaoUser = AuthUser(
        id = 1001L,
        nickname = "서연",
        email = "seoyeon@kakao.com",
        profileImageUrl = null,
        needsProfileSetup = true,
    )

    val myClub = Club(
        id = 10L,
        name = "코딩하는 사람들",
        category = "IT/학술",
        description = "함께 성장하는 개발 동아리. 매주 스터디와 사이드 프로젝트를 진행합니다.",
        memberCount = 38,
        joinCode = "DM29AX",
        emblemColor = 0xFF2F6DD3,
    )

    private val bandClub = Club(
        id = 20L,
        name = "소리풍경 밴드",
        category = "음악 · 공연",
        description = "학기말 정기공연을 목표로 모이는 직장인 밴드 동아리",
        memberCount = 15,
        emblemColor = 0xFF68B7ED,
    )

    // ── 03 가입 코드 결과 ──
    fun joinResultForCode(code: String): JoinRequestResult? = when (code.uppercase()) {
        "REJECT" -> JoinRequestResult(
            club = bandClub,
            status = JoinStatus.REJECTED,
            rejectionReason = "이번 기수 모집이 마감되었어요. 다음 모집 때 다시 신청해주세요.",
        )
        "EXPIRE" -> null
        else -> JoinRequestResult(club = myClub, status = JoinStatus.PENDING)
    }

    // ── 05/06 홈 요약 ──
    private val schedules = listOf(
        UpcomingSchedule("D-3", "6.07 토", "정기 월례회의", "오전 10:00 · 동아리방", primary = true),
        UpcomingSchedule("D-10", "6.14 토", "신입 환영 MT", "1박 2일 · 가평", primary = false),
    )

    fun homeSummary(role: ClubRole): HomeSummary = when (role) {
        ClubRole.LEADER -> HomeSummary(
            role = ClubRole.LEADER,
            clubName = myClub.name,
            memberName = kakaoUser.nickname,
            stats = listOf(HomeStat("38", "회원"), HomeStat("3", "신청 대기"), HomeStat("2", "이번 주")),
            alert = HomeAlert("가입 신청 3건이 기다려요", "탭해서 승인/거절 처리", AlertKind.JOIN_REQUEST),
            schedules = schedules,
            boardPreviews = listOf(
                BoardPreview(BoardCategory.NOTICE, "신입 회원 환영 OT 일정 안내", 5),
                BoardPreview(BoardCategory.FREE, "동아리 MT 후기 공유해요", 12),
                BoardPreview(BoardCategory.RECRUIT, "2025 하반기 신입 부원 모집", 3),
            ),
            hasUnreadNotification = true,
        )
        ClubRole.MEMBER -> HomeSummary(
            role = ClubRole.MEMBER,
            clubName = myClub.name,
            memberName = kakaoUser.nickname,
            stats = listOf(HomeStat("24기", "내 기수"), HomeStat("2", "이번 주 일정"), HomeStat("7", "새 글")),
            alert = HomeAlert("정기 월례회의가 3일 남았어요", "6.07 토 오전 10:00 · 동아리방", AlertKind.SCHEDULE, badge = "D-3"),
            schedules = schedules,
            boardPreviews = listOf(
                BoardPreview(BoardCategory.NOTICE, "신입 회원 환영 OT 일정 안내", 5),
                BoardPreview(BoardCategory.FREE, "동아리 MT 후기 공유해요", 12),
            ),
            hasUnreadNotification = false,
        )
    }

    // ── 09 가입 신청자 ──
    val applicants = listOf(
        JoinApplicant(1, "김준호", "준호", "25기 희망", "6.03 신청", "방금 전", "\"백엔드 공부하고 있는 신입생입니다. 열심히 활동하겠습니다!\""),
        JoinApplicant(2, "박지우", "지우", "25기 희망", "6.02 신청", "1일 전"),
        JoinApplicant(3, "이민아", "민아", "25기 희망", "6.01 신청", "2일 전"),
    )

    // ── 37 알림 ──
    val notifications = listOf(
        AppNotification(1, NotificationType.JOIN_APPROVED, "가입 신청이 승인되었어요. 코딩하는 사람들에 오신 것을 환영합니다! 🎉", "방금 전", isUnread = true),
        AppNotification(2, NotificationType.NOTICE, "새 공지: 신입 회원 환영 OT 일정 안내", "1시간 전", isUnread = true),
        AppNotification(3, NotificationType.COMMENT, "박준혁님이 회원님의 글에 댓글을 남겼어요: \"혹시 온라인 참여도...\"", "어제", isUnread = false),
        AppNotification(4, NotificationType.SCHEDULE, "정기 월례회의가 내일 오전 10시에 시작돼요", "어제", isUnread = false),
        AppNotification(5, NotificationType.VOTE, "MT 날짜 투표가 곧 마감돼요 (D-2)", "2일 전", isUnread = false),
    )
}
