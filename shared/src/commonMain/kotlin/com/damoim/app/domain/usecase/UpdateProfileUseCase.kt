package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.AuthUser
import com.damoim.app.domain.repository.AuthRepository

/**
 * 프로필 설정 (화면 31). 닉네임 유효성(공백/길이)을 여기서 검증한 뒤 저장한다.
 */
class UpdateProfileUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        nickname: String,
        profileImageUrl: String? = null,
    ): DataResult<AuthUser> {
        val trimmed = nickname.trim()
        return when {
            trimmed.isEmpty() ->
                DataResult.Failure(DataError(code = "EMPTY_NICKNAME", message = "이름을 입력해주세요"))
            trimmed.length > MAX_NICKNAME_LENGTH ->
                DataResult.Failure(DataError(code = "TOO_LONG", message = "이름은 ${MAX_NICKNAME_LENGTH}자 이내로 입력해주세요"))
            else -> authRepository.updateProfile(trimmed, profileImageUrl)
        }
    }

    companion object {
        const val MAX_NICKNAME_LENGTH = 12
    }
}
