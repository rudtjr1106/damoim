package com.damoim.app.presentation.club

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.ClubMembership
import com.damoim.app.domain.usecase.ClubSessionUseCase
import com.damoim.app.domain.usecase.GetClubInfoUseCase
import com.damoim.app.domain.usecase.GetJoinedClubsUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ClubSwitchUiState(
    val joinedClubs: List<ClubMembership> = emptyList(),
    val currentClubId: Long = -1L,
) : UiState

sealed interface ClubSwitchSideEffect : UiSideEffect

/** 33 동아리 전환 — 설정(42)·홈 동아리명(46)이 공유하는 뷰모델. 세션만 갈아탄다. */
class ClubSwitchViewModel(
    getJoinedClubs: GetJoinedClubsUseCase,
    getClubInfo: GetClubInfoUseCase,
    private val clubSession: ClubSessionUseCase,
) : BaseViewModel<ClubSwitchUiState, ClubSwitchSideEffect>(ClubSwitchUiState()) {

    init {
        viewModelScope.launch {
            combine(getJoinedClubs(), getClubInfo()) { joined, club ->
                ClubSwitchUiState(joinedClubs = joined, currentClubId = club?.id ?: -1L)
            }.collect { next -> setState { next } }
        }
    }

    fun switch(clubId: Long) = clubSession.switch(clubId)
}
