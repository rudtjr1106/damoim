package com.damoim.app.data.remote.club

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.core.result.getOrNull
import com.damoim.app.core.result.map
import com.damoim.app.data.remote.core.ApiClient
import com.damoim.app.data.remote.core.ApiRoutes
import com.damoim.app.data.remote.core.DataTopic
import com.damoim.app.data.remote.core.ErrorCodes
import com.damoim.app.data.remote.core.RawHttp
import com.damoim.app.data.remote.core.RemoteBus
import com.damoim.app.data.remote.core.SharedFlows
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
import com.damoim.app.platform.compressImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * [ClubRepository]의 서버 구현 (B 동아리/홈 + E 회원/기수/멀티동아리).
 *
 * - 변경은 영향 도메인만 무효화(예: 기수 변경 → MEMBER·CLUB). 전환/탈퇴는 invalidateAll.
 * - observe는 [SharedFlows]로 공유 → 여러 화면이 같은 데이터를 봐도 fetch 1회. 특히 observeRole·
 *   observeMyMember는 같은 members/me 조회를 공유한다.
 */
class RemoteClubRepository(private val api: ApiClient) : ClubRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val shared = SharedFlows(scope)

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

    /** 내 명부 정보(members/me) — observeRole·observeMyMember가 공유하는 단일 조회. */
    private fun meMember(): Flow<MemberResponseDto?> =
        shared.get("me-member") {
            reactiveFlow<MemberResponseDto?>(DataTopic.CLUB, DataTopic.MEMBER, fallback = null) {
                api.getData<MemberResponseDto>(ApiRoutes.Members.ME).getOrNull()
            }
        }

    override fun observeRole(): Flow<ClubRole?> = meMember().map { it?.let { m -> clubRole(m.role) } }

    override fun observeMemberRole(): Flow<MemberRole?> = meMember().map { it?.let { m -> memberRole(m.role) } }

    override fun observePermissions(): Flow<Set<String>> = meMember().map { it?.permissions?.toSet() ?: emptySet() }

    // 공유 flow(meMember)를 쓰지 않는다 — 그 replay에는 직전 세션 값이 남아 있을 수 있어
    // 재로그인 판정이 뒤집힌다(withdrawFromActiveClub의 잔존판정과 같은 이유로 직접 조회).
    override suspend fun fetchMyRole(): ClubRole? =
        api.getData<MemberResponseDto>(ApiRoutes.Members.ME).getOrNull()?.let { clubRole(it.role) }

    override fun observeHomeSummary(): Flow<HomeSummary?> = shared.get("home") {
        reactiveFlow<HomeSummary?>(DataTopic.CLUB, DataTopic.MEMBER, DataTopic.NOTIFICATION, fallback = null) {
            api.getData<HomeSummaryResponseDto>(ApiRoutes.Clubs.ME_HOME).getOrNull()?.toDomain()
        }
    }

    override fun observeClub(): Flow<Club?> = shared.get("club") {
        reactiveFlow<Club?>(DataTopic.CLUB, fallback = null) {
            api.getData<ClubResponseDto>(ApiRoutes.Clubs.ME).getOrNull()?.toDomain()
        }
    }

    override fun observeCohorts(): Flow<List<Cohort>> = shared.get("cohorts") {
        reactiveFlow(DataTopic.CLUB, DataTopic.MEMBER, fallback = emptyList()) {
            api.getData<List<CohortResponseDto>>(ApiRoutes.Clubs.ME_COHORTS).getOrNull()?.map { it.toDomain() }
                ?: emptyList()
        }
    }

    override suspend fun uploadClubImage(bytes: ByteArray, contentType: String?): DataResult<String> {
        // 업로드 전 클라이언트 축소·재압축 — 재인코딩됐으면 image/jpeg로 선언(presign·PUT 일치 필수).
        val optimized = compressImage(bytes)
        val type = if (optimized === bytes) contentType else "image/jpeg"
        // 1) 업로드 URL 발급(상한 검증) → 2) S3에 직접 PUT → storageKey 반환. (프로필 사진과 동일 패턴)
        val presign = api.postData<ClubImageUploadResponseDto>(
            ApiRoutes.Clubs.ME_IMAGE,
            ClubImageUploadRequestDto(contentType = type, sizeBytes = optimized.size.toLong()),
        )
        val upload = when (presign) {
            is DataResult.Success -> presign.data
            is DataResult.Failure -> return presign
        }
        return if (RawHttp.put(upload.uploadUrl, optimized, type)) {
            DataResult.Success(upload.storageKey)
        } else {
            DataResult.Failure(DataError(ErrorCodes.UPLOAD_FAILED, "이미지 업로드에 실패했어요"))
        }
    }

    override suspend fun updateClub(name: String?, intro: String?, imageKey: String?): DataResult<Club> =
        api.patchData<ClubResponseDto>(ApiRoutes.Clubs.ME, UpdateClubRequestDto(name, intro, imageKey))
            .map { it.toDomain() }
            // 이름/이미지가 홈·설정·전환 시트에 반영되므로 CLUB 무효화(observeClub 즉시 재조회).
            .also { RemoteBus.invalidate(DataTopic.CLUB) }

    override suspend fun regenerateJoinCode(): DataResult<String> =
        api.postData<JoinCodeResponseDto>(ApiRoutes.Clubs.JOIN_CODE_REGENERATE)
            .map { it.joinCode ?: "" }
            .also { RemoteBus.invalidate(DataTopic.CLUB) }

    override suspend fun disableJoinCode(): DataResult<Unit> =
        api.postUnit(ApiRoutes.Clubs.JOIN_CODE_DISABLE).also { RemoteBus.invalidate(DataTopic.CLUB) }

    override fun observeApplicants(): Flow<ApplicantsBoard> = shared.get("applicants") {
        reactiveFlow(DataTopic.CLUB, fallback = ApplicantsBoard(emptyList(), emptyList())) {
            api.getData<ApplicantsBoardResponseDto>(ApiRoutes.Clubs.ME_APPLICANTS).getOrNull()?.toDomain()
                ?: ApplicantsBoard(emptyList(), emptyList())
        }
    }

    override suspend fun decideApplicant(applicantId: Long, approve: Boolean): DataResult<Unit> =
        api.postUnit(ApiRoutes.Clubs.decide(applicantId), DecideRequestDto(approve))
            .also { RemoteBus.invalidate(DataTopic.CLUB, DataTopic.MEMBER, DataTopic.NOTIFICATION) }

    // ── E: 회원/기수/멀티동아리 ──

    override fun observeMembers(): Flow<List<Member>> = shared.get("members") {
        reactiveFlow(DataTopic.MEMBER, DataTopic.CLUB, fallback = emptyList()) {
            api.getData<List<MemberResponseDto>>(ApiRoutes.Members.ROOT).getOrNull()?.map { it.toDomain() }
                ?: emptyList()
        }
    }

    override fun observeMember(memberId: Long): Flow<MemberDetail?> = shared.get("member:$memberId") {
        reactiveFlow<MemberDetail?>(DataTopic.MEMBER, fallback = null) {
            api.getData<MemberDetailResponseDto>(ApiRoutes.Members.detail(memberId)).getOrNull()?.toDomain()
        }
    }

    override fun observeMyMember(): Flow<Member?> = meMember().map { it?.toDomain() }

    override fun observeJoinedClubs(): Flow<List<ClubMembership>> = shared.get("joined") {
        reactiveFlow(DataTopic.CLUB, fallback = emptyList()) {
            api.getData<List<ClubMembershipResponseDto>>(ApiRoutes.Clubs.JOINED).getOrNull()
                ?.map { it.toDomain() } ?: emptyList()
        }
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
            RemoteBus.invalidateAll()
        }
    }

    override suspend fun withdrawFromActiveClub(): DataResult<Boolean> {
        val leave = api.postUnit(ApiRoutes.Clubs.ME_LEAVE)
        if (leave is DataResult.Failure) return leave
        RemoteBus.invalidateAll()
        val hasActiveClub = api.getData<MemberResponseDto>(ApiRoutes.Members.ME) is DataResult.Success
        return DataResult.Success(hasActiveClub)
    }
}
