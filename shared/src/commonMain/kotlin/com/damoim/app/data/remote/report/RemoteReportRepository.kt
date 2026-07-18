package com.damoim.app.data.remote.report

import com.damoim.app.core.result.DataResult
import com.damoim.app.core.result.getOrNull
import com.damoim.app.data.remote.core.ApiClient
import com.damoim.app.data.remote.core.ApiRoutes
import com.damoim.app.data.remote.core.DataTopic
import com.damoim.app.data.remote.core.RemoteBus
import com.damoim.app.data.remote.core.SharedFlows
import com.damoim.app.data.remote.core.reactiveFlow
import com.damoim.app.domain.model.ClubReport
import com.damoim.app.domain.model.MyReport
import com.damoim.app.domain.model.ReportReason
import com.damoim.app.domain.model.ReportTargetType
import com.damoim.app.domain.repository.ReportRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow

/** [ReportRepository]의 서버 구현. 신고 접수/조회는 SETTINGS 토픽으로 무효화해 '신고한 사용자' 목록을 갱신한다. */
class RemoteReportRepository(private val api: ApiClient) : ReportRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val shared = SharedFlows(scope)

    override suspend fun submit(targetType: ReportTargetType, targetId: Long, reason: ReportReason): DataResult<Unit> =
        api.postUnit(
            ApiRoutes.Reports.ROOT,
            SubmitReportRequestDto(targetType = targetType.name, targetId = targetId, reason = reason.name),
        ).also { RemoteBus.invalidate(DataTopic.SETTINGS) }

    override fun observeMyReports(): Flow<List<MyReport>> = shared.get("my-reports") {
        reactiveFlow(DataTopic.SETTINGS, fallback = emptyList()) {
            api.getData<List<MyReportResponseDto>>(ApiRoutes.Reports.ME).getOrNull()?.map { it.toDomain() }
                ?: emptyList()
        }
    }

    override fun observeClubReports(): Flow<List<ClubReport>> = shared.get("club-reports") {
        reactiveFlow(DataTopic.SETTINGS, fallback = emptyList()) {
            api.getData<List<ClubReportResponseDto>>(ApiRoutes.Reports.CLUB).getOrNull()?.map { it.toDomain() }
                ?: emptyList()
        }
    }
}
