package com.damoim.app.domain.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.HomeSummary
import com.damoim.app.domain.model.JoinApplicant
import com.damoim.app.domain.model.JoinRequestResult

/**
 * 동아리 관련 레포지토리. 구현체는 data 계층에 있으며 현재는 Mock.
 */
interface ClubRepository {

    /** 가입 코드 제출 (화면 03). */
    suspend fun submitJoinCode(code: String): DataResult<JoinRequestResult>

    /** 새 동아리 생성 (화면 07). */
    suspend fun createClub(name: String, intro: String, category: String): DataResult<Club>

    /** 홈 요약 (화면 05/06). role별 통계·알림·미리보기 구성. */
    suspend fun getHomeSummary(role: ClubRole): DataResult<HomeSummary>

    /** 내 동아리 정보 (화면 08 설정). */
    suspend fun getClubInfo(): DataResult<Club>

    /** 가입 코드 재발급 (화면 08). 새 코드 반환. */
    suspend fun regenerateJoinCode(): DataResult<String>

    /** 가입 신청자 목록 (화면 09). */
    suspend fun getJoinApplicants(): DataResult<List<JoinApplicant>>

    /** 신청 승인/거절 (화면 09). */
    suspend fun decideApplicant(applicantId: Long, approve: Boolean): DataResult<Unit>
}
