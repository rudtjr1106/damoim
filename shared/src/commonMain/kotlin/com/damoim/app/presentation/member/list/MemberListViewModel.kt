package com.damoim.app.presentation.member.list

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.Cohort
import com.damoim.app.domain.model.Member
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.domain.usecase.GetClubInfoUseCase
import com.damoim.app.domain.usecase.GetCohortsUseCase
import com.damoim.app.domain.usecase.GetMembersUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** 17 필터. 전체 / 운영진 / 특정 기수. */
sealed interface MemberFilter {
    data object All : MemberFilter
    data object Staff : MemberFilter
    data class Cohort(val cohortId: Long) : MemberFilter
}

data class MemberListUiState(
    val isLoading: Boolean = true,
    val all: List<Member> = emptyList(),
    val cohorts: List<Cohort> = emptyList(),
    val totalCount: Int = 0,
    val query: String = "",
    val filter: MemberFilter = MemberFilter.All,
) : UiState {
    fun cohortShort(cohortId: Long): String = cohorts.firstOrNull { it.id == cohortId }?.short ?: ""

    val displayed: List<Member>
        get() = all
            .filter { m ->
                when (val f = filter) {
                    MemberFilter.All -> true
                    MemberFilter.Staff -> m.role == MemberRole.STAFF || m.role == MemberRole.LEADER
                    is MemberFilter.Cohort -> m.cohortId == f.cohortId
                }
            }
            .filter { m -> query.isBlank() || m.name.contains(query, true) || cohortShort(m.cohortId).contains(query, true) }

    /** 신규 동아리 등으로 명부 자체가 비었을 때(77). */
    val isClubEmpty: Boolean get() = !isLoading && all.none { !it.isMe } && all.size <= 1
    val isSearchEmpty: Boolean get() = !isLoading && !isClubEmpty && displayed.isEmpty()
}

sealed interface MemberListSideEffect : UiSideEffect

/** 화면 17 회원 목록 — 검색·필터는 스토어 명부 위에서 클라이언트 필터링. */
class MemberListViewModel(
    getMembers: GetMembersUseCase,
    getCohorts: GetCohortsUseCase,
    getClubInfo: GetClubInfoUseCase,
    initialCohortId: Long?,
) : BaseViewModel<MemberListUiState, MemberListSideEffect>(
    MemberListUiState(filter = initialCohortId?.let { MemberFilter.Cohort(it) } ?: MemberFilter.All),
) {
    init {
        viewModelScope.launch {
            combine(getMembers(), getCohorts(), getClubInfo()) { members, cohorts, club ->
                Triple(members, cohorts, club?.memberCount ?: members.size)
            }.collect { (members, cohorts, total) ->
                setState { copy(isLoading = false, all = members, cohorts = cohorts, totalCount = total) }
            }
        }
    }

    fun onQuery(value: String) = setState { copy(query = value) }
    fun onFilter(filter: MemberFilter) = setState { copy(filter = filter) }
}
