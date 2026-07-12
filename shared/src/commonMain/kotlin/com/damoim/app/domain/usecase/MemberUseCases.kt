package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.Cohort
import com.damoim.app.domain.model.ClubMembership
import com.damoim.app.domain.model.Member
import com.damoim.app.domain.model.MemberDetail
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.domain.repository.AuthRepository
import com.damoim.app.domain.repository.ClubRepository
import kotlinx.coroutines.flow.Flow

/** 17 회원 목록. */
class GetMembersUseCase(private val repo: ClubRepository) {
    operator fun invoke(): Flow<List<Member>> = repo.observeMembers()
}

/** 18 회원 상세. */
class GetMemberDetailUseCase(private val repo: ClubRepository) {
    operator fun invoke(memberId: Long): Flow<MemberDetail?> = repo.observeMember(memberId)
}

/** 20 내 명부 정보. */
class GetMyMemberUseCase(private val repo: ClubRepository) {
    operator fun invoke(): Flow<Member?> = repo.observeMyMember()
}

/** 33 내가 속한 동아리. */
class GetJoinedClubsUseCase(private val repo: ClubRepository) {
    operator fun invoke(): Flow<List<ClubMembership>> = repo.observeJoinedClubs()
}

/** 18 회원 관리 액션 묶음(기수 변경/역할 변경/내보내기). */
class MemberActionUseCase(private val repo: ClubRepository) {
    suspend fun changeCohort(memberId: Long, cohortId: Long): DataResult<Unit> = repo.changeMemberCohort(memberId, cohortId)
    suspend fun changeRole(memberId: Long, role: MemberRole): DataResult<Unit> = repo.changeMemberRole(memberId, role)
    suspend fun remove(memberId: Long): DataResult<Unit> = repo.removeMember(memberId)
}

/** 19/44 기수 추가·이름 변경. */
class CohortActionUseCase(private val repo: ClubRepository) {
    suspend fun add(shortLabel: String, displayName: String): DataResult<Cohort> = repo.addCohort(shortLabel, displayName)
    suspend fun rename(cohortId: Long, shortLabel: String, displayName: String): DataResult<Unit> =
        repo.renameCohort(cohortId, shortLabel, displayName)
}

/** 33 전환 / 60 탈퇴 / 로그아웃 — 세션 전환·종료. */
class ClubSessionUseCase(private val repo: ClubRepository) {
    fun switch(clubId: Long) = repo.switchClub(clubId)
    /** 60 동아리 탈퇴. @return true=잔존 활성 동아리 있음(→ 새 홈), false=없음(→ 온보딩). */
    suspend fun withdraw(): DataResult<Boolean> = repo.withdrawFromActiveClub()
}

/** 로그아웃 — 토큰 폐기·세션 종료(→ 로그인). */
class LogoutUseCase(private val repo: AuthRepository) {
    suspend operator fun invoke(): DataResult<Unit> = repo.logout()
}
