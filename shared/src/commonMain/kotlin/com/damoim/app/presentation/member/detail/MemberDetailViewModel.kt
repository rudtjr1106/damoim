package com.damoim.app.presentation.member.detail

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.Cohort
import com.damoim.app.domain.model.MemberDetail
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.domain.usecase.GetCohortsUseCase
import com.damoim.app.domain.usecase.GetMemberDetailUseCase
import com.damoim.app.domain.usecase.MemberActionUseCase
import com.damoim.app.domain.usecase.ObserveMyContextUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.launch

data class MemberDetailUiState(
    val isLoading: Boolean = true,
    val detail: MemberDetail? = null,
    val cohorts: List<Cohort> = emptyList(),
    val isLeader: Boolean = false,
) : UiState {
    /** 동아리장 관리 섹션은 내가 리더이고 대상이 내가 아닐 때만. */
    val canManage: Boolean get() = isLeader && detail?.member?.isMe == false
}

sealed interface MemberDetailSideEffect : UiSideEffect {
    data class Toast(val message: String) : MemberDetailSideEffect
    data object Removed : MemberDetailSideEffect
}

/** 화면 18 회원 상세 — 기수 변경/역할 변경/내보내기가 스토어에 실제 반영된다. */
class MemberDetailViewModel(
    getMemberDetail: GetMemberDetailUseCase,
    getCohorts: GetCohortsUseCase,
    observeMyContext: ObserveMyContextUseCase,
    private val memberAction: MemberActionUseCase,
    private val memberId: Long,
) : BaseViewModel<MemberDetailUiState, MemberDetailSideEffect>(MemberDetailUiState()) {

    init {
        viewModelScope.launch {
            getMemberDetail(memberId).collect { d -> setState { copy(isLoading = false, detail = d) } }
        }
        viewModelScope.launch {
            getCohorts().collect { list -> setState { copy(cohorts = list) } }
        }
        viewModelScope.launch {
            observeMyContext().collect { ctx -> setState { copy(isLeader = ctx.role == ClubRole.LEADER) } }
        }
    }

    fun onChangeCohort(cohortId: Long) = viewModelScope.launch {
        handleResult(memberAction.changeCohort(memberId, cohortId), onSuccess = { sendEffect(MemberDetailSideEffect.Toast(DamoimStrings.TOAST_MEMBER_COHORT_CHANGED)) })
    }

    fun onChangeRole(role: MemberRole) = viewModelScope.launch {
        handleResult(memberAction.changeRole(memberId, role), onSuccess = { sendEffect(MemberDetailSideEffect.Toast(DamoimStrings.TOAST_MEMBER_ROLE_CHANGED)) })
    }

    fun onRemove() = viewModelScope.launch {
        handleResult(memberAction.remove(memberId), onSuccess = { sendEffect(MemberDetailSideEffect.Removed) })
    }
}
