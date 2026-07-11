package com.damoim.app.presentation.profile

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.ClubMembership
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.domain.usecase.ClubSessionUseCase
import com.damoim.app.domain.usecase.GetClubInfoUseCase
import com.damoim.app.domain.usecase.GetCohortsUseCase
import com.damoim.app.domain.usecase.GetJoinedClubsUseCase
import com.damoim.app.domain.usecase.GetMyMemberUseCase
import com.damoim.app.domain.usecase.ObserveMyContextUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MyProfileUiState(
    val name: String = "",
    val initials: String = "",
    val email: String = "",
    val cohortShort: String = "",
    val cohortLabel: String = "",
    val role: MemberRole = MemberRole.MEMBER,
    val joinedLabel: String = "",
    val currentClubName: String = "",
    val joinedClubs: List<ClubMembership> = emptyList(),
) : UiState

sealed interface MyProfileSideEffect : UiSideEffect

/** 화면 20 내 프로필 — 내 명부·프로필·기수를 파생. 프로필 수정 시 자동 반영. */
class MyProfileViewModel(
    getMyMember: GetMyMemberUseCase,
    getCohorts: GetCohortsUseCase,
    getClubInfo: GetClubInfoUseCase,
    getJoinedClubs: GetJoinedClubsUseCase,
    observeMyContext: ObserveMyContextUseCase,
    private val clubSession: ClubSessionUseCase,
) : BaseViewModel<MyProfileUiState, MyProfileSideEffect>(MyProfileUiState()) {

    init {
        viewModelScope.launch {
            combine(getMyMember(), getCohorts(), getClubInfo(), getJoinedClubs(), observeMyContext()) { me, cohorts, club, joined, ctx ->
                val cohort = cohorts.firstOrNull { it.id == me?.cohortId }
                MyProfileUiState(
                    name = me?.name ?: ctx.name,
                    initials = me?.initials ?: "",
                    email = me?.email ?: "",
                    cohortShort = cohort?.short ?: "",
                    cohortLabel = cohort?.label ?: "",
                    role = me?.role ?: MemberRole.MEMBER,
                    joinedLabel = me?.joinedLabel ?: "",
                    currentClubName = club?.name ?: "",
                    joinedClubs = joined,
                )
            }.collect { next -> setState { next } }
        }
    }

    fun onSwitchClub(clubId: Long) = clubSession.switch(clubId)

    /** 로그아웃 / 탈퇴 / 새 동아리 참여 — 세션 종료. */
    fun onLeave() = clubSession.leave()
}
