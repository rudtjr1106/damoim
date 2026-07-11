package com.damoim.app.presentation.member

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.domain.usecase.GetCohortsUseCase
import com.damoim.app.domain.usecase.GetClubInfoUseCase
import com.damoim.app.domain.usecase.GetJoinApplicantsUseCase
import com.damoim.app.domain.usecase.GetMembersUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MemberManageUiState(
    val totalCount: Int = 0,
    val staffCount: Int = 0,
    val cohortCount: Int = 0,
    val cohortRange: String = "",
    val pendingCount: Int = 0,
    val myName: String = "",
    val myCohortShort: String = "",
    val myRole: MemberRole = MemberRole.MEMBER,
) : UiState

sealed interface MemberManageSideEffect : UiSideEffect

/** 화면 16 회원 관리 허브 — 통계·내 프로필 요약을 스토어 flow에서 실시간 파생한다. */
class MemberManageViewModel(
    getMembers: GetMembersUseCase,
    getClubInfo: GetClubInfoUseCase,
    getCohorts: GetCohortsUseCase,
    getApplicants: GetJoinApplicantsUseCase,
) : BaseViewModel<MemberManageUiState, MemberManageSideEffect>(MemberManageUiState()) {

    init {
        viewModelScope.launch {
            combine(getMembers(), getClubInfo(), getCohorts(), getApplicants()) { members, club, cohorts, applicants ->
                val me = members.firstOrNull { it.isMe }
                val myCohort = cohorts.firstOrNull { it.id == me?.cohortId }
                MemberManageUiState(
                    totalCount = club?.memberCount ?: members.size,
                    staffCount = members.count { it.role == MemberRole.STAFF || it.role == MemberRole.LEADER },
                    cohortCount = cohorts.size,
                    cohortRange = if (cohorts.isEmpty()) "" else "${cohorts.last().short} ~ ${cohorts.first().short}",
                    pendingCount = applicants.pending.size,
                    myName = me?.name ?: "",
                    myCohortShort = myCohort?.short ?: "",
                    myRole = me?.role ?: MemberRole.MEMBER,
                )
            }.collect { next -> setState { next } }
        }
    }
}
