package com.damoim.app.domain.usecase

import com.damoim.app.domain.model.JoinApplicant
import com.damoim.app.domain.repository.ClubRepository
import kotlinx.coroutines.flow.Flow

/** 가입 신청 관찰 (화면 09) — (대기 목록, 처리 완료 수). */
class GetJoinApplicantsUseCase(private val clubRepository: ClubRepository) {
    operator fun invoke(): Flow<Pair<List<JoinApplicant>, Int>> = clubRepository.observeApplicants()
}
