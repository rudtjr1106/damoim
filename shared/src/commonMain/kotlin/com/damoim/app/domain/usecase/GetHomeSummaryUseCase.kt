package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.HomeSummary
import com.damoim.app.domain.repository.ClubRepository

/** 홈 요약 조회 (화면 05/06). */
class GetHomeSummaryUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(role: ClubRole): DataResult<HomeSummary> =
        clubRepository.getHomeSummary(role)
}
