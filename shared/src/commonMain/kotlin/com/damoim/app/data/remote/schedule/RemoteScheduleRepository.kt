package com.damoim.app.data.remote.schedule

import com.damoim.app.core.result.DataResult
import com.damoim.app.core.result.getOrNull
import com.damoim.app.data.remote.core.ApiClient
import com.damoim.app.data.remote.core.ApiRoutes
import com.damoim.app.data.remote.core.DataTopic
import com.damoim.app.data.remote.core.RemoteBus
import com.damoim.app.data.remote.core.reactiveFlow
import com.damoim.app.domain.model.MyApplication
import com.damoim.app.domain.model.QuestionAnswer
import com.damoim.app.domain.model.Schedule
import com.damoim.app.domain.model.ScheduleDraft
import com.damoim.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow

/**
 * [ScheduleRepository]의 서버 구현 (F 일정/이벤트). 변경은 [DataTopic.SCHEDULE]만 무효화
 * (공지 등록은 게시판 글을 만들므로 BOARD도 함께). 초안은 클라 로컬 상태.
 */
class RemoteScheduleRepository(private val api: ApiClient) : ScheduleRepository {

    override fun observeSchedules(): Flow<List<Schedule>> =
        reactiveFlow(DataTopic.SCHEDULE, fallback = emptyList()) {
            api.getData<List<ScheduleResponseDto>>(ApiRoutes.Schedules.ROOT).getOrNull()?.map { it.toDomain() }
                ?: emptyList()
        }

    override fun observeScheduleDetail(scheduleId: Long): Flow<Schedule?> =
        reactiveFlow<Schedule?>(DataTopic.SCHEDULE, fallback = null) {
            api.getData<ScheduleResponseDto>(ApiRoutes.Schedules.detail(scheduleId)).getOrNull()?.toDomain()
        }

    override fun observeMyApplications(): Flow<List<MyApplication>> =
        reactiveFlow(DataTopic.SCHEDULE, fallback = emptyList()) {
            api.getData<List<MyApplicationResponseDto>>(ApiRoutes.Schedules.MY_APPLICATIONS).getOrNull()
                ?.map { it.toDomain() } ?: emptyList()
        }

    override suspend fun createSchedule(draft: ScheduleDraft): DataResult<Long> =
        api.postData<Long>(ApiRoutes.Schedules.ROOT, draft.toSaveRequest())
            .also { RemoteBus.invalidate(DataTopic.SCHEDULE) }

    override suspend fun updateSchedule(draft: ScheduleDraft): DataResult<Long> {
        val editId = draft.editId ?: return createSchedule(draft)
        return api.patchData<Long>(ApiRoutes.Schedules.detail(editId), draft.toSaveRequest())
            .also { RemoteBus.invalidate(DataTopic.SCHEDULE) }
    }

    override suspend fun deleteSchedule(scheduleId: Long): DataResult<Unit> =
        api.deleteUnit(ApiRoutes.Schedules.detail(scheduleId)).also { RemoteBus.invalidate(DataTopic.SCHEDULE) }

    override suspend fun applyToEvent(scheduleId: Long, answers: List<QuestionAnswer>): DataResult<Unit> =
        api.postUnit(ApiRoutes.Schedules.apply(scheduleId), answers.toAnswersRequest())
            .also { RemoteBus.invalidate(DataTopic.SCHEDULE) }

    override suspend fun updateMyApplication(
        scheduleId: Long,
        answers: List<QuestionAnswer>,
    ): DataResult<Unit> =
        api.patchUnit(ApiRoutes.Schedules.myApplication(scheduleId), answers.toAnswersRequest())
            .also { RemoteBus.invalidate(DataTopic.SCHEDULE) }

    override suspend fun cancelMyApplication(eventId: Long): DataResult<Unit> =
        api.deleteUnit(ApiRoutes.Schedules.myApplication(eventId)).also { RemoteBus.invalidate(DataTopic.SCHEDULE) }

    override suspend fun closeEventEarly(scheduleId: Long): DataResult<Unit> =
        api.postUnit(ApiRoutes.Schedules.close(scheduleId)).also { RemoteBus.invalidate(DataTopic.SCHEDULE) }

    override suspend fun toggleMyCalendar(scheduleId: Long): DataResult<Boolean> =
        api.postData<Boolean>(ApiRoutes.Schedules.calendarToggle(scheduleId))
            .also { RemoteBus.invalidate(DataTopic.SCHEDULE) }

    override suspend fun announceEvent(scheduleId: Long): DataResult<Unit> =
        api.postUnit(ApiRoutes.Schedules.announce(scheduleId))
            // 게시판 공지 글을 자동 생성하므로 BOARD도 무효화.
            .also { RemoteBus.invalidate(DataTopic.SCHEDULE, DataTopic.BOARD) }

    // ── 진행 중 등록 초안(클라 로컬, 동기) ──
    private var draft: ScheduleDraft? = null
    override fun currentDraft(): ScheduleDraft? = draft
    override fun saveDraft(draft: ScheduleDraft) { this.draft = draft }
    override fun clearDraft() { draft = null }
}
