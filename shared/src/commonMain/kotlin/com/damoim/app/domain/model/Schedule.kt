package com.damoim.app.domain.model

import kotlinx.datetime.LocalDate

/**
 * 일정/이벤트(F 그룹). [type]이 [ScheduleType.EVENT]이면 참여 신청을 받는 이벤트라
 * [event]가 채워진다(정원·마감·신청 양식·신청자). 일반 일정은 event == null.
 *
 * [date]는 캘린더 배치·정렬 기준 시작일. 표시 라벨(요일/그룹 헤더)은 이 값에서 파생한다.
 */
data class Schedule(
    val id: Long,
    val type: ScheduleType,
    val title: String,
    val date: LocalDate,               // 시작 날짜(캘린더 점·선택·정렬)
    val timeLabel: String,             // "오전 10:00"
    val startHour: Int = 0,            // 수정 프리필용 원본 시각
    val startMinute: Int = 0,
    val endLabel: String? = null,      // 종료 표시(이벤트)
    val location: String = "",
    val memo: String = "",
    val accent: ScheduleAccent = ScheduleAccent.PRIMARY,   // 좌측 컬러바
    val addedToMyCalendar: Boolean = false,                // 21/22 '내 일정에 추가'
    val hostName: String = "",
    val createdAt: Long = 0,           // 정렬 보조(동일 날짜 내)
    val event: EventInfo? = null,
) {
    val isEvent: Boolean get() = type == ScheduleType.EVENT
}

enum class ScheduleType { SCHEDULE, EVENT }

/** 일정 카드 좌측 컬러바/포인트 색. PRIMARY=파랑, SKY=하늘. */
enum class ScheduleAccent { PRIMARY, SKY }

/** 이벤트 부가 정보(24 상세·47 관리). */
data class EventInfo(
    val capacity: Int,
    val appliedCount: Int,
    val deadlineDate: LocalDate,
    val deadlineLabel: String,         // "6.12 (목) 자정"
    val status: EventStatus = EventStatus.OPEN,
    val dday: String = "",             // "D-10"
    val meta: String = "",             // "1박 2일 · 가평"
    val form: List<FormQuestion> = emptyList(),
    val applicants: List<EventApplicant> = emptyList(),
    val appliedByMe: Boolean = false,
) {
    /** 남은 자리(취소 제외 활성 신청 기준은 store에서 appliedCount로 관리). */
    val remaining: Int get() = (capacity - appliedCount).coerceAtLeast(0)
    val activeApplicants: List<EventApplicant> get() = applicants.filter { it.status == ApplicantStatus.APPLIED }
}

/** 모집중 / 마감(정원·조기마감) / 종료(지난 이벤트). */
enum class EventStatus { OPEN, CLOSED, ENDED }

// ── 신청 양식(23·46·51·25) ──

/** 선택형 / 복수 선택 / 주관식. */
enum class QuestionType { SELECT, MULTI, TEXT }

data class FormQuestion(
    val id: Long,
    val text: String,
    val type: QuestionType,
    val options: List<String> = emptyList(),   // SELECT/MULTI만
    val required: Boolean = true,
)

// ── 신청자(47) / 내 신청(48) ──

enum class ApplicantStatus { APPLIED, CANCELED }

data class EventApplicant(
    val id: Long,
    val name: String,
    val initials: String,
    val avatarTint: Int = 0,           // 아바타 배경 톤 인덱스
    val status: ApplicantStatus = ApplicantStatus.APPLIED,
    val appliedLabel: String = "방금 전",
    val answers: List<QuestionAnswer> = emptyList(),   // G2 신청자 응답 상세
)

/** 폼 응답 한 줄(질문→답). */
data class QuestionAnswer(val question: String, val answer: String)

enum class ApplicationStatus { APPLIED, ENDED }

/** 48 내 신청 내역 항목. */
data class MyApplication(
    val eventId: Long,
    val title: String,
    val dateLabel: String,             // "6월 14일 (토)"
    val status: ApplicationStatus,
    val answers: List<QuestionAnswer> = emptyList(),
)

/**
 * 23 등록/수정 입력값(진행 중 초안 — MockStore가 보관해 46 양식편집과 공유).
 * 시간은 구조화 값(시/분)으로 담고 표시 라벨은 store/화면에서 파생한다.
 */
data class ScheduleDraft(
    val editId: Long? = null,
    val title: String = "",
    val startDate: LocalDate? = null,
    val startHour: Int = 14, val startMinute: Int = 0,
    val hasEnd: Boolean = false,
    val endDate: LocalDate? = null,
    val endHour: Int = 16, val endMinute: Int = 0,
    val location: String = "",
    val memo: String = "",
    val isEvent: Boolean = false,
    val capacity: String = "",         // 인라인 숫자 입력(빈값 허용)
    val deadlineDate: LocalDate? = null,
    val deadlineHour: Int = 23, val deadlineMinute: Int = 59,
    val form: List<FormQuestion> = emptyList(),
)
