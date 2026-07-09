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

    // 프리뷰용 더미 사용자명
    const val PREVIEW_USER_NAME = "서연"
}
