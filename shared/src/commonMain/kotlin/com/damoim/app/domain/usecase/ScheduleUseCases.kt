package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.MyApplication
import com.damoim.app.domain.model.QuestionAnswer
import com.damoim.app.domain.model.Schedule
import com.damoim.app.domain.model.ScheduleDraft
import com.damoim.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow

/**
 * F 그룹(일정/이벤트) 유스케이스 묶음. 응집도가 높아 한 파일에 둔다([MemberUseCases]와 동일 방침).
 */

/** 21 캘린더 · 22 목록 일정 목록. */
class GetSchedulesUseCase(private val repo: ScheduleRepository) {
    operator fun invoke(): Flow<List<Schedule>> = repo.observeSchedules()
}

/** 24 이벤트 상세. */
class GetScheduleDetailUseCase(private val repo: ScheduleRepository) {
    operator fun invoke(scheduleId: Long): Flow<Schedule?> = repo.observeScheduleDetail(scheduleId)
}

/** 48 내 신청 내역. */
class GetMyApplicationsUseCase(private val repo: ScheduleRepository) {
    operator fun invoke(): Flow<List<MyApplication>> = repo.observeMyApplications()
}

/** 23 등록/수정 + 진행 중 초안 공유(46 양식편집). */
class SubmitScheduleUseCase(private val repo: ScheduleRepository) {
    suspend fun create(draft: ScheduleDraft): DataResult<Long> = repo.createSchedule(draft)
    suspend fun update(draft: ScheduleDraft): DataResult<Long> = repo.updateSchedule(draft)
    fun currentDraft(): ScheduleDraft? = repo.currentDraft()
    fun saveDraft(draft: ScheduleDraft) = repo.saveDraft(draft)
    fun clearDraft() = repo.clearDraft()
}

/** 47/62/63 이벤트 운영 액션 — 삭제·조기마감·공지·내 일정 토글. */
class ScheduleActionUseCase(private val repo: ScheduleRepository) {
    suspend fun delete(scheduleId: Long): DataResult<Unit> = repo.deleteSchedule(scheduleId)
    suspend fun closeEarly(scheduleId: Long): DataResult<Unit> = repo.closeEventEarly(scheduleId)
    suspend fun announce(scheduleId: Long): DataResult<Unit> = repo.announceEvent(scheduleId)
    suspend fun toggleMyCalendar(scheduleId: Long): DataResult<Boolean> = repo.toggleMyCalendar(scheduleId)
}

/** 25 참여 신청 · 48 응답 수정/취소. */
class EventApplicationUseCase(private val repo: ScheduleRepository) {
    suspend fun apply(scheduleId: Long, answers: List<QuestionAnswer>): DataResult<Unit> = repo.applyToEvent(scheduleId, answers)
    suspend fun updateAnswers(scheduleId: Long, answers: List<QuestionAnswer>): DataResult<Unit> = repo.updateMyApplication(scheduleId, answers)
    suspend fun cancel(eventId: Long): DataResult<Unit> = repo.cancelMyApplication(eventId)
}
