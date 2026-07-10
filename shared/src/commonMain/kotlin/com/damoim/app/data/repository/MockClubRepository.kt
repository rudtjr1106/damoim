package com.damoim.app.data.repository

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.data.mock.MockData
import com.damoim.app.data.mock.MockStore
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.HomeSummary
import com.damoim.app.domain.model.ApplicantsBoard
import com.damoim.app.domain.model.JoinRequestResult
import com.damoim.app.domain.repository.ClubRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * [ClubRepository]의 Mock 구현 — [MockStore]에 위임. 코드값에 따라 접수/거절/무효를 돌려준다.
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
        return DataResult.Success(MockStore.createClub(name, intro, category))
    }

    override fun enterClub(role: ClubRole) = MockStore.enterClub(role)

    override fun observeRole(): Flow<ClubRole?> = MockStore.session.map { it?.role }

    override fun observeHomeSummary(): Flow<HomeSummary?> = MockStore.homeSummaryFlow()

    override fun observeClub(): Flow<Club?> = MockStore.session.map { it?.club }

    override suspend fun regenerateJoinCode(): DataResult<String> {
        delay(NETWORK_DELAY_MS)
        return DataResult.Success(MockStore.regenerateJoinCode())
    }

    override suspend fun disableJoinCode(): DataResult<Unit> {
        MockStore.disableJoinCode()
        return DataResult.Success(Unit)
    }

    override fun observeApplicants(): Flow<ApplicantsBoard> = MockStore.applicantsFlow()

    override suspend fun decideApplicant(applicantId: Long, approve: Boolean): DataResult<Unit> {
        delay(300L)
        MockStore.decideApplicant(applicantId, approve)
        return DataResult.Success(Unit)
    }

    private companion object {
        const val NETWORK_DELAY_MS = 500L
    }
}
