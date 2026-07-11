package com.damoim.app.domain.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.MyApplication
import com.damoim.app.domain.model.QuestionAnswer
import com.damoim.app.domain.model.Schedule
import com.damoim.app.domain.model.ScheduleDraft
import kotlinx.coroutines.flow.Flow

/** 일정/이벤트(F 그룹) 저장소. 조회는 Flow, 변경은 suspend + DataResult. */
interface ScheduleRepository {
    fun observeSchedules(): Flow<List<Schedule>>
    fun observeScheduleDetail(scheduleId: Long): Flow<Schedule?>
    fun observeMyApplications(): Flow<List<MyApplication>>

    suspend fun createSchedule(draft: ScheduleDraft): DataResult<Long>
    suspend fun updateSchedule(draft: ScheduleDraft): DataResult<Long>
    suspend fun deleteSchedule(scheduleId: Long): DataResult<Unit>

    /** 25 참여 신청 — 폼 응답과 함께. */
    suspend fun applyToEvent(scheduleId: Long, answers: List<QuestionAnswer>): DataResult<Unit>
    /** 48 응답 수정 — 재신청 없이 답변만 교체. */
    suspend fun updateMyApplication(scheduleId: Long, answers: List<QuestionAnswer>): DataResult<Unit>
    /** 48 신청 취소. */
    suspend fun cancelMyApplication(eventId: Long): DataResult<Unit>
    /** 47/62 신청 조기 마감. */
    suspend fun closeEventEarly(scheduleId: Long): DataResult<Unit>
    /** 21/22 '내 일정에 추가' 토글 — 새 상태 반환. */
    suspend fun toggleMyCalendar(scheduleId: Long): DataResult<Boolean>
    /** G5 이벤트를 게시판 공지로 등록. */
    suspend fun announceEvent(scheduleId: Long): DataResult<Unit>

    // 진행 중 등록 초안(23 ↔ 46 양식편집 공유). 동기 상태.
    fun currentDraft(): ScheduleDraft?
    fun saveDraft(draft: ScheduleDraft)
    fun clearDraft()
}
