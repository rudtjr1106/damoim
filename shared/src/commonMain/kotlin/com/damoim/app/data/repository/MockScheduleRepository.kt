package com.damoim.app.data.repository

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.data.mock.MockStore
import com.damoim.app.domain.model.MyApplication
import com.damoim.app.domain.model.QuestionAnswer
import com.damoim.app.domain.model.Schedule
import com.damoim.app.domain.model.ScheduleDraft
import com.damoim.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/**
 * [ScheduleRepository]의 Mock 구현 — [MockStore]에 위임. 서버 도입 시 Ktor 구현으로 교체.
 */
class MockScheduleRepository : ScheduleRepository {

    override fun observeSchedules(): Flow<List<Schedule>> = MockStore.schedulesFlow()

    override fun observeScheduleDetail(scheduleId: Long): Flow<Schedule?> =
        MockStore.scheduleDetailFlow(scheduleId)

    override fun observeMyApplications(): Flow<List<MyApplication>> = MockStore.myApplicationsFlow()

    override suspend fun createSchedule(draft: ScheduleDraft): DataResult<Long> {
        delay(WRITE_DELAY_MS)
        return DataResult.Success(MockStore.createSchedule(draft))
    }

    override suspend fun updateSchedule(draft: ScheduleDraft): DataResult<Long> {
        delay(WRITE_DELAY_MS)
        return DataResult.Success(MockStore.updateSchedule(draft))
    }

    override suspend fun deleteSchedule(scheduleId: Long): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.deleteSchedule(scheduleId)
        return DataResult.Success(Unit)
    }

    override suspend fun applyToEvent(scheduleId: Long, answers: List<QuestionAnswer>): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        return if (MockStore.applyToEvent(scheduleId, answers)) DataResult.Success(Unit)
        else DataResult.Failure(DataError(message = "신청할 수 없는 이벤트예요"))
    }

    override suspend fun updateMyApplication(scheduleId: Long, answers: List<QuestionAnswer>): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.updateMyApplication(scheduleId, answers)
        return DataResult.Success(Unit)
    }

    override suspend fun cancelMyApplication(eventId: Long): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.cancelMyApplication(eventId)
        return DataResult.Success(Unit)
    }

    override suspend fun closeEventEarly(scheduleId: Long): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.closeEventEarly(scheduleId)
        return DataResult.Success(Unit)
    }

    override suspend fun toggleMyCalendar(scheduleId: Long): DataResult<Boolean> {
        return DataResult.Success(MockStore.toggleScheduleCalendar(scheduleId))
    }

    override suspend fun announceEvent(scheduleId: Long): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.announceEvent(scheduleId)
        return DataResult.Success(Unit)
    }

    override fun currentDraft(): ScheduleDraft? = MockStore.currentScheduleDraft()
    override fun saveDraft(draft: ScheduleDraft) = MockStore.saveScheduleDraft(draft)
    override fun clearDraft() = MockStore.clearScheduleDraft()

    private companion object {
        const val WRITE_DELAY_MS = 350L
    }
}
