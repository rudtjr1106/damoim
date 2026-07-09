package com.damoim.app.data.repository

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.data.mock.MockData
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.HomeSummary
import com.damoim.app.domain.model.JoinApplicant
import com.damoim.app.domain.model.JoinRequestResult
import com.damoim.app.domain.repository.ClubRepository
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * [ClubRepository]의 Mock 구현. 코드값에 따라 접수/거절/무효를 돌려주고,
 * 홈·설정·신청 관리 데이터는 MockData에서 가져온다.
 */
class MockClubRepository : ClubRepository {

    override suspend fun submitJoinCode(code: String): DataResult<JoinRequestResult> {
        delay(NETWORK_DELAY_MS)
        val result = MockData.joinResultForCode(code)
            ?: return DataResult.Failure(DataError(code = "INVALID_CODE", message = "존재하지 않거나 만료된 코드예요"))
        return DataResult.Success(result)
    }

    override suspend fun createClub(name: String, intro: String, category: String): DataResult<Club> {
        delay(NETWORK_DELAY_MS)
        return DataResult.Success(
            Club(
                id = Random.nextLong(1000, 9999),
                name = name,
                category = category,
                description = intro,
                memberCount = 1,
                joinCode = randomCode(),
            ),
        )
    }

    override suspend fun getHomeSummary(role: ClubRole): DataResult<HomeSummary> {
        delay(NETWORK_DELAY_MS)
        return DataResult.Success(MockData.homeSummary(role))
    }

    override suspend fun getClubInfo(): DataResult<Club> {
        delay(NETWORK_DELAY_MS)
        return DataResult.Success(MockData.myClub)
    }

    override suspend fun regenerateJoinCode(): DataResult<String> {
        delay(NETWORK_DELAY_MS)
        return DataResult.Success(randomCode())
    }

    override suspend fun getJoinApplicants(): DataResult<List<JoinApplicant>> {
        delay(NETWORK_DELAY_MS)
        return DataResult.Success(MockData.applicants)
    }

    override suspend fun decideApplicant(applicantId: Long, approve: Boolean): DataResult<Unit> {
        delay(NETWORK_DELAY_MS)
        return DataResult.Success(Unit)
    }

    private fun randomCode(): String =
        (1..6).map { CODE_CHARS.random() }.joinToString("")

    private companion object {
        const val NETWORK_DELAY_MS = 500L
        const val CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}
