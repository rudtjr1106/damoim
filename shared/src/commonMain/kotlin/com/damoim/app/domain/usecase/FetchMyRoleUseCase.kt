package com.damoim.app.domain.usecase

import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.repository.ClubRepository

/** 시작 화면 판정용 역할 조회(동아리 없으면 null). 캐시를 우회한 일회성 조회 — 상세는 ClubRepository.fetchMyRole. */
class FetchMyRoleUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(): ClubRole? = clubRepository.fetchMyRole()
}
