package com.damoim.app.domain.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.MyReport
import com.damoim.app.domain.model.ReportReason
import com.damoim.app.domain.model.ReportTargetType
import kotlinx.coroutines.flow.Flow

/** 신고(82/34/35). 접수는 게시글/댓글 대상, 조회는 내 내역(34). 운영진 목록(35)은 확장에서 추가. */
interface ReportRepository {
    /** 82 게시글/댓글 신고 접수. */
    suspend fun submit(targetType: ReportTargetType, targetId: Long, reason: ReportReason): DataResult<Unit>

    /** 34 내가 신고한 내역(현재 동아리). */
    fun observeMyReports(): Flow<List<MyReport>>
}
