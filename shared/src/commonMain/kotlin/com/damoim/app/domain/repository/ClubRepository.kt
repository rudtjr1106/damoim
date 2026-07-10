package com.damoim.app.domain.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.HomeSummary
import com.damoim.app.domain.model.JoinApplicant
import com.damoim.app.domain.model.JoinRequestResult
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

    /** 홈 요약 (화면 05/06). 회원 수·신청 대기·게시판 미리보기가 실시간 반영된다. */
    fun observeHomeSummary(): Flow<HomeSummary?>

    /** 내 동아리 정보 (화면 08 설정). */
    fun observeClub(): Flow<Club?>

    /** 가입 코드 재발급 (화면 08). 새 코드 반환. */
    suspend fun regenerateJoinCode(): DataResult<String>

    /** 가입 코드 비활성화 (화면 08). */
    suspend fun disableJoinCode(): DataResult<Unit>

    /** 가입 신청 (대기 목록, 처리 완료 수) (화면 09). */
    fun observeApplicants(): Flow<Pair<List<JoinApplicant>, Int>>

    /** 신청 승인/거절 (화면 09). 승인 시 회원 수가 즉시 반영된다. */
    suspend fun decideApplicant(applicantId: Long, approve: Boolean): DataResult<Unit>
}
