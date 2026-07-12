package com.damoim.app.data.remote.schedule

import com.damoim.app.core.result.DataResult
import com.damoim.app.core.result.getOrNull
import com.damoim.app.data.remote.core.ApiClient
import com.damoim.app.data.remote.core.ApiRoutes
import com.damoim.app.data.remote.core.RemoteBus
import com.damoim.app.data.remote.core.reactiveFlow
import com.damoim.app.domain.model.MyApplication
import com.damoim.app.domain.model.QuestionAnswer
import com.damoim.app.domain.model.Schedule
import com.damoim.app.domain.model.ScheduleDraft
import com.damoim.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow

/**
 * [ScheduleRepository]의 서버 구현 (F 일정/이벤트).
 *
 * 초안(23↔46 공유)은 순수 클라 로컬 상태 — 네트워크 없이 [draft] 홀더로 관리.
 * 폼 응답은 질문 텍스트를 키로 매칭한다(QuestionAnswer.question == FormQuestion.text 계약).
 */
class RemoteScheduleRepository(private val api: ApiClient) : ScheduleRepository {

    override fun observeSchedules(): Flow<List<Schedule>> = reactiveFlow(emptyList()) {
        api.getData<List<ScheduleResponseDto>>(ApiRoutes.Schedules.ROOT).getOrNull()?.map { it.toDomain() }
            ?: emptyList()
    }

    override fun observeScheduleDetail(scheduleId: Long): Flow<Schedule?> = reactiveFlow<Schedule?>(null) {
        api.getData<ScheduleResponseDto>(ApiRoutes.Schedules.detail(scheduleId)).getOrNull()?.toDomain()
    }

    override fun observeMyApplications(): Flow<List<MyApplication>> = reactiveFlow(emptyList()) {
        api.getData<List<MyApplicationResponseDto>>(ApiRoutes.Schedules.MY_APPLICATIONS).getOrNull()
            ?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun createSchedule(draft: ScheduleDraft): DataResult<Long> =
        api.postData<Long>(ApiRoutes.Schedules.ROOT, draft.toSaveRequest()).also { RemoteBus.invalidate() }

    override suspend fun updateSchedule(draft: ScheduleDraft): DataResult<Long> {
        val editId = draft.editId ?: return createSchedule(draft) // Mock 폴백과 동일(신규로 처리)
        return api.patchData<Long>(ApiRoutes.Schedules.detail(editId), draft.toSaveRequest())
            .also { RemoteBus.invalidate() }
    }

    override suspend fun deleteSchedule(scheduleId: Long): DataResult<Unit> =
        api.deleteUnit(ApiRoutes.Schedules.detail(scheduleId)).also { RemoteBus.invalidate() }

    override suspend fun applyToEvent(scheduleId: Long, answers: List<QuestionAnswer>): DataResult<Unit> =
        api.postUnit(ApiRoutes.Schedules.apply(scheduleId), answers.toAnswersRequest())
            .also { RemoteBus.invalidate() }

    override suspend fun updateMyApplication(
        scheduleId: Long,
        answers: List<QuestionAnswer>,
    ): DataResult<Unit> =
        api.patchUnit(ApiRoutes.Schedules.myApplication(scheduleId), answers.toAnswersRequest())
            .also { RemoteBus.invalidate() }

    override suspend fun cancelMyApplication(eventId: Long): DataResult<Unit> =
        // 인자명은 eventId지만 서버 경로는 scheduleId다(계약).
        api.deleteUnit(ApiRoutes.Schedules.myApplication(eventId)).also { RemoteBus.invalidate() }

    override suspend fun closeEventEarly(scheduleId: Long): DataResult<Unit> =
        api.postUnit(ApiRoutes.Schedules.close(scheduleId)).also { RemoteBus.invalidate() }

    override suspend fun toggleMyCalendar(scheduleId: Long): DataResult<Boolean> =
        api.postData<Boolean>(ApiRoutes.Schedules.calendarToggle(scheduleId)).also { RemoteBus.invalidate() }

    override suspend fun announceEvent(scheduleId: Long): DataResult<Unit> =
        api.postUnit(ApiRoutes.Schedules.announce(scheduleId)).also { RemoteBus.invalidate() }

    // ── 진행 중 등록 초안(클라 로컬, 동기) ──
    private var draft: ScheduleDraft? = null
    override fun currentDraft(): ScheduleDraft? = draft
    override fun saveDraft(draft: ScheduleDraft) { this.draft = draft }
    override fun clearDraft() { draft = null }
}
