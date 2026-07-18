package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.MyReport
import com.damoim.app.domain.model.ReportReason
import com.damoim.app.domain.model.ReportTargetType
import com.damoim.app.domain.repository.ReportRepository
import kotlinx.coroutines.flow.Flow

/** 82 게시글/댓글 신고 접수. */
class SubmitReportUseCase(private val repo: ReportRepository) {
    suspend operator fun invoke(targetType: ReportTargetType, targetId: Long, reason: ReportReason): DataResult<Unit> =
        repo.submit(targetType, targetId, reason)
}

/** 34 내가 신고한 내역. */
class MyReportsUseCase(private val repo: ReportRepository) {
    fun observe(): Flow<List<MyReport>> = repo.observeMyReports()
}
