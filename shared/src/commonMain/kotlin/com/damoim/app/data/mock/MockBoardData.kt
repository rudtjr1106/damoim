package com.damoim.app.data.mock

import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.BoardPost
import com.damoim.app.domain.model.Comment
import com.damoim.app.domain.model.Poll
import com.damoim.app.domain.model.PollOption
import com.damoim.app.domain.model.PostAttachment
import com.damoim.app.domain.model.RecruitApplicant
import com.damoim.app.domain.model.RecruitInfo
import com.damoim.app.domain.model.RecruitStatus

/**
 * 데모 동아리("코딩하는 사람들") 게시판 시드 데이터. [MockStore.enterClub] 시 1회 주입되고,
 * 이후의 모든 변경(작성/댓글/좋아요/투표/신청)은 MockStore가 관리한다.
 *
 * createdAt은 최신순 정렬용 상대값(클수록 최신). 작성자 id: 김민준 2001 · 이서연 2002 ·
 * 박준혁 2003 · 최유진 2004 · 정하늘 2005 · 한지우 2006.
 */
internal object MockBoardData {

    fun seedPosts(): List<BoardPost> = listOf(
        // ── 공지 ──
        BoardPost(
            id = 101, category = BoardCategory.NOTICE,
            title = "신입 회원 환영 OT 일정 안내",
            content = "안녕하세요, 회장 김민준입니다.\n\n" +
                "6월 14일(토) 신입 회원 환영 OT를 진행합니다. 25기 신입 부원들은 꼭 참석해주시고, 기존 기수 분들도 많이 와주세요!\n\n" +
                "· 일시: 6월 14일(토) 오후 2시\n· 장소: 동아리방 (학생회관 302호)\n· 준비물: 없음",
            preview = "6월 14일 신입 환영 OT를 진행합니다. 참석 여부를 이벤트 페이지에서...",
            authorId = 2001, authorName = "김민준", authorInitials = "민준",
            timeLabel = "관리자 · 6.01", dateLabel = "2025.06.01", createdAt = 100,
            viewCount = 128, likeCount = 14, isPinned = true, isAuthorLeader = true, readRate = 82,
            attachments = listOf(PostAttachment.FileDoc("OT_일정표.pdf", "1.2MB")),
        ),
        BoardPost(
            id = 102, category = BoardCategory.NOTICE,
            title = "2025년 상반기 회비 납부 안내",
            content = "상반기 회비는 3만원이며 6월 10일까지 총무에게 전달해주세요.",
            preview = "상반기 회비는 3만원이며 6월 10일까지 총무에게 전달해주세요.",
            authorId = 2001, authorName = "김민준", authorInitials = "민준",
            timeLabel = "관리자 · 5.28", dateLabel = "2025.05.28", createdAt = 90,
            viewCount = 96,
        ),
        BoardPost(
            id = 103, category = BoardCategory.NOTICE,
            title = "동아리방 이용 수칙 변경",
            content = "동아리방 이용 수칙이 일부 변경되었습니다. 자세한 내용은 공지를 확인해주세요.",
            authorId = 2001, authorName = "김민준", authorInitials = "민준",
            timeLabel = "관리자 · 5.20", dateLabel = "2025.05.20", createdAt = 80,
            viewCount = 74,
        ),
        // ── 자유 ──
        BoardPost(
            id = 201, category = BoardCategory.FREE,
            title = "동아리 MT 후기 공유해요",
            content = "이번 MT 진짜 재밌었는데 다들 사진 올려주세요!\n\n" +
                "특히 둘째 날 게임할 때 찍은 사진들 단체방 말고 여기에 모아두면 좋을 것 같아요.",
            preview = "이번 MT 진짜 재밌었는데 다들 사진 올려주세요! 특히 둘째 날 게임할 때 찍은 사진들이요",
            authorId = 2002, authorName = "이서연", authorInitials = "서연", authorGisu = "24기",
            timeLabel = "10분 전", dateLabel = "2025.06.05", createdAt = 170,
            viewCount = 64, likeCount = 8, hasThumbnail = true,
            attachments = listOf(
                PostAttachment.Image("MT_photo_01.jpg"), PostAttachment.Image("MT_photo_02.jpg"),
                PostAttachment.Image("MT_photo_03.jpg"), PostAttachment.Image("MT_photo_04.jpg"),
                PostAttachment.Image("MT_photo_05.jpg"),
                PostAttachment.FileDoc("MT_준비물_체크리스트.xlsx", "24KB"),
            ),
        ),
        BoardPost(
            id = 202, category = BoardCategory.FREE,
            title = "이번 주 모임 시간 변경 가능한가요?",
            content = "목요일에 시험이 있어서 금요일로 옮길 수 있을지 궁금합니다",
            preview = "목요일에 시험이 있어서 금요일로 옮길 수 있을지 궁금합니다",
            authorId = 2003, authorName = "박준혁", authorInitials = "준혁", authorGisu = "26기",
            timeLabel = "어제", createdAt = 130,
        ),
        BoardPost(
            id = 203, category = BoardCategory.FREE,
            title = "신입 환영 파티 사진 올립니다",
            content = "지난 주 신입 환영 파티 사진 공유합니다!",
            authorId = 2004, authorName = "최유진", authorInitials = "유진", authorGisu = "23기",
            timeLabel = "2일 전", createdAt = 120,
        ),
        BoardPost(
            id = 204, category = BoardCategory.FREE,
            title = "스터디 자료 어디서 받나요?",
            content = "지난 스터디 자료 링크 좀 공유해주실 수 있나요?",
            authorId = 2005, authorName = "정하늘", authorInitials = "하늘", authorGisu = "25기",
            timeLabel = "3일 전", createdAt = 110,
        ),
        BoardPost(
            id = 205, category = BoardCategory.FREE,
            title = "MT 날짜 투표해주세요!",
            content = "둘 다 가능하신 분들은 앞 날짜로 부탁드려요. 화요일 저녁에 마감할게요!",
            preview = "MT 날짜 투표 진행 중이에요. 참여 부탁드려요!",
            authorId = 2001, authorName = "김민준", authorInitials = "민준",
            timeLabel = "1시간 전", dateLabel = "1시간 전", createdAt = 160,
            viewCount = 42, likeCount = 6, isAuthorLeader = true,
            poll = Poll(
                options = listOf(PollOption("6월 14일 ~ 15일 (토일)", votes = 15), PollOption("6월 21일 ~ 22일 (토일)", votes = 8)),
                anonymous = true, multiSelect = false, deadlineLabel = "마감 D-2",
            ),
        ),
        // ── 모집 ──
        BoardPost(
            id = 301, category = BoardCategory.RECRUIT,
            title = "2025 하반기 신입 부원 모집",
            content = "함께 성장할 신입 부원을 모집합니다. 백엔드/프론트엔드 관심자 모두 환영해요!\n\n" +
                "· 활동 기간: 2025년 하반기 (9월~12월)\n· 정기 모임: 매주 목요일 저녁 7시\n· 지원 자격: 열정 있는 분 누구나\n\n" +
                "신청 후 간단한 커피챗을 진행할 예정이에요. 궁금한 점은 댓글로 남겨주세요 :)",
            preview = "함께 성장할 신입 부원을 모집합니다. 백엔드/프론트 관심자 환영!",
            authorId = 2001, authorName = "김민준", authorInitials = "민준",
            timeLabel = "2시간 전", dateLabel = "2시간 전", createdAt = 150,
            viewCount = 86, isAuthorLeader = true,
            recruit = RecruitInfo(
                RecruitStatus.OPEN, dday = "D-7", current = 12, capacity = 20,
                deadlineLabel = "6.17 (화) 자정", method = "선착순",
                applicants = listOf(RecruitApplicant("서연", 0), RecruitApplicant("준혁", 1), RecruitApplicant("하늘", 2)),
            ),
        ),
        BoardPost(
            id = 302, category = BoardCategory.RECRUIT,
            title = "알고리즘 스터디 팀원 모집 (주 2회)",
            content = "매주 화/목 저녁 8시에 모여서 백준 골드 문제를 풉니다. 초보도 환영!",
            preview = "매주 화/목 저녁 8시에 모여서 백준 골드 문제를 풉니다. 초보도 환영!",
            authorId = 2004, authorName = "최유진", authorInitials = "유진",
            timeLabel = "어제", createdAt = 140,
            recruit = RecruitInfo(RecruitStatus.OPEN, dday = "D-3", current = 4, capacity = 5, deadlineLabel = "6.13 (금) 자정", method = "선착순"),
        ),
        BoardPost(
            id = 303, category = BoardCategory.RECRUIT,
            title = "사이드 프로젝트 디자이너 모집",
            content = "사이드 프로젝트 함께할 디자이너를 찾았습니다. 모집이 마감되었어요.",
            authorId = 2005, authorName = "정하늘", authorInitials = "하늘",
            timeLabel = "5.22", createdAt = 60,
            recruit = RecruitInfo(RecruitStatus.CLOSED, dday = null, current = 2, capacity = 2, deadlineLabel = "5.30 (금) 자정", method = "선착순"),
        ),
    )

    fun seedComments(): Map<Long, List<Comment>> = mapOf(
        101L to listOf(
            Comment(9001, "이서연", "서연", "1시간 전", "참석합니다! 신입분들 환영해요 🎉"),
            Comment(9002, "박준혁", "준혁", "40분 전", "혹시 온라인 참여도 가능한가요?"),
            Comment(9003, "김민준", "민준", "35분 전", "네! 줌 링크 공유드릴게요.", isReply = true, isAuthor = true, parentId = 9002),
        ),
        201L to listOf(
            Comment(9011, "박준혁", "준혁", "8분 전", "둘째 날 게임 사진 저한테 많아요 ㅋㅋ 올릴게요"),
            Comment(9012, "최유진", "유진", "5분 전", "바베큐 사진도 부탁해요!"),
            Comment(9013, "이서연", "서연", "3분 전", "네네 여기로 모아주세요 🙌", isReply = true, isAuthor = true, parentId = 9012),
        ),
        202L to listOf(
            Comment(9021, "김민준", "민준", "20시간 전", "이번 주는 금요일로 옮길게요! 공지 올리겠습니다."),
        ),
        205L to listOf(
            Comment(9031, "이서연", "서연", "50분 전", "저는 6/14 좋아요!"),
            Comment(9032, "박준혁", "준혁", "30분 전", "21일도 괜찮습니다"),
        ),
        301L to listOf(
            Comment(9041, "한지우", "지우", "30분 전", "비전공자도 지원 가능한가요?"),
            Comment(9042, "김민준", "민준", "20분 전", "물론이죠! 열정만 있으면 환영합니다 🙌", isReply = true, isAuthor = true, parentId = 9041),
        ),
    )
}
