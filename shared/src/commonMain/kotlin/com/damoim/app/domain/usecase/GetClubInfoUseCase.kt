package com.damoim.app.domain.usecase

import com.damoim.app.domain.model.Club
import com.damoim.app.domain.repository.ClubRepository
import kotlinx.coroutines.flow.Flow

/** 내 동아리 정보 관찰 (화면 08). 코드 재발급·회원 수 변경이 실시간 반영. */
class GetClubInfoUseCase(private val clubRepository: ClubRepository) {
    operator fun invoke(): Flow<Club?> = clubRepository.observeClub()
}
