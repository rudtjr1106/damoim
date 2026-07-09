package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.repository.ClubRepository

/** 내 동아리 정보 조회 (화면 08). */
class GetClubInfoUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(): DataResult<Club> = clubRepository.getClubInfo()
}
