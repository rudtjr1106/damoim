package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.JoinRequestResult
import com.damoim.app.domain.repository.ClubRepository

/**
 * 가입 코드 제출 (화면 03). 6자리 형식 검증 후 레포지토리에 접수한다.
 */
class SubmitJoinCodeUseCase(
    private val clubRepository: ClubRepository,
) {
    suspend operator fun invoke(code: String): DataResult<JoinRequestResult> {
        val normalized = code.trim().uppercase()
        // 영문/숫자만 허용 — isLetterOrDigit()은 한글도 통과하므로 ASCII 영숫자로 검증한다.
        if (normalized.length != CODE_LENGTH || !normalized.all { it in 'A'..'Z' || it in '0'..'9' }) {
            return DataResult.Failure(
                DataError(code = "INVALID_FORMAT", message = "${CODE_LENGTH}자리 코드를 정확히 입력해주세요"),
            )
        }
        return clubRepository.submitJoinCode(normalized)
    }

    companion object {
        const val CODE_LENGTH = 6
    }
}
