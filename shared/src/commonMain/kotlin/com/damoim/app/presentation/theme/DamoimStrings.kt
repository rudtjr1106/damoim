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

    // NavHost 토스트 (그룹 B 미구현 안내)
    const val TOAST_CREATE_CLUB_TODO = "동아리 생성은 다음 단계(B 홈/동아리 관리)에서 구현돼요"
    const val TOAST_HOME_TODO = "홈 화면은 다음 단계(B 홈/동아리 관리)에서 구현돼요"

    // 프리뷰용 더미 사용자명
    const val PREVIEW_USER_NAME = "서연"
}
