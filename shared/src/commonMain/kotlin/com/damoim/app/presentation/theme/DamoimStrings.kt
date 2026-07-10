package com.damoim.app.presentation.theme

/**
 * UI 문자열 네이밍 저장소. (UMC-Product `AppStrings` 방식)
 *
 * 화면 코드에 한글 리터럴을 직접 쓰지 않고 여기서 참조한다. 다국어/문구 변경 시 이 파일만 손대면 되고,
 * 서버·i18n 도입 시 Compose Resources(`Res.string.*`)로 옮기기도 쉽다.
 * 네이밍 규칙: `화면(기능)_용도` (UPPER_SNAKE).
 */
object DamoimStrings {

    // 공통
    const val COMMON_CANCEL = "취소"
    const val COMMON_CONFIRM = "확인"
    const val COMMON_DONE = "완료"
    const val COMMON_CLOSE = "닫기"
    const val REQUIRED_MARK = "*"
    fun charCounter(current: Int, max: Int) = "$current/$max"

    // 01 로그인 / 온보딩
    const val APP_NAME = "다모임"
    const val LOGIN_TAGLINE = "우리 동아리의 모든 것을\n한곳에서"
    const val LOGIN_KAKAO = "카카오 로그인"
    const val LOGIN_FOOTER =
        "로그인 후 가입 코드로 동아리에 참여할 수 있어요\n시작하면 이용약관 및 개인정보 처리방침에 동의하게 됩니다"

    // 31 프로필 설정
    const val PROFILE_TITLE = "거의 다 왔어요!\n프로필을 설정해주세요"
    const val PROFILE_SUBTITLE = "동아리에서 사용할 프로필이에요"
    const val PROFILE_NICKNAME_LABEL = "이름 (실명 권장)"
    const val PROFILE_NICKNAME_PLACEHOLDER = "이름을 입력해주세요"
    const val PROFILE_CONTACT_LABEL = "연락처"
    const val PROFILE_CONTACT_PLACEHOLDER = "010-0000-0000"
    const val PROFILE_AVATAR_FALLBACK = "다"

    // 32 시작하기 (소속 동아리 없음)
    const val START_EMPTY_TITLE = "아직 소속된 동아리가 없어요"
    const val START_EMPTY_SUBTITLE = "가입 코드로 참여하거나 직접 만들어보세요"
    const val START_JOIN_TITLE = "가입 코드로 참여"
    const val START_JOIN_SUBTITLE = "동아리장에게 받은 6자리 코드 입력"
    const val START_CREATE_TITLE = "새 동아리 만들기"
    const val START_CREATE_SUBTITLE = "30명 미만은 무료로 시작"
    const val START_FOOTER = "가입 신청 후 승인을 기다리는 동아리가 있다면 여기에 표시돼요"

    // 03 가입 코드 입력
    const val JOINCODE_TITLE = "가입 코드를\n입력해주세요"
    const val JOINCODE_SUBTITLE = "동아리장에게 받은 6자리 코드를 입력하면\n가입 신청이 전달돼요"
    const val JOINCODE_INFO = "코드를 모른다면 동아리장에게 문의하세요"
    const val JOINCODE_SUBMIT = "가입 신청하기"
    const val JOINCODE_CREATE_PROMPT = "동아리를 새로 만들고 싶다면? "
    const val JOINCODE_CREATE_LINK = "동아리 생성"

    // 04 가입 신청 완료
    const val JOIN_COMPLETE_TITLE = "가입 신청 완료!"
    const val JOIN_COMPLETE_MESSAGE = "동아리장이 신청을 확인하고 있어요\n승인되면 알림으로 알려드릴게요"

    // 38 가입 신청 거절됨
    const val JOIN_REJECTED_TITLE = "가입이 거절되었어요"
    const val JOIN_REJECTED_RETRY = "다른 코드 입력하기"

    // 동아리 요약 카드
    fun clubMeta(category: String, memberCount: Int) = "$category · 멤버 ${memberCount}명"

    // ── B. 홈 · 동아리 관리 ──
    const val COMMON_SAVE = "저장"

    // 05/06 홈
    const val HOME_MY_CLUB = "우리 동아리"
    const val HOME_ROLE_LEADER = "동아리장"
    fun homeGreeting(name: String) = "안녕하세요, ${name}님"
    const val HOME_SECTION_SCHEDULE = "다가오는 일정"
    const val HOME_SECTION_BOARD = "지금 게시판에선"
    const val HOME_SEE_ALL = "전체보기"
    // 퀵액션
    const val QA_BOARD = "게시판"
    const val QA_SCHEDULE = "일정"
    const val QA_MEMBERS = "회원 관리"
    const val QA_CODE = "가입 코드"
    const val QA_ARCHIVE = "자료실"
    const val QA_NOTICE = "공지"
    const val QA_PROFILE = "내 프로필"
    // 하단 탭
    const val TAB_HOME = "홈"
    const val TAB_BOARD = "게시판"
    const val TAB_SCHEDULE = "일정"
    const val TAB_MEMBERS = "회원"
    const val TAB_SETTINGS = "설정"
    // 게시판 카테고리
    const val BOARD_NOTICE = "공지"
    const val BOARD_FREE = "자유"
    const val BOARD_RECRUIT = "모집"

    // 07 동아리 생성
    const val CREATE_TITLE = "동아리 만들기"
    const val CREATE_LOGO = "로고 등록"
    const val CREATE_NAME_LABEL = "동아리 이름"
    const val CREATE_NAME_PLACEHOLDER = "동아리 이름을 입력해주세요"
    const val CREATE_INTRO_LABEL = "한 줄 소개"
    const val CREATE_INTRO_PLACEHOLDER = "우리 동아리를 소개해주세요"
    const val CREATE_CATEGORY_LABEL = "카테고리"
    val CREATE_CATEGORIES = listOf("IT/학술", "운동", "취미", "봉사", "문화/예술", "기타")
    const val CREATE_FREE_INFO = "30명 미만 동아리는 무료로 모든 핵심 기능을 이용할 수 있어요"
    const val CREATE_SUBMIT = "동아리 만들기"

    // 08 정보 설정
    const val SETTINGS_TITLE = "동아리 정보 설정"
    const val SETTINGS_NAME_LABEL = "동아리 이름"
    const val SETTINGS_INTRO_LABEL = "소개"
    const val SETTINGS_CODE_LABEL = "가입 코드"
    const val SETTINGS_CODE_CURRENT = "현재 코드"
    const val SETTINGS_CODE_REGEN = "재발급"
    const val SETTINGS_CODE_DISABLE = "비활성화"
    const val SETTINGS_CODE_HINT = "코드를 재발급하면 이전 코드는 즉시 사용할 수 없어요"

    // 59 코드 공유
    const val SHARE_TITLE = "가입 코드 공유"
    const val SHARE_SUBTITLE = "새 부원에게 코드를 전달하세요"
    const val SHARE_KAKAO = "카카오톡으로 공유"
    const val SHARE_COPY_LINK = "초대 링크 복사"
    const val SHARE_HINT = "링크로 접속하면 코드가 자동 입력돼요"

    // 09 가입 신청 관리
    const val JOINMANAGE_TITLE = "가입 신청 관리"
    fun joinTabPending(count: Int) = "대기 $count"
    fun joinTabDone(count: Int) = "처리 완료 $count"
    const val JOINMANAGE_APPROVE = "승인"
    const val JOINMANAGE_REJECT = "거절"
    fun applicantMeta(gisu: String, date: String) = "$gisu · $date"

    // 37/74 알림
    const val NOTIFICATION_TITLE = "알림"
    const val NOTIFICATION_MARK_ALL = "모두 읽음"
    const val NOTIFICATION_EMPTY_TITLE = "새 알림이 없어요"
    const val NOTIFICATION_EMPTY_SUBTITLE = "공지·댓글·일정 소식이 도착하면\n여기에서 모아 볼 수 있어요"

    // 미구현(그룹 C~G) 안내 토스트
    const val TOAST_COMING_SOON = "아직 준비 중인 화면이에요"
    const val TOAST_KAKAO_SHARE = "카카오톡 공유는 추후 연동돼요"
    const val TOAST_LINK_COPIED = "초대 링크가 복사되었어요"
    const val TOAST_CODE_COPIED = "가입 코드가 복사되었어요"
    const val TOAST_CODE_REGENERATED = "새 코드가 발급되었어요"
    const val TOAST_CODE_DISABLED = "가입 코드를 비활성화했어요"

    // NavHost 토스트 (그룹 B 미구현 안내)
    const val TOAST_CREATE_CLUB_TODO = "동아리 생성은 다음 단계(B 홈/동아리 관리)에서 구현돼요"
    const val TOAST_HOME_TODO = "홈 화면은 다음 단계(B 홈/동아리 관리)에서 구현돼요"

    // ── C. 게시판 ──
    // 10 게시판 홈 / 카테고리 필터
    const val BOARD_TITLE = "게시판"
    const val BOARD_FILTER_ALL = "전체"
    const val BOARD_PINNED = "필독"
    // (BOARD_NOTICE/BOARD_FREE/BOARD_RECRUIT 는 위 B 섹션에 이미 정의됨)

    // 11/12/13 목록
    const val BOARD_LIST_FREE = "자유 게시판"
    const val BOARD_LIST_NOTICE = "공지 게시판"
    const val BOARD_LIST_RECRUIT = "모집 게시판"
    const val BOARD_SEARCH_PLACEHOLDER = "제목, 내용, 작성자 검색"
    const val BOARD_SORT_RECENT = "최신순"
    const val BOARD_SORT_POPULAR = "인기순"
    const val BOARD_SORT_COMMENTS = "댓글순"
    const val BOARD_SORT_PERIOD = "기간"
    const val BOARD_RECRUIT_ONLY = "모집중만"
    const val RECRUIT_OPEN = "모집중"
    const val RECRUIT_CLOSED = "마감"
    fun recruitProgress(current: Int, capacity: Int) = "$current/${capacity}명"
    fun boardAuthorGisu(name: String, gisu: String) = "$name · $gisu"

    // 게시글 메타
    fun viewCountLabel(n: Int) = "조회 $n"
    fun readRateLabel(n: Int) = "확인율 $n%"
    fun commentCountLabel(n: Int) = "댓글 $n"
    fun likeCountLabel(n: Int) = "좋아요 $n"

    // 14/36 상세
    fun commentSectionHeader(n: Int) = "댓글 $n"
    const val BOARD_AUTHOR_BADGE = "작성자"
    const val BOARD_REPLY = "답글 달기"
    const val BOARD_COMMENT_HINT = "댓글을 입력하세요"
    // 36 투표 상세
    const val POLL_LABEL = "투표"
    const val POLL_MULTI_OFF = "복수 선택 불가"
    const val POLL_MULTI_ON = "복수 선택"
    const val POLL_ANON = "익명 투표"
    fun pollMeta(anon: Boolean, multi: Boolean): String {
        val a = if (anon) POLL_ANON else "공개 투표"
        val m = if (multi) POLL_MULTI_ON else POLL_MULTI_OFF
        return "$a · $m"
    }
    fun pollParticipation(count: Int, myVote: String?): String =
        if (myVote != null) "${count}명 참여 · 내 투표: $myVote" else "${count}명 참여"
    const val POLL_REVOTE = "다시 투표"

    // 15/34/35/39 작성
    const val WRITE_TITLE = "글쓰기"
    const val WRITE_SUBMIT = "등록"
    const val WRITE_NOTICE_LOCK = "공지는 운영진만"
    const val WRITE_TITLE_PLACEHOLDER = "제목을 입력하세요"
    const val WRITE_BODY_PLACEHOLDER = "내용을 입력하세요"
    const val WRITE_TEMP_SAVE = "임시저장"
    fun imageCount(current: Int, max: Int) = "$current/$max"

    // 41 게시판 빈 상태
    const val BOARD_EMPTY_TITLE = "아직 게시글이 없어요"
    const val BOARD_EMPTY_SUBTITLE = "첫 번째 글을 남겨 동아리 게시판을\n시작해보세요"
    const val BOARD_EMPTY_CTA = "첫 글 작성하기"

    // 40 검색 / 76 무결과
    const val SEARCH_TAB_ALL = "전체"
    const val SEARCH_SECTION_POST = "게시글"
    const val SEARCH_SECTION_SCHEDULE = "일정"
    const val SEARCH_SECTION_FILE = "파일"
    fun searchNoResultTitle(query: String) = "'$query' 검색 결과가 없어요"
    const val SEARCH_NO_RESULT_SUBTITLE = "단어의 철자를 확인하거나\n다른 키워드로 검색해보세요"

    // 79 없는 콘텐츠 오류
    const val POST_ERROR_TITLE = "삭제되었거나 없는 게시글이에요"
    const val POST_ERROR_SUBTITLE = "작성자가 글을 삭제했거나\n접근 권한이 변경되었을 수 있어요"
    const val POST_ERROR_BACK = "게시판으로 돌아가기"

    // 84 모집 글 상세
    const val RECRUIT_STATUS = "모집 현황"
    fun recruitDeadlineBadge(dday: String) = "마감 $dday"
    fun recruitAppliedSuffix(capacity: Int) = "/ ${capacity}명 신청"
    fun recruitRemaining(n: Int) = "${n}자리 남음"
    const val RECRUIT_AUTO_CLOSE = "선착순 마감 · 정원이 차면 자동 종료돼요"
    const val RECRUIT_INFO_DEADLINE = "모집 마감"
    const val RECRUIT_INFO_TARGET = "모집 대상"
    const val RECRUIT_INFO_METHOD = "모집 방식"
    fun recruitApplicantSummary(name: String, others: Int) = "$name 외 ${others}명이 신청했어요"
    const val RECRUIT_APPLY = "신청하기"

    // 85 검색 시작
    const val SEARCH_PLACEHOLDER = "게시글, 일정, 파일 검색"
    const val SEARCH_RECENT = "최근 검색어"
    const val SEARCH_CLEAR_ALL = "전체 삭제"
    const val SEARCH_RECOMMENDED = "추천 검색어"

    // 86 날짜·시간 선택기
    const val PICKER_TITLE = "마감일 설정"
    const val PICKER_TIME = "시간"
    const val PICKER_AM = "오전"
    const val PICKER_PM = "오후"
    val PICKER_WEEKDAYS = listOf("일", "월", "화", "수", "목", "금", "토")
    fun pickerMonth(year: Int, month: Int) = "${year}년 ${month}월"
    val PICKER_PRESETS = listOf("오늘 자정", "내일 오후 6시", "1주일 뒤")

    // 52/53 카테고리 선택 시트
    const val CATEGORY_SHEET_TITLE = "게시판 선택"
    const val CATEGORY_SHEET_SUBTITLE = "글을 올릴 게시판을 골라주세요"
    const val CATEGORY_DESC_FREE = "일상 소통, 후기, 질문 무엇이든"
    const val CATEGORY_DESC_RECRUIT = "스터디·프로젝트 팀원, 신입 부원 모집"
    const val CATEGORY_DESC_NOTICE = "전체 회원에게 전달하는 소식"
    const val CATEGORY_MEMBER_HINT = "운영진 권한이 있으면 공지 게시판이 활성화되고, 필독 지정 옵션이 추가로 표시돼요"
    const val ADMIN_BADGE = "운영진"
    const val CATEGORY_PIN = "필독 지정"
    const val CATEGORY_PIN_DESC = "게시판 상단에 고정되고 확인율이 집계돼요"
    const val CATEGORY_PUSH = "푸시 알림 발송"
    const val CATEGORY_PUSH_DESC = "등록 즉시 전체 회원에게 알림"
    const val CATEGORY_CONFIRM = "선택 완료"

    // 71 첨부 방식 시트
    const val ATTACH_SHEET_TITLE = "첨부하기"
    const val ATTACH_PHOTO = "사진·동영상"
    const val ATTACH_PHOTO_DESC = "갤러리에서 선택"
    const val ATTACH_CAMERA = "카메라"
    const val ATTACH_CAMERA_DESC = "바로 촬영해서 첨부"
    const val ATTACH_DOC = "문서 파일"
    const val ATTACH_DOC_DESC = "PDF·문서 등 첨부"
    const val ATTACH_LINK = "링크"
    const val ATTACH_LINK_DESC = "URL 붙여넣기"
    const val ATTACH_POLL = "투표"
    const val ATTACH_POLL_DESC = "선택지를 만들어 의견 모으기"

    // 54 게시글 ⋯ 메뉴
    const val MENU_EDIT = "수정하기"
    const val MENU_SHARE = "공유하기"
    const val MENU_COPY_LINK = "링크 복사"
    const val MENU_PIN = "상단 고정"
    const val MENU_DELETE = "삭제하기"
    const val POST_MENU_NOTE = "다른 회원의 글에는 공유하기 · 링크 복사 · 신고하기만 표시돼요"

    // 55 댓글 메뉴
    const val CMENU_REPLY = "답글 달기"
    const val CMENU_COPY = "내용 복사"
    const val CMENU_REPORT = "신고하기"
    const val COMMENT_MENU_NOTE = "내 댓글에는 수정하기 · 삭제하기가 대신 표시돼요"

    // 56 삭제 확인
    const val DELETE_TITLE = "게시글을 삭제할까요?"
    fun deleteMessage(comments: Int) = "댓글 ${comments}개와 첨부 파일도 함께 삭제되며\n되돌릴 수 없어요."
    const val DELETE_CONFIRM = "삭제"

    // 57 이미지 뷰어
    fun imageIndex(current: Int, total: Int) = "$current / $total"

    // 82 신고 사유 시트
    const val REPORT_TITLE = "신고 사유를 선택해주세요"
    const val REPORT_SUBTITLE = "신고 내용은 운영진이 확인하며, 작성자에게 알려지지 않아요"
    const val REPORT_SUBMIT = "신고하기"
    fun reportReasonLabel(reason: com.damoim.app.domain.model.ReportReason): String = when (reason) {
        com.damoim.app.domain.model.ReportReason.SPAM -> "스팸·광고성 홍보"
        com.damoim.app.domain.model.ReportReason.ABUSE -> "욕설·비방·혐오 표현"
        com.damoim.app.domain.model.ReportReason.SEXUAL -> "음란·선정성 콘텐츠"
        com.damoim.app.domain.model.ReportReason.FRAUD -> "사기·사칭"
        com.damoim.app.domain.model.ReportReason.PRIVACY -> "개인정보 노출"
        com.damoim.app.domain.model.ReportReason.ETC -> "기타"
    }

    // C 그룹 토스트(58 피드백)
    const val TOAST_POST_LINK_COPIED = "링크가 복사되었어요"
    const val TOAST_REPORTED = "신고가 접수되었어요"
    const val TOAST_FILE_DOWNLOADED = "파일을 다운로드했어요"
    const val TOAST_POST_DELETED = "게시글을 삭제했어요"
    const val TOAST_POST_PINNED = "게시글을 상단에 고정했어요"
    const val TOAST_POST_UNPINNED = "상단 고정을 해제했어요"
    const val TOAST_COMMENT_COPIED = "댓글을 복사했어요"
    const val TOAST_RECRUIT_APPLIED = "모집 신청이 접수되었어요"
    const val TOAST_RECRUIT_FULL = "이미 신청했거나 모집이 마감되었어요"
    const val TOAST_POST_SUBMITTED = "게시글이 등록되었어요"
    const val TOAST_POST_UPDATED = "게시글을 수정했어요"
    const val WRITE_TITLE_REQUIRED = "제목을 입력해주세요"
    const val RECRUIT_APPLIED_BUTTON = "신청 완료"
    fun replyingTo(name: String) = "${name}님에게 답글 작성 중"
    const val POLL_TAP_TO_VOTE = "항목을 탭해 투표하세요"

    // 프리뷰용 더미 사용자명
    const val PREVIEW_USER_NAME = "서연"
}
