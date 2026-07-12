package com.damoim.app.data.remote.club

import com.damoim.app.core.result.DataResult
import com.damoim.app.core.result.getOrNull
import com.damoim.app.core.result.map
import com.damoim.app.data.remote.core.ApiClient
import com.damoim.app.data.remote.core.ApiRoutes
import com.damoim.app.data.remote.core.DataTopic
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
 * 변경은 영향 도메인만 무효화한다(예: 기수 변경 → MEMBER·CLUB만). 동아리 전환/탈퇴는 전체 데이터가
 * 바뀌므로 invalidateAll.
 */
class RemoteClubRepository(private val api: ApiClient) : ClubRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // ── B: 가입/생성/홈/코드/신청 ──

    override suspend fun submitJoinCode(code: String): DataResult<JoinRequestResult> =
        api.postData<JoinResultResponseDto>(ApiRoutes.Clubs.JOIN, JoinCodeRequestDto(code))
            .map { it.toDomain() }
            .also { RemoteBus.invalidate(DataTopic.CLUB) }

    override suspend fun createClub(name: String, intro: String, category: String): DataResult<Club> =
        api.postData<ClubResponseDto>(ApiRoutes.Clubs.ROOT, CreateClubRequestDto(name, category, intro))
            .map { it.toDomain() }
            .also { RemoteBus.invalidate(DataTopic.CLUB, DataTopic.MEMBER) }

    override fun enterClub(role: ClubRole) {
        RemoteBus.invalidate(DataTopic.CLUB, DataTopic.MEMBER)
    }

    override fun observeRole(): Flow<ClubRole?> =
        reactiveFlow<ClubRole?>(DataTopic.CLUB, DataTopic.MEMBER, fallback = null) {
            api.getData<MemberResponseDto>(ApiRoutes.Members.ME).getOrNull()?.let { clubRole(it.role) }
        }

    override fun observeHomeSummary(): Flow<HomeSummary?> =
        reactiveFlow<HomeSummary?>(DataTopic.CLUB, DataTopic.MEMBER, DataTopic.NOTIFICATION, fallback = null) {
            api.getData<HomeSummaryResponseDto>(ApiRoutes.Clubs.ME_HOME).getOrNull()?.toDomain()
        }

    override fun observeClub(): Flow<Club?> = reactiveFlow<Club?>(DataTopic.CLUB, fallback = null) {
        api.getData<ClubResponseDto>(ApiRoutes.Clubs.ME).getOrNull()?.toDomain()
    }

    override fun observeCohorts(): Flow<List<Cohort>> =
        reactiveFlow(DataTopic.CLUB, DataTopic.MEMBER, fallback = emptyList()) {
            api.getData<List<CohortResponseDto>>(ApiRoutes.Clubs.ME_COHORTS).getOrNull()?.map { it.toDomain() }
                ?: emptyList()
        }

    override suspend fun regenerateJoinCode(): DataResult<String> =
        api.postData<JoinCodeResponseDto>(ApiRoutes.Clubs.JOIN_CODE_REGENERATE)
            .map { it.joinCode ?: "" }
            .also { RemoteBus.invalidate(DataTopic.CLUB) }

    override suspend fun disableJoinCode(): DataResult<Unit> =
        api.postUnit(ApiRoutes.Clubs.JOIN_CODE_DISABLE).also { RemoteBus.invalidate(DataTopic.CLUB) }

    override fun observeApplicants(): Flow<ApplicantsBoard> =
        reactiveFlow(DataTopic.CLUB, fallback = ApplicantsBoard(emptyList(), emptyList())) {
            api.getData<ApplicantsBoardResponseDto>(ApiRoutes.Clubs.ME_APPLICANTS).getOrNull()?.toDomain()
                ?: ApplicantsBoard(emptyList(), emptyList())
        }

    override suspend fun decideApplicant(applicantId: Long, approve: Boolean): DataResult<Unit> =
        api.postUnit(ApiRoutes.Clubs.decide(applicantId), DecideRequestDto(approve))
            // 승인 = 신청목록·회원목록·홈 통계 변경 + 승인 알림 생성.
            .also { RemoteBus.invalidate(DataTopic.CLUB, DataTopic.MEMBER, DataTopic.NOTIFICATION) }

    // ── E: 회원/기수/멀티동아리 ──

    override fun observeMembers(): Flow<List<Member>> =
        reactiveFlow(DataTopic.MEMBER, DataTopic.CLUB, fallback = emptyList()) {
            api.getData<List<MemberResponseDto>>(ApiRoutes.Members.ROOT).getOrNull()?.map { it.toDomain() }
                ?: emptyList()
        }

    override fun observeMember(memberId: Long): Flow<MemberDetail?> =
        reactiveFlow<MemberDetail?>(DataTopic.MEMBER, fallback = null) {
            api.getData<MemberDetailResponseDto>(ApiRoutes.Members.detail(memberId)).getOrNull()?.toDomain()
        }

    override fun observeMyMember(): Flow<Member?> =
        reactiveFlow<Member?>(DataTopic.MEMBER, DataTopic.CLUB, fallback = null) {
            api.getData<MemberResponseDto>(ApiRoutes.Members.ME).getOrNull()?.toDomain()
        }

    override fun observeJoinedClubs(): Flow<List<ClubMembership>> =
        reactiveFlow(DataTopic.CLUB, fallback = emptyList()) {
            api.getData<List<ClubMembershipResponseDto>>(ApiRoutes.Clubs.JOINED).getOrNull()
                ?.map { it.toDomain() } ?: emptyList()
        }

    override suspend fun changeMemberCohort(memberId: Long, cohortId: Long): DataResult<Unit> =
        api.postUnit(ApiRoutes.Members.cohort(memberId), ChangeCohortRequestDto(cohortId))
            .also { RemoteBus.invalidate(DataTopic.MEMBER, DataTopic.CLUB) }

    override suspend fun changeMemberRole(memberId: Long, role: MemberRole): DataResult<Unit> =
        api.postUnit(ApiRoutes.Members.role(memberId), ChangeRoleRequestDto(role.name))
            .also { RemoteBus.invalidate(DataTopic.MEMBER, DataTopic.CLUB, DataTopic.SETTINGS) }

    override suspend fun removeMember(memberId: Long): DataResult<Unit> =
        api.deleteUnit(ApiRoutes.Members.detail(memberId))
            .also { RemoteBus.invalidate(DataTopic.MEMBER, DataTopic.CLUB) }

    override suspend fun addCohort(shortLabel: String, displayName: String): DataResult<Cohort> =
        api.postData<CohortResponseDto>(ApiRoutes.Clubs.ME_COHORTS, CohortCreateRequestDto(shortLabel, displayName))
            .map { it.toDomain() }
            .also { RemoteBus.invalidate(DataTopic.CLUB) }

    override suspend fun renameCohort(
        cohortId: Long,
        shortLabel: String,
        displayName: String,
    ): DataResult<Unit> =
        api.patchUnit(ApiRoutes.Clubs.cohort(cohortId), CohortRenameRequestDto(shortLabel, displayName))
            .also { RemoteBus.invalidate(DataTopic.CLUB) }

    override fun switchClub(clubId: Long) {
        scope.launch {
            api.postUnit(ApiRoutes.Clubs.SWITCH, SwitchClubRequestDto(clubId))
            RemoteBus.invalidateAll() // 활성 동아리 교체 → 전 도메인 데이터가 바뀜
        }
    }

    override suspend fun withdrawFromActiveClub(): DataResult<Boolean> {
        val leave = api.postUnit(ApiRoutes.Clubs.ME_LEAVE)
        if (leave is DataResult.Failure) return leave
        RemoteBus.invalidateAll() // 멤버십 삭제 + 활성 동아리 재지정 → 전 도메인 갱신
        val hasActiveClub = api.getData<MemberResponseDto>(ApiRoutes.Members.ME) is DataResult.Success
        return DataResult.Success(hasActiveClub)
    }
}
