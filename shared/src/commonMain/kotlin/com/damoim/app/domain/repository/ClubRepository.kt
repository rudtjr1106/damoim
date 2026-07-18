package com.damoim.app.domain.repository

import com.damoim.app.core.result.DataResult
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
import kotlinx.coroutines.flow.Flow

/**
 * 동아리 관련 레포지토리. 조회는 Flow(상태 변경 실시간 반영), 변경은 suspend.
 */
interface ClubRepository {

    /** 가입 코드 제출 (화면 03). */
    suspend fun submitJoinCode(code: String): DataResult<JoinRequestResult>

    /** 새 동아리 생성 (화면 07). 세션이 동아리장(LEADER)으로 시작된다. */
    suspend fun createClub(name: String, intro: String, category: String): DataResult<Club>

    /** 온보딩 완료 후 동아리 입장(04 확인 → MEMBER). 이미 세션이 있으면 무시. */
    fun enterClub(role: ClubRole)

    /** 현재 세션 역할. 세션이 없으면 null. */
    fun observeRole(): Flow<ClubRole?>

    /** 명부 등급(LEADER/STAFF/MEMBER). 운영진(STAFF) 권한 판정용 — ClubRole만으론 STAFF를 구분 못 한다. */
    fun observeMemberRole(): Flow<MemberRole?>

    /** 내 세분 권한 이름 집합(SCHEDULE_MANAGE 등). 리더는 서버가 전권을 내려준다. */
    fun observePermissions(): Flow<Set<String>>

    /**
     * 현재 세션 역할을 **일회성으로** 조회한다(동아리 없으면 null). 라우팅 판정 전용.
     * [observeRole]은 공유 flow라 직전 세션의 값이 replay될 수 있어(로그아웃 → 재로그인),
     * 시작 화면 판정처럼 한 번의 정확한 답이 필요한 곳은 캐시를 우회해 서버에 직접 묻는다.
     */
    suspend fun fetchMyRole(): ClubRole?

    /** 홈 요약 (화면 05/06). 회원 수·신청 대기·게시판 미리보기가 실시간 반영된다. */
    fun observeHomeSummary(): Flow<HomeSummary?>

    /** 내 동아리 정보 (화면 08 설정). */
    fun observeClub(): Flow<Club?>

    /** 동아리 기수 목록 (19/42 · 69 공개 범위 선택). */
    fun observeCohorts(): Flow<List<Cohort>>

    /** 08 대표 이미지 업로드 — 바이트를 S3에 올리고 storageKey 반환(이후 [updateClub]에 전달). */
    suspend fun uploadClubImage(bytes: ByteArray, contentType: String?): DataResult<String>

    /** 08 동아리 정보 수정 — null 필드는 변경 안 함(부분 수정). imageKey는 업로드한 S3 키. */
    suspend fun updateClub(name: String?, intro: String?, imageKey: String?): DataResult<Club>

    /** 가입 코드 재발급 (화면 08). 새 코드 반환. */
    suspend fun regenerateJoinCode(): DataResult<String>

    /** 가입 코드 비활성화 (화면 08). */
    suspend fun disableJoinCode(): DataResult<Unit>

    /** 가입 신청 관리 데이터 — 대기 + 처리 완료 (화면 09). */
    fun observeApplicants(): Flow<ApplicantsBoard>

    /** 신청 승인/거절 (화면 09). 승인 시 회원 수가 즉시 반영된다. */
    suspend fun decideApplicant(applicantId: Long, approve: Boolean): DataResult<Unit>

    // ── E. 회원·기수 관리 ──

    /** 회원 명부 (16/17). */
    fun observeMembers(): Flow<List<Member>>

    /** 회원 상세 (18). */
    fun observeMember(memberId: Long): Flow<MemberDetail?>

    /** 내 명부 정보 (20 내 프로필의 기수·역할 뱃지). */
    fun observeMyMember(): Flow<Member?>

    /** 44 동아리별 프로필 수정 — 표시 이름 오버라이드(빈값=해제). 명부·게시글 작성자 등에 반영. */
    suspend fun updateMyClubProfile(displayName: String): DataResult<Member>

    /** 내가 속한 동아리들 (33 동아리 전환). */
    fun observeJoinedClubs(): Flow<List<ClubMembership>>

    /** 42 기수 변경 — 옛/새 기수 회원 수가 함께 이동한다. */
    suspend fun changeMemberCohort(memberId: Long, cohortId: Long): DataResult<Unit>

    /** 18 역할 변경 (운영진 ↔ 일반). */
    suspend fun changeMemberRole(memberId: Long, role: MemberRole): DataResult<Unit>

    /** 43 내보내기 — 명부에서 제거하고 기수·동아리 회원 수 감소. */
    suspend fun removeMember(memberId: Long): DataResult<Unit>

    /** 44 새 기수 추가. */
    suspend fun addCohort(shortLabel: String, displayName: String): DataResult<Cohort>

    /** 19 기수 이름 변경. */
    suspend fun renameCohort(cohortId: Long, shortLabel: String, displayName: String): DataResult<Unit>

    /** 33 동아리 전환 (즉시 데이터 스왑). */
    fun switchClub(clubId: Long)

    /**
     * 60 동아리 탈퇴 — 활성 동아리 멤버십 삭제(POST /api/clubs/me/leave). 로그인은 유지된다.
     * 서버가 남은 가입 동아리 중 하나로 활성 동아리를 재지정한다.
     * @return true=아직 활성 동아리 있음(→ 새 동아리 홈), false=남은 동아리 없음(→ 온보딩, 재로그인 없이).
     */
    suspend fun withdrawFromActiveClub(): DataResult<Boolean>

    /**
     * 52 동아리 삭제 — 동아리장이 마지막 1인일 때만(DELETE /api/clubs/me). 동아리 데이터 전체가 사라진다.
     * @return true=아직 다른 활성 동아리 있음(→ 새 동아리 홈), false=없음(→ 온보딩).
     */
    suspend fun deleteActiveClub(): DataResult<Boolean>
}
