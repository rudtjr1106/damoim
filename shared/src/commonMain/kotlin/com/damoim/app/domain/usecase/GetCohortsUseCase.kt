package com.damoim.app.domain.usecase

import com.damoim.app.domain.model.Cohort
import com.damoim.app.domain.repository.ClubRepository
import kotlinx.coroutines.flow.Flow

/** 동아리 기수 목록 (69 공개 범위 '특정 기수만' 선택 · 추후 19/42 기수 관리). */
class GetCohortsUseCase(private val clubRepository: ClubRepository) {
    operator fun invoke(): Flow<List<Cohort>> = clubRepository.observeCohorts()
}
