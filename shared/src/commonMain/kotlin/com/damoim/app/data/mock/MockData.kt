package com.damoim.app.data.mock

import com.damoim.app.domain.model.AppNotification
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.model.Cohort
import com.damoim.app.domain.model.Member
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.domain.model.MemberStatus
import com.damoim.app.domain.model.JoinApplicant
import com.damoim.app.domain.model.JoinRequestResult
import com.damoim.app.domain.model.JoinStatus
import com.damoim.app.domain.model.NotificationType

/**
 * 정적 시드 데이터 모음. 가변 상태(현재 사용자·세션·게시글·신청자·알림)는 [MockStore]가
 * 단일 소스로 관리하고, 이 파일은 데모 동아리 입장 시 주입할 초기값만 제공한다.
 */
object MockData {

    /** 데모 동아리의 기수 (디자인 42 기수 변경 시트 기준). */
    val cohorts = listOf(
        Cohort(25, "2025학년 1기 (25기)", "25기", 12),
        Cohort(24, "2024학년 1기 (24기)", "24기", 15),
        Cohort(23, "2023학년 1기 (23기)", "23기", 11),
    )

    /** 데모 동아리 — 가입 코드로 입장하는 대상. */
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

    /** 동아리 전환(33)용 두 번째 동아리 — 나는 여기서 일반 회원. 전환하면 이 동아리 데이터로 갈아탄다. */
    val runnersClub = Club(
        id = 30L,
        name = "한강 러너스",
        category = "운동 · 러닝",
        description = "주말마다 한강을 달리는 러닝 크루",
        memberCount = 24,
        joinCode = "RUN7KM",
        emblemColor = 0xFF1F9D55,
    )

    /** 한강 러너스 기수. */
    val runnersCohorts = listOf(
        Cohort(31, "2025 시즌", "25시즌", 14),
        Cohort(32, "2024 시즌", "24시즌", 10),
    )

    /**
     * 데모 동아리 회원 명부(16/17/18) — 본인(isMe) 제외 고정 5명.
     * 본인은 세션 역할에 따라 [MockStore]가 붙인다(LEADER면 리더 슬롯을 본인이 차지).
     */
    fun demoMembersExceptMe(includeLeader: Boolean): List<Member> = listOfNotNull(
        if (includeLeader)
            Member(502, "김민준", "민준", 23, MemberRole.LEADER, email = "minjun@kakao.com", joinedLabel = "2022.03.02")
        else null,
        Member(503, "최유진", "유진", 23, MemberRole.STAFF, email = "yujin@kakao.com", joinedLabel = "2022.03.05"),
        Member(504, "박준혁", "준혁", 25, MemberRole.MEMBER, MemberStatus.DORMANT, "junhyuk@kakao.com", "2025.03.10"),
        Member(505, "정하늘", "하늘", 25, MemberRole.MEMBER, email = "haneul@kakao.com", joinedLabel = "2025.03.12"),
        Member(506, "강도윤", "도윤", 25, MemberRole.STAFF, email = "doyun@kakao.com", joinedLabel = "2024.09.15"),
    )

    /** 한강 러너스 회원 명부(본인 제외) — 나는 일반 회원, 리더는 따로. */
    fun runnersMembersExceptMe(): List<Member> = listOf(
        Member(602, "오세훈", "세훈", 31, MemberRole.LEADER, email = "sehun@kakao.com", joinedLabel = "2024.01.06"),
        Member(603, "임지연", "지연", 31, MemberRole.STAFF, email = "jiyeon@kakao.com", joinedLabel = "2024.02.11"),
        Member(604, "한도훈", "도훈", 32, MemberRole.MEMBER, email = "dohoon@kakao.com", joinedLabel = "2024.05.20"),
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

    // ── 09 가입 신청자 시드 ──
    internal val applicants = listOf(
        JoinApplicant(1, "김준호", "준호", "25기 희망", "6.03 신청", "방금 전", "\"백엔드 공부하고 있는 신입생입니다. 열심히 활동하겠습니다!\""),
        JoinApplicant(2, "박지우", "지우", "25기 희망", "6.02 신청", "1일 전"),
        JoinApplicant(3, "이민아", "민아", "25기 희망", "6.01 신청", "2일 전"),
    )

    // ── 09 처리 완료 시드 (12건: 승인 10 · 거절 2) ──
    internal val processedApplicants: List<com.damoim.app.domain.model.ProcessedApplicant> = run {
        val entries = listOf(
            Triple("정우성", "우성", true), Triple("한소희", "소희", true), Triple("이도현", "도현", true),
            Triple("김세정", "세정", true), Triple("박보검", "보검", false), Triple("최수빈", "수빈", true),
            Triple("장원영", "원영", true), Triple("서강준", "강준", true), Triple("문가영", "가영", false),
            Triple("남주혁", "주혁", true), Triple("김태리", "태리", true), Triple("이준호", "준호", true),
        )
        entries.mapIndexed { i, (name, initial, approved) ->
            com.damoim.app.domain.model.ProcessedApplicant(
                applicant = JoinApplicant(
                    id = 100L + i, name = name, initial = initial,
                    desiredGisu = "25기 희망", appliedDate = "5.${28 - i} 신청", timeAgo = "",
                ),
                approved = approved,
                decidedLabel = "5.${29 - i} 처리",
            )
        }
    }

    // ── 37 알림 시드 ──
    internal val notifications = listOf(
        AppNotification(1, NotificationType.JOIN_APPROVED, "가입 신청이 승인되었어요. 코딩하는 사람들에 오신 것을 환영합니다! 🎉", "방금 전", isUnread = true),
        AppNotification(2, NotificationType.NOTICE, "새 공지: 신입 회원 환영 OT 일정 안내", "1시간 전", isUnread = true),
        AppNotification(3, NotificationType.COMMENT, "박준혁님이 회원님의 글에 댓글을 남겼어요: \"혹시 온라인 참여도...\"", "어제", isUnread = false),
        AppNotification(4, NotificationType.SCHEDULE, "정기 월례회의가 내일 오전 10시에 시작돼요", "어제", isUnread = false),
        AppNotification(5, NotificationType.VOTE, "MT 날짜 투표가 곧 마감돼요 (D-2)", "2일 전", isUnread = false),
    )

    // ── 85 추천 검색어 ──
    internal val recommendedKeywords = listOf("MT 후기", "정기 모임", "회비 납부", "스터디", "자료실", "가입 코드")
}
