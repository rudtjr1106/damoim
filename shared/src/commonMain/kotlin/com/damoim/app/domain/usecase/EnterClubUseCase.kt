package com.damoim.app.domain.usecase

import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.repository.ClubRepository

/** 온보딩 완료 후 동아리 입장(04 확인 → MEMBER). 생성(07) 경로는 이미 세션이 있어 무시된다. */
class EnterClubUseCase(private val clubRepository: ClubRepository) {
    operator fun invoke(role: ClubRole) = clubRepository.enterClub(role)
}
