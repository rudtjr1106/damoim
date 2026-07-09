package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.repository.ClubRepository

/** 동아리 생성 (화면 07). 이름 유효성 검증 후 생성. */
class CreateClubUseCase(private val clubRepository: ClubRepository) {
    suspend operator fun invoke(name: String, intro: String, category: String): DataResult<Club> {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() ->
                DataResult.Failure(DataError(code = "EMPTY_NAME", message = "동아리 이름을 입력해주세요"))
            trimmed.length > MAX_NAME_LENGTH ->
                DataResult.Failure(DataError(code = "TOO_LONG", message = "이름은 ${MAX_NAME_LENGTH}자 이내로 입력해주세요"))
            else -> clubRepository.createClub(trimmed, intro.trim(), category)
        }
    }
    companion object { const val MAX_NAME_LENGTH = 20 }
}
