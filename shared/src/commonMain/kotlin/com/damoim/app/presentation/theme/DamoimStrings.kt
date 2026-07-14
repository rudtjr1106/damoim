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
    const val JOINMANAGE_APPROVED = "승인됨"
    const val JOINMANAGE_REJECTED = "거절됨"
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
    const val TOAST_DRAFT_SAVED = "임시저장했어요"
    const val TOAST_DRAFT_LOADED = "임시저장한 글을 불러왔어요"
    const val TOAST_CAMERA_UNAVAILABLE = "카메라를 사용할 수 없어요"
    const val ROSTER_TITLE = "신청한 부원"
    fun replyingTo(name: String) = "${name}님에게 답글 작성 중"
    const val POLL_TAP_TO_VOTE = "항목을 탭해 투표하세요"

    // ══════════ D 자료실 ══════════

    // 67 자료실 홈
    const val ARCHIVE_TITLE = "자료실"
    const val ARCHIVE_STORAGE_LABEL = "저장공간"
    fun archiveStorageTotal(total: String) = " / $total"
    const val ARCHIVE_FILTER_ALL = "전체"
    const val ARCHIVE_FOLDER_DOCS = "회칙·문서"
    const val ARCHIVE_FOLDER_ACCOUNTING = "회계"
    const val ARCHIVE_FOLDER_PRESENTATION = "발표자료"
    const val ARCHIVE_FOLDER_PHOTOS = "활동사진"
    fun archiveCount(count: Int) = "전체 자료 $count"
    fun archiveFolderCount(folder: String, count: Int) = "$folder $count"
    fun resourceMeta(uploader: String, time: String, size: String) = "$uploader · $time · $size"

    // 67 빈 상태 (디자인 아카이브에 없음 — 41 게시판 빈 상태 패턴으로 신설)
    const val ARCHIVE_EMPTY_TITLE = "아직 올라온 자료가 없어요"
    const val ARCHIVE_EMPTY_SUBTITLE = "회칙·회계·발표자료를 올려\n부원들과 공유해보세요"
    const val ARCHIVE_EMPTY_CTA = "첫 자료 올리기"
    const val ARCHIVE_FOLDER_EMPTY = "이 폴더엔 아직 자료가 없어요"

    // 68 자료 상세
    const val RESOURCE_DETAIL_TITLE = "자료 상세"
    const val RESOURCE_PREVIEW = "문서 미리보기"
    fun resourcePreviewPages(pages: Int) = "문서 미리보기 · ${pages}쪽"
    const val RESOURCE_INFO_UPLOADER = "올린 사람"
    const val RESOURCE_INFO_FOLDER = "폴더"
    const val RESOURCE_INFO_FORMAT = "형식 · 크기"
    const val RESOURCE_INFO_DOWNLOADS = "다운로드"
    fun resourceUploaderName(name: String, isLeader: Boolean) = if (isLeader) "$name (동아리장)" else name
    fun resourceFormatValue(ext: String, size: String) = "$ext · $size"
    fun resourceDownloadCount(count: Int) = "${count}회"
    const val RESOURCE_DOWNLOAD = "다운로드"
    fun resourceShareText(title: String) = "[다모임] 자료실 · $title"
    const val RESOURCE_DELETE_TITLE = "자료를 삭제할까요?"
    const val RESOURCE_DELETE_MESSAGE = "삭제한 자료는 되돌릴 수 없어요."

    // 69 자료 올리기
    const val RESOURCE_UPLOAD_TITLE = "자료 올리기"
    const val RESOURCE_UPLOAD_SUBMIT = "올리기"
    const val RESOURCE_UPLOAD_ADD_FILE = "파일 추가"
    const val RESOURCE_UPLOAD_HINT = "PDF · 한글(hwp) · 오피스(ppt·xls·doc) · 압축(zip)\n한 파일당 최대 100MB"
    fun resourceUploadReady(size: String) = "$size · 업로드 준비 완료"
    const val RESOURCE_FIELD_FOLDER = "폴더"
    const val RESOURCE_FIELD_TITLE = "제목"
    const val RESOURCE_FIELD_DESC = "설명"
    const val RESOURCE_FIELD_OPTIONAL = "(선택)"
    const val RESOURCE_FIELD_VISIBILITY = "공개 범위"
    const val RESOURCE_TITLE_PLACEHOLDER = "자료 제목을 입력하세요"
    const val RESOURCE_DESC_PLACEHOLDER = "자료에 대한 간단한 설명을 남겨주세요"
    const val RESOURCE_VISIBILITY_ALL = "전체 회원"
    const val RESOURCE_VISIBILITY_COHORT = "특정 기수만"
    const val RESOURCE_FOLDER_SHEET_TITLE = "폴더 선택"
    /** 일반 회원은 활동사진 폴더에만 올릴 수 있다. */
    const val RESOURCE_FOLDER_MEMBER_NOTE = "회칙·문서, 회계, 발표자료 폴더는 운영진만 올릴 수 있어요"

    // 69 기수 선택 시트 (디자인 아카이브에 없음 — 42 기수 변경 시트 패턴으로 신설)
    const val RESOURCE_COHORT_SHEET_TITLE = "공개할 기수 선택"
    const val RESOURCE_COHORT_SHEET_DESC = "선택한 기수의 부원만 이 자료를 볼 수 있어요"
    const val RESOURCE_COHORT_CONFIRM = "선택 완료"
    const val RESOURCE_COHORT_EMPTY = "등록된 기수가 없어요"
    fun resourceCohortMembers(count: Int) = "${count}명"
    fun resourceCohortSummary(labels: String) = "$labels 부원만 볼 수 있어요"
    const val RESOURCE_INFO_VISIBILITY = "공개 범위"

    // D 그룹 토스트
    const val TOAST_RESOURCE_UPLOADED = "자료를 올렸어요"
    const val TOAST_RESOURCE_DELETED = "자료를 삭제했어요"

    // ══════════ E. 회원·기수 관리 ══════════

    // 공통 역할·상태 라벨
    const val ROLE_LEADER = "동아리장"
    const val ROLE_STAFF = "운영진"
    const val ROLE_MEMBER = "일반 회원"
    const val MEMBER_STATUS_ACTIVE = "활동"
    const val MEMBER_STATUS_DORMANT = "휴면"
    fun memberCountLabel(n: Int) = "${n}명"

    // 16 회원 관리 허브
    const val MEMBER_HUB_TITLE = "회원 관리"
    const val MEMBER_STAT_TOTAL = "전체 회원"
    const val MEMBER_STAT_STAFF = "운영진"
    const val MEMBER_STAT_COHORT = "기수"
    const val MEMBER_HUB_LIST = "회원 목록"
    fun memberHubListSub(count: Int) = "전체 ${count}명 · 검색과 필터"
    const val MEMBER_HUB_JOIN = "가입 신청"
    fun memberHubJoinSub(n: Int) = if (n > 0) "${n}건의 신청이 기다려요" else "새 신청이 없어요"
    const val MEMBER_HUB_COHORT = "기수 관리"
    fun memberHubCohortSub(range: String) = "$range · 기수 추가/배정"
    const val MEMBER_HUB_PROFILE = "내 프로필"
    fun memberHubProfileSub(name: String, cohort: String, role: String) = "$name · $cohort · $role"

    // 17 회원 목록 + 77 빈 상태
    const val MEMBER_LIST_TITLE = "회원 목록"
    const val MEMBER_SEARCH_HINT = "이름으로 검색"
    const val MEMBER_FILTER_ALL = "전체"
    const val MEMBER_EMPTY_TITLE = "아직 회원이 없어요"
    const val MEMBER_EMPTY_SUBTITLE = "가입 코드를 공유해\n부원을 초대해보세요"
    const val MEMBER_EMPTY_CTA = "가입 코드 공유"
    fun memberSearchEmpty(query: String) = "'$query' 검색 결과가 없어요"

    // 18 회원 상세
    const val MEMBER_DETAIL_TITLE = "회원 상세"
    const val MEMBER_INFO_JOINED = "가입일"
    const val MEMBER_INFO_POSTS = "작성 글"
    const val MEMBER_INFO_EVENTS = "이벤트 참여"
    const val MEMBER_INFO_LAST_ACTIVE = "최근 활동"
    fun memberInfoPosts(n: Int) = "${n}개"
    fun memberInfoEvents(n: Int) = "${n}회"
    const val MEMBER_LEADER_ACTIONS = "동아리장 관리"
    const val MEMBER_CHANGE_COHORT = "기수 변경"
    const val MEMBER_CHANGE_ROLE = "역할 변경"
    const val MEMBER_REMOVE = "동아리에서 내보내기"

    // 42 기수 변경 시트
    fun cohortChangeTitle(name: String) = "${name}님의 기수 변경"
    fun cohortChangeCurrent(label: String) = "현재: $label"
    const val COHORT_CHANGE_CONFIRM = "변경하기"

    // 18 역할 변경 시트 (신설)
    fun roleChangeTitle(name: String) = "${name}님의 역할 변경"
    const val ROLE_CHANGE_STAFF_DESC = "게시글·회원·기수를 관리할 수 있어요"
    const val ROLE_CHANGE_MEMBER_DESC = "일반 회원 권한이에요"
    const val ROLE_CHANGE_CONFIRM = "변경하기"

    // 43 내보내기 확인
    fun memberRemoveTitle(name: String) = "${name}님을 내보낼까요?"
    const val MEMBER_REMOVE_BODY = "내보낸 회원은 다시 가입 신청을 해야 참여할 수 있어요."
    const val MEMBER_REMOVE_CONFIRM = "내보내기"
    const val MEMBER_REMOVE_BLOCK_REJOIN = "같은 카카오 계정의 재가입 차단"

    // 19 기수 관리
    const val COHORT_MANAGE_TITLE = "기수 관리"
    const val COHORT_ADD = "새 기수 추가"
    const val COHORT_RECRUITING = "모집중"
    const val COHORT_INFO = "새 가입자는 가장 최근 기수로 자동 배정돼요. 기수 이름은 언제든 바꿀 수 있어요."

    // 44 새 기수 추가 시트
    const val COHORT_ADD_TITLE = "새 기수 추가"
    const val COHORT_FIELD_SHORT = "기수 번호"
    const val COHORT_FIELD_SHORT_HINT = "예: 26기"
    const val COHORT_FIELD_NAME = "표시 이름"
    const val COHORT_FIELD_NAME_HINT = "예: 2026학년 1기 (26기)"
    const val COHORT_FIELD_START = "활동 시작"
    const val COHORT_FIELD_START_HINT = "예: 2026년 3월"
    const val COHORT_DEFAULT_ASSIGN = "신규 가입자 기본 배정"
    const val COHORT_DEFAULT_ASSIGN_SUB = "새로 승인되는 회원을 이 기수로 배정해요"
    const val COHORT_ADD_CONFIRM = "추가하기"

    // 19 기수 이름 변경 시트 (신설)
    const val COHORT_RENAME_TITLE = "기수 이름 변경"
    const val COHORT_RENAME_CONFIRM = "저장하기"

    // 20 내 프로필
    const val MY_PROFILE_TITLE = "내 프로필"
    const val PROFILE_INFO_COHORT = "내 기수"
    const val PROFILE_INFO_LINKED = "연동 계정"
    const val PROFILE_LINKED_KAKAO = "카카오"
    const val PROFILE_ROW_EDIT = "프로필 수정"
    const val PROFILE_ROW_NOTIFICATION = "알림 설정"
    const val PROFILE_ROW_SWITCH = "동아리 전환"
    const val PROFILE_ROW_LOGOUT = "로그아웃"
    const val PROFILE_ROW_LEAVE = "동아리 탈퇴"
    const val APP_VERSION = "다모임 v1.0.0"

    // 33 동아리 전환 시트
    const val CLUB_SWITCH_TITLE = "동아리 전환"
    const val CLUB_SWITCH_JOIN = "코드로 참여"
    const val CLUB_SWITCH_CREATE = "새로 만들기"

    // 45 프로필 수정
    const val PROFILE_EDIT_TITLE = "프로필 수정"
    const val PROFILE_EDIT_SAVE = "저장"
    const val PROFILE_CONTACT_HELPER = "동아리장과 운영진에게만 공개돼요"
    const val PROFILE_BIO_LABEL = "한 줄 소개"
    const val PROFILE_BIO_PLACEHOLDER = "나를 소개하는 한 마디"

    // 60 동아리 탈퇴 확인
    fun clubLeaveTitle(club: String) = "${club}에서\n탈퇴할까요?"
    const val CLUB_LEAVE_BODY = "탈퇴하면 내 활동 기록과 권한이 모두 사라져요."
    const val CLUB_LEAVE_NOTE = "다시 참여하려면 가입 코드가 필요해요."
    const val CLUB_LEAVE_CONFIRM = "탈퇴하기"

    // 로그아웃 확인
    const val LOGOUT_TITLE = "로그아웃할까요?"
    const val LOGOUT_BODY = "다시 로그인하면 이어서 활동할 수 있어요."
    const val LOGOUT_CONFIRM = "로그아웃"

    // E 그룹 토스트
    const val TOAST_MEMBER_COHORT_CHANGED = "기수를 변경했어요"
    const val TOAST_MEMBER_ROLE_CHANGED = "역할을 변경했어요"
    const val TOAST_MEMBER_REMOVED = "회원을 내보냈어요"
    const val TOAST_COHORT_ADDED = "새 기수를 추가했어요"
    const val TOAST_COHORT_RENAMED = "기수 이름을 변경했어요"
    const val TOAST_PROFILE_UPDATED = "프로필을 수정했어요"

    // ══════════ F 일정/이벤트 (21~25 · 46~48 · 61~63) ══════════

    // 21/22 일정 홈·목록
    const val SCHEDULE_TITLE = "일정"
    const val SCHEDULE_SEG_CALENDAR = "캘린더"
    const val SCHEDULE_SEG_LIST = "목록"
    const val SCHEDULE_MY_APPS_ACTION = "내 신청"
    const val SCHEDULE_ADD_MY = "내 일정에 추가"
    const val SCHEDULE_ADDED_MY = "추가됨"
    const val SCHEDULE_GROUP_THIS_WEEK = "이번 주"
    const val SCHEDULE_GROUP_NEXT_WEEK = "다음 주"
    const val SCHEDULE_GROUP_LATER = "이후 일정"
    const val SCHEDULE_GROUP_PAST = "지난 일정"
    const val SCHEDULE_EMPTY_TITLE = "예정된 일정이 없어요"
    const val SCHEDULE_EMPTY_SUBTITLE = "새 일정이 등록되면\n여기에서 확인할 수 있어요"
    const val EVENT_BADGE = "이벤트"
    const val EVENT_OPEN_BADGE = "모집중"
    const val EVENT_CLOSED_BADGE = "마감"
    const val EVENT_ENDED_BADGE = "종료"
    fun eventParticipation(applied: Int, capacity: Int) = "참여 $applied/$capacity"

    // 23 일정/이벤트 등록
    const val SCHEDULE_REGISTER_TITLE = "일정 등록"
    const val SCHEDULE_EDIT_TITLE = "일정 수정"
    const val SCHEDULE_SAVE = "저장"
    const val SCHEDULE_FIELD_TITLE = "제목"
    const val SCHEDULE_TITLE_PLACEHOLDER = "일정 제목을 입력하세요"
    const val SCHEDULE_FIELD_START = "시작"
    const val SCHEDULE_FIELD_END = "종료"
    const val SCHEDULE_FIELD_LOCATION = "장소"
    const val SCHEDULE_LOCATION_PLACEHOLDER = "장소를 입력하세요"
    const val SCHEDULE_FIELD_MEMO = "메모"
    const val SCHEDULE_MEMO_PLACEHOLDER = "부원들에게 전할 내용을 적어주세요"
    const val SCHEDULE_DATE_PLACEHOLDER = "날짜와 시간 선택"
    const val SCHEDULE_MAKE_EVENT = "이벤트로 만들기"
    const val SCHEDULE_MAKE_EVENT_DESC = "참여 신청을 받는 이벤트로 등록해요"
    const val SCHEDULE_FIELD_CAPACITY = "참여 정원"
    const val SCHEDULE_CAPACITY_UNIT = "명"
    const val SCHEDULE_FIELD_DEADLINE = "신청 마감"
    const val SCHEDULE_FIELD_FORM = "신청 양식"
    fun scheduleFormSummary(n: Int) = if (n == 0) "질문 추가하기" else "질문 ${n}개 편집"

    // 61 날짜·시간 피커
    const val PICKER_START_TITLE = "시작 일시"
    const val PICKER_END_TITLE = "종료 일시"
    const val PICKER_DEADLINE_TITLE = "신청 마감"
    const val PICKER_TAB_DATE = "날짜"
    const val PICKER_TAB_TIME = "시간"
    fun pickerConfirm(label: String) = "$label 설정"

    // 46/51 신청 양식 편집 · 질문 추가
    const val FORM_EDIT_TITLE = "신청 양식 편집"
    const val FORM_ADD_QUESTION = "질문 추가"
    const val FORM_QUESTION_ADD_TITLE = "질문 추가"
    const val FORM_QUESTION_EDIT_TITLE = "질문 편집"
    const val FORM_QUESTION_CONTENT = "질문 내용"
    const val FORM_QUESTION_PLACEHOLDER = "질문을 입력하세요"
    const val FORM_ANSWER_TYPE = "응답 유형"
    const val FORM_TYPE_SELECT = "선택형"
    const val FORM_TYPE_TEXT = "주관식"
    const val FORM_TYPE_MULTI = "복수 선택"
    const val FORM_OPTION_ADD = "항목 추가"
    const val FORM_OPTION_PLACEHOLDER = "항목을 입력하세요"
    const val FORM_REQUIRED = "필수 응답"
    const val FORM_QUESTION_DELETE = "삭제"
    const val FORM_QUESTION_DONE = "질문 완료"
    fun formQuestionCount(n: Int) = "질문 ${n}개"

    // 24 이벤트 상세
    const val EVENT_HOST_PREFIX = "주최"
    const val EVENT_INFO_WHEN = "일시"
    const val EVENT_INFO_WHERE = "장소"
    const val EVENT_INFO_CAPACITY = "정원"
    const val EVENT_INFO_DEADLINE = "마감"
    const val EVENT_PARTICIPANTS = "참여자"
    const val EVENT_APPLY_CTA = "참여 신청하기"
    const val EVENT_APPLIED_CTA = "신청 완료"
    const val EVENT_CLOSED_CTA = "모집이 마감됐어요"
    const val EVENT_ENDED_CTA = "종료된 이벤트예요"
    fun eventRemaining(n: Int) = "${n}자리 남음"

    // 62 이벤트 ⋯ 메뉴
    const val EVENT_MENU_EDIT = "이벤트 수정"
    const val EVENT_MENU_APPLICANTS = "신청자 관리"
    const val EVENT_MENU_ANNOUNCE = "공지로 알리기"
    const val EVENT_MENU_CLOSE = "신청 조기 마감"
    const val EVENT_MENU_CANCEL = "이벤트 취소"

    // 47 신청자 관리
    const val APPLICANTS_TITLE = "신청자 관리"
    const val APPLICANTS_EXPORT = "명단 내보내기"
    const val APPLICANTS_STAT_APPLIED = "신청"
    const val APPLICANTS_STAT_CAPACITY = "정원"
    const val APPLICANTS_STAT_DEADLINE = "신청 마감"
    const val APPLICANTS_CLOSE_CTA = "신청 조기 마감하기"
    const val APPLICANTS_STATUS_APPLIED = "신청"
    const val APPLICANTS_STATUS_CANCELED = "취소"
    const val APPLICANT_ANSWERS_TITLE = "신청 응답"

    // 25 이벤트 참여 신청
    const val EVENT_APPLY_SHEET_TITLE = "참여 신청"
    const val EVENT_APPLY_SUBMIT = "신청 완료하기"
    const val EVENT_APPLY_UPDATE = "응답 수정하기"
    const val EVENT_APPLY_TEXT_PLACEHOLDER = "답변을 입력하세요"

    // 48 내 신청 내역 (+75 빈 상태)
    const val MY_APPS_TITLE = "내 신청 내역"
    const val MY_APPS_EDIT = "응답 수정"
    const val MY_APPS_CANCEL = "신청 취소"
    const val MY_APPS_STATUS_APPLIED = "신청완료"
    const val MY_APPS_STATUS_ENDED = "종료"
    const val MY_APPS_EMPTY_TITLE = "신청한 이벤트가 없어요"
    const val MY_APPS_EMPTY_SUBTITLE = "일정에서 이벤트에 참여 신청하면\n여기에서 모아 볼 수 있어요"
    const val MY_APPS_EMPTY_CTA = "일정 보러 가기"

    // 63 일정 삭제 / 이벤트 취소 확인
    const val SCHEDULE_DELETE_TITLE = "일정을 삭제할까요?"
    const val SCHEDULE_DELETE_DESC = "삭제한 일정은 되돌릴 수 없어요."
    const val EVENT_CANCEL_TITLE = "이벤트를 취소할까요?"
    const val EVENT_CANCEL_DESC = "신청자에게 취소가 통보되고 되돌릴 수 없어요."
    const val EVENT_CLOSE_TITLE = "신청을 마감할까요?"
    const val EVENT_CLOSE_DESC = "마감하면 더 이상 참여 신청을 받지 않아요."
    const val SCHEDULE_DELETE_CONFIRM = "삭제하기"
    const val EVENT_CANCEL_CONFIRM = "이벤트 취소"
    const val EVENT_CLOSE_CONFIRM = "마감하기"

    const val SCHEDULE_DAY_EMPTY = "이 날은 일정이 없어요"
    const val EVENT_INFO_NOTICE = "안내"
    const val APPLICANTS_STAT_REMAINING = "남은 자리"
    const val APPLICANTS_CANCELED_LABEL = "신청 취소함"
    const val MY_APPS_CANCEL_TITLE = "신청을 취소할까요?"
    const val MY_APPS_CANCEL_DESC = "취소하면 참여 명단에서 제외돼요."

    // F 토스트
    const val TOAST_SCHEDULE_CREATED = "일정을 등록했어요"
    const val TOAST_SCHEDULE_UPDATED = "일정을 수정했어요"
    const val TOAST_SCHEDULE_DELETED = "일정을 삭제했어요"
    const val TOAST_EVENT_CANCELED = "이벤트를 취소했어요"
    const val TOAST_EVENT_APPLIED = "참여 신청이 완료됐어요"
    const val TOAST_EVENT_APPLY_FAIL = "이미 신청했거나 모집이 마감됐어요"
    const val TOAST_APPLICATION_UPDATED = "응답을 수정했어요"
    const val TOAST_APPLICATION_CANCELED = "신청을 취소했어요"
    const val TOAST_EVENT_CLOSED = "신청을 마감했어요"
    const val TOAST_EVENT_ANNOUNCED = "게시판 공지로 등록했어요"
    const val TOAST_CALENDAR_ADDED = "내 일정에 추가했어요"
    const val TOAST_CALENDAR_REMOVED = "내 일정에서 뺐어요"

    // ══════════ G 설정·구독·권한 (26~30 · 49/50 · 64~66 · 83) ══════════

    // 26 설정 홈
    const val SETTINGS_HOME_TITLE = "설정"
    const val SETTINGS_SEC_CLUB = "동아리 관리"
    const val SETTINGS_SEC_SUBSCRIPTION = "구독"
    const val SETTINGS_SEC_ETC = "기타"
    const val SETTINGS_CLUB_INFO = "동아리 정보 설정"
    const val SETTINGS_JOIN_CODE = "가입 코드 관리"
    const val SETTINGS_ADMIN_PERM = "운영진 권한 관리"
    const val SETTINGS_PLAN_INFO = "구독 플랜 안내"
    const val SETTINGS_PAYMENT_HISTORY = "결제 내역"
    const val SETTINGS_NOTIF = "알림 설정"
    const val SETTINGS_INQUIRY = "문의하기"
    const val SETTINGS_BLOCKED = "차단한 사용자"
    const val SETTINGS_LOGOUT = "로그아웃"
    fun settingsClubMeta(count: Int, plan: String) = "회원 ${count}명 · $plan"
    const val SETTINGS_OVERLIMIT_TITLE = "무료 플랜 인원 초과"
    fun settingsOverLimit(used: Int, limit: Int) = "무료 플랜 인원 초과 (${used}/${limit}명)"
    const val SETTINGS_OVERLIMIT_BODY = "30명 이상은 유료 플랜이 필요해요. 7일 안에 업그레이드하지 않으면 신규 가입이 제한됩니다."
    const val SETTINGS_UPGRADE = "플랜 업그레이드"

    // 27 구독 플랜
    const val PLAN_TITLE = "구독 플랜"
    const val PLAN_HEADING = "동아리 규모에 맞는\n플랜을 선택하세요"
    fun planSubtitle(count: Int) = "현재 회원 ${count}명 — 스탠다드 플랜을 추천해요"
    const val PLAN_CURRENT_BADGE = "현재 플랜"
    const val PLAN_RECOMMEND_BADGE = "추천"
    const val PLAN_PER_MONTH = "/ 월"
    fun planStartCta(name: String) = "$name 시작하기"

    // 29 구독 관리
    const val SUB_MANAGE_TITLE = "구독 관리"
    const val SUB_MONTHLY_FEE = "월 요금"
    const val SUB_NEXT_BILLING = "다음 결제일"
    const val SUB_USAGE = "회원 사용량"
    const val SUB_CHANGE_PLAN = "플랜 변경"
    const val SUB_CHANGE_PLAN_SUB = "다른 플랜으로 변경"
    const val SUB_PAYMENT_METHOD = "결제 수단 · 영수증"
    const val SUB_PAYMENT_METHOD_SUB = "App Store에서 관리"
    const val SUB_CANCEL = "구독 해지"
    const val SUB_CANCEL_SUB = "App Store 구독"
    const val SUB_PAYMENT_HISTORY = "결제 내역"
    const val SUB_ACTIVE_BADGE = "이용 중"
    const val SUB_CANCEL_DIALOG_TITLE = "구독을 해지할까요?"
    const val SUB_CANCEL_DIALOG_DESC = "무료 플랜으로 전환되고, 다음 결제일부터 청구되지 않아요."
    const val SUB_CANCEL_CONFIRM = "해지하기"

    // 결제(인앱)
    fun billingCheckout(price: String) = "$price · 결제하기"

    // 49 결제 완료 / 50 결제 실패
    const val PAY_DONE_TITLE = "결제가 완료되었어요"
    const val PAY_DONE_DESC = "이제 회원 100명까지 함께할 수 있어요.\n신규 가입 제한이 해제되었습니다."
    const val PAY_DONE_CTA = "확인"
    const val PAY_FAIL_TITLE = "결제에 실패했어요"
    const val PAY_FAIL_DESC = "결제가 완료되지 않았어요. 카드 한도·네트워크를 확인하고 다시 시도해주세요."
    const val PAY_FAIL_RETRY = "다시 시도"
    const val PAY_FAIL_LATER = "나중에 하기"

    // 30 운영진 권한 관리
    const val ADMIN_TITLE = "운영진 권한 관리"
    const val ADMIN_ADD = "운영진 추가"
    const val ADMIN_INFO = "동아리장 권한(구독 관리, 동아리 삭제)은 위임할 수 없어요"
    const val ADMIN_ADD_SHEET_TITLE = "운영진 추가"
    const val ADMIN_ADD_SHEET_SUB = "일반 회원을 운영진으로 지정해요"
    const val ADMIN_ADD_EMPTY = "지정할 수 있는 일반 회원이 없어요"
    const val ADMIN_DEFAULT_TITLE = "운영진"

    // 64 운영진 ⋯ 메뉴
    const val ADMIN_MENU_TITLE = "직함 변경"
    const val ADMIN_MENU_DETAIL = "회원 상세 보기"
    const val ADMIN_MENU_REMOVE = "운영진에서 해제"
    const val ADMIN_TITLE_DIALOG = "직함 변경"
    const val ADMIN_TITLE_PLACEHOLDER = "직함을 입력하세요 (예: 부회장)"
    const val ADMIN_TITLE_SAVE = "저장"
    const val ADMIN_REMOVE_TITLE = "운영진에서 해제할까요?"
    fun adminRemoveDesc(name: String) = "${name}님의 위임 권한이 모두 회수돼요."
    const val ADMIN_REMOVE_CONFIRM = "해제하기"

    // 65 알림 설정
    const val NOTIF_TITLE = "알림 설정"
    const val NOTIF_SEC_ACTIVITY = "활동 알림"
    const val NOTIF_SEC_ADMIN = "운영 알림"
    const val NOTIF_SEC_DND = "방해 금지"
    const val NOTIF_PUSH = "푸시 알림"
    const val NOTIF_PUSH_SUB = "앱 전체 알림 받기"
    const val NOTIF_NEW_POST = "새 글"
    const val NOTIF_COMMENT = "댓글·답글"
    const val NOTIF_SCHEDULE = "일정 리마인드"
    const val NOTIF_REMINDER_TIMING = "리마인드 시점"
    const val NOTIF_JOIN = "가입 신청"
    const val NOTIF_EVENT = "이벤트 신청"
    const val NOTIF_DND = "방해 금지 시간"
    const val NOTIF_DND_SUB = "이 시간에는 알림을 받지 않아요"
    const val NOTIF_REMINDER_SHEET_TITLE = "리마인드 시점"
    val NOTIF_REMINDER_OPTIONS = listOf("1시간 전", "1일 전", "1일 전 · 1시간 전", "3일 전 · 1일 전")
    const val NOTIF_DND_SHEET_TITLE = "방해 금지 시간"
    val NOTIF_DND_OPTIONS = listOf("22:00 ~ 07:00", "23:00 ~ 08:00", "00:00 ~ 09:00")

    // 66 문의하기
    const val INQUIRY_TITLE = "문의하기"
    const val INQUIRY_TYPE = "문의 유형"
    val INQUIRY_TYPES = listOf("이용 문의", "버그 신고", "결제·환불", "기타")
    const val INQUIRY_SUBJECT = "제목"
    const val INQUIRY_SUBJECT_PLACEHOLDER = "제목을 입력하세요"
    const val INQUIRY_CONTENT = "내용"
    const val INQUIRY_CONTENT_PLACEHOLDER = "문의 내용을 자세히 적어주세요"
    const val INQUIRY_ATTACH = "스크린샷 첨부 (선택)"
    const val INQUIRY_ATTACH_HINT = "탭해서 이미지 추가"
    const val INQUIRY_HELP = "도움말 센터"
    const val INQUIRY_HELP_HINT = "자주 묻는 질문을 먼저 확인해보세요"
    const val INQUIRY_SEND = "보내기"
    const val INQUIRY_EMAIL = "rudtjr1206@naver.com"
    const val INQUIRY_SUBJECT_PREFIX = "[다모임 문의]"
    const val TOAST_INQUIRY_ATTACH = "메일 앱에서 스크린샷을 첨부할 수 있어요"

    // 83 차단한 사용자
    const val BLOCKED_TITLE = "차단한 사용자"
    const val BLOCKED_DESC = "차단한 사용자의 게시글·댓글이 보이지 않고, 서로의 프로필에 접근할 수 없어요"
    fun blockedCount(n: Int) = "차단 ${n}명"
    const val BLOCKED_UNBLOCK = "차단 해제"
    const val BLOCKED_EMPTY_TITLE = "차단한 사용자가 없어요"
    const val BLOCKED_EMPTY_SUB = "차단하면 여기에서 관리할 수 있어요"

    // G 토스트
    const val TOAST_SUBSCRIBED = "구독이 시작됐어요"
    const val TOAST_SUB_CANCELED = "구독을 해지했어요"
    const val TOAST_EXTERNAL_STORE = "스토어 구독 설정에서 관리해요"
    const val TOAST_ADMIN_ADDED = "운영진으로 지정했어요"
    const val TOAST_ADMIN_REMOVED = "운영진에서 해제했어요"
    const val TOAST_ADMIN_TITLE_CHANGED = "직함을 변경했어요"
    const val TOAST_UNBLOCKED = "차단을 해제했어요"
    const val TOAST_INQUIRY_SENT = "문의가 접수되었어요"
    const val TOAST_NOTIF_SAVED = "알림 설정을 저장했어요"

    // 프리뷰용 더미 사용자명
    const val PREVIEW_USER_NAME = "서연"
}
