package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.AuthUser
import com.damoim.app.domain.repository.AuthRepository

/**
 * 프로필 설정 (화면 31). 이름/연락처 유효성을 검증한 뒤 저장한다.
 */
class UpdateProfileUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        nickname: String,
        contact: String,
        profileImageUrl: String? = null,
        profileImageKey: String? = null,
    ): DataResult<AuthUser> {
        val trimmedName = nickname.trim()
        val trimmedContact = contact.trim()
        return when {
            trimmedName.isEmpty() ->
                DataResult.Failure(DataError(code = "EMPTY_NICKNAME", message = "이름을 입력해주세요"))
            trimmedName.length > MAX_NICKNAME_LENGTH ->
                DataResult.Failure(DataError(code = "TOO_LONG", message = "이름은 ${MAX_NICKNAME_LENGTH}자 이내로 입력해주세요"))
            trimmedContact.isEmpty() ->
                DataResult.Failure(DataError(code = "EMPTY_CONTACT", message = "연락처를 입력해주세요"))
            else -> authRepository.updateProfile(trimmedName, trimmedContact, profileImageUrl, profileImageKey)
        }
    }

    companion object {
        const val MAX_NICKNAME_LENGTH = 10
    }
}
