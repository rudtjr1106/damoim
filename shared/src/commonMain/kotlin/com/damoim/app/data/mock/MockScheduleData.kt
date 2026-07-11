package com.damoim.app.data.mock

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
import com.damoim.app.domain.model.ScheduleType
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus

/**
 * 데모 동아리 일정/이벤트 시드. 날짜는 seed 시점의 today 기준 상대값이라 캘린더가
 * 항상 현재 월 기준으로 살아있게 렌더된다(디자인의 6월 고정값 대신).
 */
object MockScheduleData {

    private const val WD = "월화수목금토일"
    fun weekday(d: LocalDate): String = WD[d.dayOfWeek.isoDayNumber - 1].toString()
    /** "8.14 (목)" */
    fun shortDate(d: LocalDate): String = "${d.monthNumber}.${d.dayOfMonth} (${weekday(d)})"
    /** "8월 14일 (목)" */
    fun midDate(d: LocalDate): String = "${d.monthNumber}월 ${d.dayOfMonth}일 (${weekday(d)})"
    /** "8월 14일 목요일" */
    fun longDate(d: LocalDate): String = "${d.monthNumber}월 ${d.dayOfMonth}일 ${weekday(d)}요일"

    private fun dday(target: LocalDate, today: LocalDate): String {
        val diff = target.toEpochDays() - today.toEpochDays()
        return when {
            diff > 0 -> "D-$diff"
            diff == 0L -> "D-DAY"
            else -> "종료"
        }
    }

    /** 신입 환영 MT 신청 양식(25/46/47 공유). */
    private fun mtForm(): List<FormQuestion> = listOf(
        FormQuestion(9101, "MT 참여 기간을 선택해주세요", QuestionType.SELECT, listOf("1박 2일 전체", "첫날만"), required = true),
        FormQuestion(9102, "저녁 식사 여부", QuestionType.SELECT, listOf("먹어요", "안 먹어요"), required = true),
        FormQuestion(9103, "운영진에게 전달 사항 (선택)", QuestionType.TEXT, required = false),
    )

    private fun mtApplicants(): List<EventApplicant> = listOf(
        EventApplicant(
            9201, "이서연", "서연", 0, ApplicantStatus.APPLIED, "10분 전",
            answers = listOf(QuestionAnswer("MT 참여 기간", "1박 2일 전체"), QuestionAnswer("저녁 식사", "먹어요"), QuestionAnswer("전달 사항", "매운 음식 못 먹어요!")),
        ),
        EventApplicant(
            9202, "박준혁", "준혁", 1, ApplicantStatus.APPLIED, "32분 전",
            answers = listOf(QuestionAnswer("MT 참여 기간", "1박 2일 전체"), QuestionAnswer("저녁 식사", "먹어요")),
        ),
        EventApplicant(
            9203, "정하늘", "하늘", 2, ApplicantStatus.APPLIED, "1시간 전",
            answers = listOf(QuestionAnswer("MT 참여 기간", "첫날만"), QuestionAnswer("저녁 식사", "안 먹어요"), QuestionAnswer("전달 사항", "둘째 날 오전에 빠져야 해요")),
        ),
        EventApplicant(
            9204, "강도윤", "도윤", 3, ApplicantStatus.CANCELED, "어제",
            answers = listOf(QuestionAnswer("MT 참여 기간", "1박 2일 전체"), QuestionAnswer("저녁 식사", "먹어요")),
        ),
    )

    /** 데모 동아리 일정/이벤트 목록. */
    fun seedSchedules(today: LocalDate): List<Schedule> {
        val monthly = today.plus(DatePeriod(days = 3))
        val mtDate = today.plus(DatePeriod(days = 10))
        val mtDeadline = today.plus(DatePeriod(days = 7))
        val assembly = today.plus(DatePeriod(days = 17))
        val assemblyDeadline = today.plus(DatePeriod(days = 14))
        val hiking = today.plus(DatePeriod(days = -20))   // 지난 이벤트(종료)

        return listOf(
            Schedule(
                id = 9001, type = ScheduleType.SCHEDULE, title = "정기 월례회의",
                date = monthly, timeLabel = "오전 10:00", startHour = 10, startMinute = 0, location = "동아리방",
                memo = "6월 활동 리뷰와 여름 MT 계획을 논의합니다.",
                accent = ScheduleAccent.PRIMARY, hostName = "김민준", createdAt = 1,
            ),
            Schedule(
                id = 9002, type = ScheduleType.EVENT, title = "신입 환영 MT",
                date = mtDate, timeLabel = "오전 9:00", startHour = 9, startMinute = 0, endLabel = "1박 2일",
                location = "가평 청평계곡 펜션", memo = "신입 부원 환영 MT! 1박 2일 동안 함께 즐겨요. 준비물은 개인 세면도구와 편한 옷차림입니다.",
                accent = ScheduleAccent.SKY, hostName = "김민준", createdAt = 2,
                event = EventInfo(
                    capacity = 20, appliedCount = 12,
                    deadlineDate = mtDeadline, deadlineLabel = "${shortDate(mtDeadline)} 자정",
                    status = EventStatus.OPEN, dday = dday(mtDate, today), meta = "1박 2일 · 가평",
                    form = mtForm(), applicants = mtApplicants(), appliedByMe = false,
                ),
            ),
            Schedule(
                id = 9003, type = ScheduleType.EVENT, title = "상반기 결산 총회",
                date = assembly, timeLabel = "오후 7:00", startHour = 19, startMinute = 0, location = "학생회관 대강당",
                memo = "상반기 활동을 결산하고 하반기 계획을 공유하는 정기 총회입니다.",
                accent = ScheduleAccent.PRIMARY, hostName = "김민준", createdAt = 3,
                event = EventInfo(
                    capacity = 40, appliedCount = 23,
                    deadlineDate = assemblyDeadline, deadlineLabel = "${shortDate(assemblyDeadline)} 오후 6:00",
                    status = EventStatus.OPEN, dday = dday(assembly, today), meta = "저녁 · 대강당",
                    form = listOf(
                        FormQuestion(9301, "뒤풀이 참석 여부", QuestionType.SELECT, listOf("참석", "불참"), required = true),
                    ),
                    applicants = emptyList(), appliedByMe = true,
                ),
            ),
            Schedule(
                id = 9004, type = ScheduleType.EVENT, title = "봄맞이 등산",
                date = hiking, timeLabel = "오전 8:00", startHour = 8, startMinute = 0, location = "북한산 우이역",
                memo = "봄을 맞아 함께 북한산에 올랐습니다.",
                accent = ScheduleAccent.SKY, hostName = "김민준", createdAt = 4,
                event = EventInfo(
                    capacity = 15, appliedCount = 15,
                    deadlineDate = hiking, deadlineLabel = "마감",
                    status = EventStatus.ENDED, dday = "종료", meta = "지난 이벤트",
                    form = emptyList(), applicants = emptyList(), appliedByMe = true,
                ),
            ),
        )
    }

    /** 내 신청 내역(48) 시드 — 총회(신청완료)·등산(종료). MT는 미신청(24→25 데모용). */
    fun seedMyApplications(today: LocalDate): List<MyApplication> {
        val assembly = today.plus(DatePeriod(days = 17))
        val hiking = today.plus(DatePeriod(days = -20))
        return listOf(
            MyApplication(
                eventId = 9003, title = "상반기 결산 총회", dateLabel = midDate(assembly),
                status = ApplicationStatus.APPLIED,
                answers = listOf(QuestionAnswer("뒤풀이 참석 여부", "참석")),
            ),
            MyApplication(
                eventId = 9004, title = "봄맞이 등산", dateLabel = midDate(hiking),
                status = ApplicationStatus.ENDED,
                answers = emptyList(),
            ),
        )
    }
}
