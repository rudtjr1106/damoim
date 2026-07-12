package com.damoim.app.data.remote.club

import com.damoim.app.core.result.DataResult
import com.damoim.app.core.result.getOrNull
import com.damoim.app.core.result.map
import com.damoim.app.data.remote.core.ApiClient
import com.damoim.app.data.remote.core.ApiRoutes
import com.damoim.app.data.remote.core.RemoteBus
import com.damoim.app.data.remote.core.reactiveFlow
import com.damoim.app.domain.model.ApplicantsBoard
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.model.ClubMembership
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.Cohort
import com.damoim.app.domain.model.HomeSummary
import com.damoim.app.domain.model.JoinRequestResult
import com.damoim.app.domain.model.Member
import com.damoim.app.domain.model.MemberDetail
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.domain.repository.ClubRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * [ClubRepository]의 서버 구현 (B 동아리/홈 + E 회원/기수/멀티동아리).
 *
 * - observe*(): [reactiveFlow]로 구독/변경 시 재조회. 활성 동아리는 서버가 JWT로 해석(clubId 미전송, IDOR 차단).
 * - 동기 세션 함수(enterClub/switchClub/leaveClub)는 [scope]에서 fire-and-forget 후 [RemoteBus.invalidate].
 */
class RemoteClubRepository(private val api: ApiClient) : ClubRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // ── B: 가입/생성/홈/코드/신청 ──

    override suspend fun submitJoinCode(code: String): DataResult<JoinRequestResult> =
        api.postData<JoinResultResponseDto>(ApiRoutes.Clubs.JOIN, JoinCodeRequestDto(code))
            .map { it.toDomain() }
            .also { RemoteBus.invalidate() }

    override suspend fun createClub(name: String, intro: String, category: String): DataResult<Club> =
        api.postData<ClubResponseDto>(ApiRoutes.Clubs.ROOT, CreateClubRequestDto(name, category, intro))
            .map { it.toDomain() }
            .also { RemoteBus.invalidate() }

    override fun enterClub(role: ClubRole) {
        // 서버가 활성 동아리를 이미 보유(생성=LEADER 지정). 새로고침만 트리거.
        RemoteBus.invalidate()
    }

    override fun observeRole(): Flow<ClubRole?> = reactiveFlow<ClubRole?>(null) {
        api.getData<MemberResponseDto>(ApiRoutes.Members.ME).getOrNull()?.let { clubRole(it.role) }
    }

    override fun observeHomeSummary(): Flow<HomeSummary?> = reactiveFlow<HomeSummary?>(null) {
        api.getData<HomeSummaryResponseDto>(ApiRoutes.Clubs.ME_HOME).getOrNull()?.toDomain()
    }

    override fun observeClub(): Flow<Club?> = reactiveFlow<Club?>(null) {
        api.getData<ClubResponseDto>(ApiRoutes.Clubs.ME).getOrNull()?.toDomain()
    }

    override fun observeCohorts(): Flow<List<Cohort>> = reactiveFlow(emptyList()) {
        api.getData<List<CohortResponseDto>>(ApiRoutes.Clubs.ME_COHORTS).getOrNull()?.map { it.toDomain() }
            ?: emptyList()
    }

    override suspend fun regenerateJoinCode(): DataResult<String> =
        api.postData<JoinCodeResponseDto>(ApiRoutes.Clubs.JOIN_CODE_REGENERATE)
            .map { it.joinCode ?: "" }
            .also { RemoteBus.invalidate() }

    override suspend fun disableJoinCode(): DataResult<Unit> =
        api.postUnit(ApiRoutes.Clubs.JOIN_CODE_DISABLE).also { RemoteBus.invalidate() }

    override fun observeApplicants(): Flow<ApplicantsBoard> =
        reactiveFlow(ApplicantsBoard(emptyList(), emptyList())) {
            api.getData<ApplicantsBoardResponseDto>(ApiRoutes.Clubs.ME_APPLICANTS).getOrNull()?.toDomain()
                ?: ApplicantsBoard(emptyList(), emptyList())
        }

    override suspend fun decideApplicant(applicantId: Long, approve: Boolean): DataResult<Unit> =
        api.postUnit(ApiRoutes.Clubs.decide(applicantId), DecideRequestDto(approve))
            .also { RemoteBus.invalidate() }

    // ── E: 회원/기수/멀티동아리 ──

    override fun observeMembers(): Flow<List<Member>> = reactiveFlow(emptyList()) {
        api.getData<List<MemberResponseDto>>(ApiRoutes.Members.ROOT).getOrNull()?.map { it.toDomain() }
            ?: emptyList()
    }

    override fun observeMember(memberId: Long): Flow<MemberDetail?> = reactiveFlow<MemberDetail?>(null) {
        api.getData<MemberDetailResponseDto>(ApiRoutes.Members.detail(memberId)).getOrNull()?.toDomain()
    }

    override fun observeMyMember(): Flow<Member?> = reactiveFlow<Member?>(null) {
        api.getData<MemberResponseDto>(ApiRoutes.Members.ME).getOrNull()?.toDomain()
    }

    override fun observeJoinedClubs(): Flow<List<ClubMembership>> = reactiveFlow(emptyList()) {
        api.getData<List<ClubMembershipResponseDto>>(ApiRoutes.Clubs.JOINED).getOrNull()?.map { it.toDomain() }
            ?: emptyList()
    }

    override suspend fun changeMemberCohort(memberId: Long, cohortId: Long): DataResult<Unit> =
        api.postUnit(ApiRoutes.Members.cohort(memberId), ChangeCohortRequestDto(cohortId))
            .also { RemoteBus.invalidate() }

    override suspend fun changeMemberRole(memberId: Long, role: MemberRole): DataResult<Unit> =
        api.postUnit(ApiRoutes.Members.role(memberId), ChangeRoleRequestDto(role.name))
            .also { RemoteBus.invalidate() }

    override suspend fun removeMember(memberId: Long): DataResult<Unit> =
        api.deleteUnit(ApiRoutes.Members.detail(memberId)).also { RemoteBus.invalidate() }

    override suspend fun addCohort(shortLabel: String, displayName: String): DataResult<Cohort> =
        api.postData<CohortResponseDto>(ApiRoutes.Clubs.ME_COHORTS, CohortCreateRequestDto(shortLabel, displayName))
            .map { it.toDomain() }
            .also { RemoteBus.invalidate() }

    override suspend fun renameCohort(
        cohortId: Long,
        shortLabel: String,
        displayName: String,
    ): DataResult<Unit> =
        api.patchUnit(ApiRoutes.Clubs.cohort(cohortId), CohortRenameRequestDto(shortLabel, displayName))
            .also { RemoteBus.invalidate() }

    override fun switchClub(clubId: Long) {
        scope.launch {
            api.postUnit(ApiRoutes.Clubs.SWITCH, SwitchClubRequestDto(clubId))
            RemoteBus.invalidate() // 전환 완료 후 전 화면 재조회(새 활성 동아리 데이터)
        }
    }

    override suspend fun withdrawFromActiveClub(): DataResult<Boolean> {
        // 멤버십 삭제. 로그인/토큰은 유지(로그아웃과 다름). 단독 리더면 서버가 409 LEADER_MUST_DELEGATE.
        val leave = api.postUnit(ApiRoutes.Clubs.ME_LEAVE)
        if (leave is DataResult.Failure) return leave
        RemoteBus.invalidate() // 서버가 활성 동아리 재지정 → 홈/역할/명부 재조회
        // 서버 leaveClub은 Unit 반환이라, members/me 성공 여부로 잔존 활성 동아리를 판정.
        val hasActiveClub = api.getData<MemberResponseDto>(ApiRoutes.Members.ME) is DataResult.Success
        return DataResult.Success(hasActiveClub)
    }
}
