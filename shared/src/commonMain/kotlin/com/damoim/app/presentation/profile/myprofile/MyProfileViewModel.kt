package com.damoim.app.presentation.profile.myprofile

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.domain.usecase.ClubSessionUseCase
import com.damoim.app.domain.usecase.GetClubInfoUseCase
import com.damoim.app.domain.usecase.GetCohortsUseCase
import com.damoim.app.domain.usecase.GetMyMemberUseCase
import com.damoim.app.domain.usecase.LogoutUseCase
import com.damoim.app.domain.usecase.ObserveMyContextUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class MyProfileUiState(
    val name: String = "",
    val initials: String = "",
    val cohortShort: String = "",
    val cohortLabel: String = "",
    val role: MemberRole = MemberRole.MEMBER,
    val joinedLabel: String = "",
    val currentClubName: String = "",
    val profileImageUrl: String? = null,
) : UiState

sealed interface MyProfileSideEffect : UiSideEffect {
    data object WithdrewToClub : MyProfileSideEffect   // 탈퇴 후 잔존 동아리 → 새 동아리 홈
    data object WithdrewNoClub : MyProfileSideEffect   // 탈퇴 후 없음 → 온보딩(재로그인 X)
    data object LoggedOut : MyProfileSideEffect         // 로그아웃 → 로그인
    data class ActionFailed(val message: String) : MyProfileSideEffect  // 예: 단독 리더 탈퇴(위임 필요)
}

/** 화면 20 내 프로필 — 내 명부·프로필·기수를 파생. 프로필 수정 시 자동 반영. */
class MyProfileViewModel(
    getMyMember: GetMyMemberUseCase,
    getCohorts: GetCohortsUseCase,
    getClubInfo: GetClubInfoUseCase,
    observeMyContext: ObserveMyContextUseCase,
    private val clubSession: ClubSessionUseCase,
    private val logout: LogoutUseCase,
) : BaseViewModel<MyProfileUiState, MyProfileSideEffect>(MyProfileUiState()) {

    init {
        viewModelScope.launch {
            combine(getMyMember(), getCohorts(), getClubInfo(), observeMyContext()) { me, cohorts, club, ctx ->
                val cohort = cohorts.firstOrNull { it.id == me?.cohortId }
                MyProfileUiState(
                    name = me?.name ?: ctx.name,
                    initials = me?.initials ?: "",
                    cohortShort = cohort?.short ?: "",
                    // 기수가 없으면 배지는 숨기고(빈 파란 원 방지) 정보 행은 '미배정'으로 표시.
                    cohortLabel = cohort?.label?.ifBlank { DamoimStrings.PROFILE_COHORT_NONE } ?: DamoimStrings.PROFILE_COHORT_NONE,
                    role = me?.role ?: MemberRole.MEMBER,
                    joinedLabel = me?.joinedLabel ?: "",
                    currentClubName = club?.name ?: "",
                    profileImageUrl = ctx.profileImageUrl,
                )
            }.collect { next -> setState { next } }
        }
    }

    /** 60 동아리 탈퇴 — 멤버십 삭제(로그인 유지). 잔존 여부로 홈/온보딩 분기. */
    fun onWithdraw() = viewModelScope.launch {
        when (val result = clubSession.withdraw()) {
            is DataResult.Success ->
                sendEffect(if (result.data) MyProfileSideEffect.WithdrewToClub else MyProfileSideEffect.WithdrewNoClub)
            is DataResult.Failure ->
                sendEffect(MyProfileSideEffect.ActionFailed(result.error.message)) // 예: 위임 필요
        }
    }

    /** 로그아웃 — 토큰 폐기. 로컬 세션은 항상 정리되므로 결과와 무관하게 로그인으로. */
    fun onLogout() = viewModelScope.launch {
        logout()
        sendEffect(MyProfileSideEffect.LoggedOut)
    }
}
