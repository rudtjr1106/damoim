package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.repository.ClubRepository

/** 가입 코드 비활성화 (화면 08). */
class DisableJoinCodeUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(): DataResult<Unit> = clubRepository.disableJoinCode()
}
