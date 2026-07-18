package com.damoim.app.domain.usecase

import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.domain.repository.AuthRepository
import com.damoim.app.domain.repository.ClubRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** 내 컨텍스트(사용자 id/이름 + 역할) 관찰 — 내 글 판정·운영진 기능 분기용. */
class ObserveMyContextUseCase(
    private val authRepository: AuthRepository,
    private val clubRepository: ClubRepository,
) {
    data class MyContext(
        val userId: Long,
        val name: String,
        val role: ClubRole?,
        val needsProfileSetup: Boolean = false,
        val profileImageUrl: String? = null,
        // 명부 등급 — STAFF 구분용(ClubRole은 LEADER/MEMBER뿐이라 STAFF를 표현 못 한다).
        val memberRole: MemberRole? = null,
        // 내 세분 권한 이름 집합(SCHEDULE_MANAGE 등). 리더는 서버가 전권을 내려준다.
        val permissions: Set<String> = emptySet(),
    ) {
        /** 동아리장. 역할 변경·위임·운영진 권한 부여 등 리더 전용 기능 게이트. */
        val isLeader: Boolean get() = role == ClubRole.LEADER
        /** 운영진(동아리장 또는 STAFF). 게시판·자료 관리 등 coarse 운영진 게이트(서버 canManage와 일치). */
        val isAdmin: Boolean get() = memberRole != null && memberRole != MemberRole.MEMBER

        private fun can(permission: String): Boolean = isLeader || permission in permissions
        /** 일정/이벤트 관리(등록·수정·삭제·조기마감·공지). */
        val canManageSchedule: Boolean get() = can("SCHEDULE_MANAGE")
        /** 가입 신청 관리(승인/거절). */
        val canApproveJoin: Boolean get() = can("JOIN_APPROVE")
        /** 회원·기수 관리(기수 변경/추가, 내보내기). */
        val canManageMember: Boolean get() = can("MEMBER_MANAGE")
        /** 동아리 정보·가입 코드 설정. */
        val canManageClubSettings: Boolean get() = can("CLUB_SETTINGS")
    }

    operator fun invoke(): Flow<MyContext> =
        combine(
            authRepository.observeUser(),
            clubRepository.observeRole(),
            clubRepository.observeMemberRole(),
            clubRepository.observePermissions(),
        ) { user, role, memberRole, permissions ->
            MyContext(user.id, user.nickname, role, user.needsProfileSetup, user.profileImageUrl, memberRole, permissions)
        }
}
