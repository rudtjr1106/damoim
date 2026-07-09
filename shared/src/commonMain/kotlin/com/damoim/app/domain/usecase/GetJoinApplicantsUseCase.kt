package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.JoinApplicant
import com.damoim.app.domain.repository.ClubRepository

/** 가입 신청자 목록 (화면 09). */
class GetJoinApplicantsUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(): DataResult<List<JoinApplicant>> = clubRepository.getJoinApplicants()
}
