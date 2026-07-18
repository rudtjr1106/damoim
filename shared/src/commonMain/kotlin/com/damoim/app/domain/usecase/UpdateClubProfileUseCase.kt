package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.Member
import com.damoim.app.domain.repository.ClubRepository

/** 44 동아리별 프로필 수정 — 현재 동아리의 표시 이름을 바꾼다(빈값=전역 닉네임으로 복귀). */
class UpdateClubProfileUseCase(private val repo: ClubRepository) {
    suspend operator fun invoke(displayName: String): DataResult<Member> = repo.updateMyClubProfile(displayName)
}
