package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.AuthUser
import com.damoim.app.domain.repository.AuthRepository

/**
 * 프로필 저장(31 설정 / 45 수정). 이름만 필수 — 연락처는 선택(서버도 빈 연락처 허용)이라
 * 사진/이름만 바꿔도 저장(=업로드한 이미지 키 반영)이 된다.
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
            else -> authRepository.updateProfile(trimmedName, trimmedContact, profileImageUrl, profileImageKey)
        }
    }

    companion object {
        const val MAX_NICKNAME_LENGTH = 10
    }
}
