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
    ) {
        /** 동아리장. 동아리 삭제·위임·설정 등 리더 전용 기능 게이트. */
        val isLeader: Boolean get() = role == ClubRole.LEADER
        /** 운영진(동아리장 또는 STAFF). 게시판·자료 관리 등 운영진 허용 기능 게이트(서버 canManage와 일치). */
        val isAdmin: Boolean get() = memberRole != null && memberRole != MemberRole.MEMBER
    }

    operator fun invoke(): Flow<MyContext> =
        combine(
            authRepository.observeUser(),
            clubRepository.observeRole(),
            clubRepository.observeMemberRole(),
        ) { user, role, memberRole ->
            MyContext(user.id, user.nickname, role, user.needsProfileSetup, user.profileImageUrl, memberRole)
        }
}
