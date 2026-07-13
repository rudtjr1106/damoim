package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.repository.ClubRepository

/**
 * 08 동아리 대표 이미지를 S3에 업로드하고 storageKey를 반환한다(이후 [UpdateClubUseCase]에 전달).
 */
class UploadClubImageUseCase(
    private val clubRepository: ClubRepository,
) {
    suspend operator fun invoke(bytes: ByteArray, contentType: String?): DataResult<String> =
        clubRepository.uploadClubImage(bytes, contentType)
}

/**
 * 08 동아리 정보 수정 — null 필드는 변경하지 않는다(부분 수정).
 */
class UpdateClubUseCase(
    private val clubRepository: ClubRepository,
) {
    suspend operator fun invoke(name: String? = null, intro: String? = null, imageKey: String? = null): DataResult<Club> =
        clubRepository.updateClub(name, intro, imageKey)
}
