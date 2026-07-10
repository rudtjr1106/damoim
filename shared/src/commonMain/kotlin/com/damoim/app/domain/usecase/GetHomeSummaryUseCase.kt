package com.damoim.app.domain.usecase

import com.damoim.app.domain.model.HomeSummary
import com.damoim.app.domain.repository.ClubRepository
import kotlinx.coroutines.flow.Flow

/** 홈 요약 관찰 (화면 05/06). 신청 승인·글 작성 등이 실시간 반영된다. */
class GetHomeSummaryUseCase(private val clubRepository: ClubRepository) {
    operator fun invoke(): Flow<HomeSummary?> = clubRepository.observeHomeSummary()
}
