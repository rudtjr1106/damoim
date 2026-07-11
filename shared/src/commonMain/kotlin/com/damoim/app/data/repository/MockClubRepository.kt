package com.damoim.app.data.repository

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.data.mock.MockData
import com.damoim.app.data.mock.MockStore
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.model.ClubMembership
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.Cohort
import com.damoim.app.domain.model.HomeSummary
import com.damoim.app.domain.model.ApplicantsBoard
import com.damoim.app.domain.model.JoinRequestResult
import com.damoim.app.domain.model.Member
import com.damoim.app.domain.model.MemberDetail
import com.damoim.app.domain.model.MemberRole
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

    override fun observeCohorts(): Flow<List<Cohort>> = MockStore.cohortsFlow()

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

    // ── E. 회원·기수 관리 ──

    override fun observeMembers(): Flow<List<Member>> = MockStore.membersFlow()

    override fun observeMember(memberId: Long): Flow<MemberDetail?> = MockStore.memberDetailFlow(memberId)

    override fun observeMyMember(): Flow<Member?> = MockStore.myMemberFlow()

    override fun observeJoinedClubs(): Flow<List<ClubMembership>> = MockStore.joinedClubsFlow()

    override suspend fun changeMemberCohort(memberId: Long, cohortId: Long): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.changeMemberCohort(memberId, cohortId)
        return DataResult.Success(Unit)
    }

    override suspend fun changeMemberRole(memberId: Long, role: MemberRole): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.changeMemberRole(memberId, role)
        return DataResult.Success(Unit)
    }

    override suspend fun removeMember(memberId: Long): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.removeMember(memberId)
        return DataResult.Success(Unit)
    }

    override suspend fun addCohort(shortLabel: String, displayName: String): DataResult<Cohort> {
        delay(WRITE_DELAY_MS)
        return DataResult.Success(MockStore.addCohort(shortLabel, displayName))
    }

    override suspend fun renameCohort(cohortId: Long, shortLabel: String, displayName: String): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.renameCohort(cohortId, shortLabel, displayName)
        return DataResult.Success(Unit)
    }

    override fun switchClub(clubId: Long) = MockStore.switchClub(clubId)

    override fun leaveClub() = MockStore.leaveClub()

    private companion object {
        const val NETWORK_DELAY_MS = 500L
        const val WRITE_DELAY_MS = 300L
    }
}
