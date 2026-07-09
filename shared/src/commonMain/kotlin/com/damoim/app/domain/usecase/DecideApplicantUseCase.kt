package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.repository.ClubRepository

/** 가입 신청 승인/거절 (화면 09). */
class DecideApplicantUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(applicantId: Long, approve: Boolean): DataResult<Unit> =
        clubRepository.decideApplicant(applicantId, approve)
}
