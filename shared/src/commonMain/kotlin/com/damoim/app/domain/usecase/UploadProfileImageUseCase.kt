package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.repository.AuthRepository

/**
 * 프로필 사진 바이트를 S3에 업로드하고 storageKey를 반환한다(이후 [UpdateProfileUseCase]에 전달).
 * 화면 31/45의 사진 선택 → 저장 흐름에서 사용.
 */
class UploadProfileImageUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(bytes: ByteArray, contentType: String?): DataResult<String> =
        authRepository.uploadProfileImage(bytes, contentType)
}
