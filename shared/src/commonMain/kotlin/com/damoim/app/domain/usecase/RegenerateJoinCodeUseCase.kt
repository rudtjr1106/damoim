package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.repository.ClubRepository

/** 가입 코드 재발급 (화면 08). */
class RegenerateJoinCodeUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(): DataResult<String> = clubRepository.regenerateJoinCode()
}
