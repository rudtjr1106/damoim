package com.damoim.app.data.remote.schedule

import com.damoim.app.domain.model.ApplicantStatus
import com.damoim.app.domain.model.ApplicationStatus
import com.damoim.app.domain.model.EventApplicant
import com.damoim.app.domain.model.EventInfo
import com.damoim.app.domain.model.EventStatus
import com.damoim.app.domain.model.FormQuestion
import com.damoim.app.domain.model.MyApplication
import com.damoim.app.domain.model.QuestionAnswer
import com.damoim.app.domain.model.QuestionType
import com.damoim.app.domain.model.Schedule
import com.damoim.app.domain.model.ScheduleAccent
import com.damoim.app.domain.model.ScheduleDraft
import com.damoim.app.domain.model.ScheduleType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/** F 일정/이벤트 그룹 DTO. 서버 schedule/ScheduleDtos와 JSON 계약 1:1. */

// ── 요청 ──
@Serializable
data class FormQuestionInputDto(
    val text: String,
    val type: String,
    val options: List<String> = emptyList(),
    val required: Boolean = true,
)

@Serializable
data class SaveScheduleRequestDto(
    val title: String,
    val startDate: String,
    val startHour: Int = 14,
    val startMinute: Int = 0,
    val hasEnd: Boolean = false,
    val endDate: String? = null,
    val endHour: Int = 16,
    val endMinute: Int = 0,
    val location: String = "",
    val memo: String = "",
    val isEvent: Boolean = false,
    val capacity: Int? = null,
    val deadlineDate: String? = null,
    val deadlineHour: Int = 23,
    val deadlineMinute: Int = 59,
    val form: List<FormQuestionInputDto> = emptyList(),
)

@Serializable
data class QuestionAnswerInputDto(val question: String, val answer: String)

@Serializable
data class ApplicationAnswersRequestDto(val answers: List<QuestionAnswerInputDto> = emptyList())

// ── 응답 ──
@Serializable
data class QuestionAnswerDto(val question: String = "", val answer: String = "")

@Serializable
data class FormQuestionResponseDto(
    val id: Long,
    val text: String,
    val type: String,
    val options: List<String> = emptyList(),
    val required: Boolean = true,
)

@Serializable
data class ApplicantResponseDto(
    val id: Long,
    val name: String = "",
    val initials: String = "",
    val avatarTint: Int = 0,
    val status: String = "APPLIED",
    val appliedLabel: String = "",
    val answers: List<QuestionAnswerDto> = emptyList(),
    val imageUrl: String? = null,
)

@Serializable
data class EventResponseDto(
    val capacity: Int = 0,
    val appliedCount: Int = 0,
    val deadlineDate: String = "",
    val deadlineLabel: String = "",
    val status: String = "OPEN",
    val dday: String = "",
    val meta: String = "",
    val form: List<FormQuestionResponseDto> = emptyList(),
    val applicants: List<ApplicantResponseDto> = emptyList(),
    val appliedByMe: Boolean = false,
    val isMine: Boolean = false,
)

@Serializable
data class ScheduleResponseDto(
    val id: Long,
    val type: String,
    val title: String,
    val date: String,
    val timeLabel: String = "",
    val startHour: Int = 0,
    val startMinute: Int = 0,
    val endLabel: String? = null,
    val endDate: String? = null,
    val endHour: Int = 0,
    val endMinute: Int = 0,
    val location: String = "",
    val memo: String = "",
    val accent: String = "PRIMARY",
    val addedToMyCalendar: Boolean = false,
    val hostName: String = "",
    val createdAt: Long = 0,
    val event: EventResponseDto? = null,
)

@Serializable
data class MyApplicationResponseDto(
    val eventId: Long,
    val title: String = "",
    val dateLabel: String = "",
    val status: String = "APPLIED",
    val answers: List<QuestionAnswerDto> = emptyList(),
)

// ── 매퍼 ──
private val EPOCH = LocalDate.parse("1970-01-01")
internal fun parseDate(s: String): LocalDate = runCatching { LocalDate.parse(s) }.getOrDefault(EPOCH)

internal fun scheduleTypeOf(s: String): ScheduleType =
    runCatching { ScheduleType.valueOf(s) }.getOrDefault(ScheduleType.SCHEDULE)

internal fun scheduleAccentOf(s: String): ScheduleAccent =
    runCatching { ScheduleAccent.valueOf(s) }.getOrDefault(ScheduleAccent.PRIMARY)

internal fun eventStatusOf(s: String): EventStatus =
    runCatching { EventStatus.valueOf(s) }.getOrDefault(EventStatus.OPEN)

internal fun questionTypeOf(s: String): QuestionType =
    runCatching { QuestionType.valueOf(s) }.getOrDefault(QuestionType.TEXT)

internal fun applicantStatusOf(s: String): ApplicantStatus =
    runCatching { ApplicantStatus.valueOf(s) }.getOrDefault(ApplicantStatus.APPLIED)

internal fun applicationStatusOf(s: String): ApplicationStatus =
    runCatching { ApplicationStatus.valueOf(s) }.getOrDefault(ApplicationStatus.APPLIED)

internal fun QuestionAnswerDto.toDomain(): QuestionAnswer = QuestionAnswer(question = question, answer = answer)

internal fun FormQuestionResponseDto.toDomain(): FormQuestion = FormQuestion(
    id = id,
    text = text,
    type = questionTypeOf(type),
    options = options,
    required = required,
)

internal fun ApplicantResponseDto.toDomain(): EventApplicant = EventApplicant(
    id = id,
    name = name,
    initials = initials,
    avatarTint = avatarTint,
    status = applicantStatusOf(status),
    appliedLabel = appliedLabel.ifBlank { "방금 전" },
    answers = answers.map { it.toDomain() },
    imageUrl = imageUrl,
)

internal fun EventResponseDto.toDomain(): EventInfo = EventInfo(
    capacity = capacity,
    appliedCount = appliedCount,
    deadlineDate = parseDate(deadlineDate),
    deadlineLabel = deadlineLabel,
    status = eventStatusOf(status),
    dday = dday,
    meta = meta,
    form = form.map { it.toDomain() },
    applicants = applicants.map { it.toDomain() },
    appliedByMe = appliedByMe,
    isMine = isMine,
)

internal fun ScheduleResponseDto.toDomain(): Schedule = Schedule(
    id = id,
    type = scheduleTypeOf(type),
    title = title,
    date = parseDate(date),
    timeLabel = timeLabel,
    startHour = startHour,
    startMinute = startMinute,
    endLabel = endLabel,
    endDate = endDate?.let { parseDate(it) },
    endHour = endHour,
    endMinute = endMinute,
    location = location,
    memo = memo,
    accent = scheduleAccentOf(accent),
    addedToMyCalendar = addedToMyCalendar,
    hostName = hostName,
    createdAt = createdAt,
    event = event?.toDomain(),
)

internal fun MyApplicationResponseDto.toDomain(): MyApplication = MyApplication(
    eventId = eventId,
    title = title,
    dateLabel = dateLabel,
    status = applicationStatusOf(status),
    answers = answers.map { it.toDomain() },
)

// ── 클라 → 요청 ──
internal fun ScheduleDraft.toSaveRequest(): SaveScheduleRequestDto = SaveScheduleRequestDto(
    title = title,
    startDate = startDate?.toString() ?: "",
    startHour = startHour,
    startMinute = startMinute,
    hasEnd = hasEnd,
    endDate = endDate?.toString(),
    endHour = endHour,
    endMinute = endMinute,
    location = location,
    memo = memo,
    isEvent = isEvent,
    capacity = capacity.toIntOrNull(),
    deadlineDate = deadlineDate?.toString(),
    deadlineHour = deadlineHour,
    deadlineMinute = deadlineMinute,
    form = form.map {
        FormQuestionInputDto(text = it.text, type = it.type.name, options = it.options, required = it.required)
    },
)

internal fun List<QuestionAnswer>.toAnswersRequest(): ApplicationAnswersRequestDto =
    ApplicationAnswersRequestDto(answers = map { QuestionAnswerInputDto(question = it.question, answer = it.answer) })
