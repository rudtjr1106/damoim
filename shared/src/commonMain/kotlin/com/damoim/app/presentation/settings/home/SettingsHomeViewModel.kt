package com.damoim.app.presentation.settings.home

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.usecase.GetClubInfoUseCase
import com.damoim.app.domain.usecase.SubscriptionUseCase
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SettingsHomeUiState(
    val clubName: String = "",
    val clubInitial: String = "동",
    val clubImageUrl: String? = null,
    val memberCount: Int = 0,
    val joinCode: String = "",
    val planName: String = "무료 플랜",
    val overLimit: Boolean = false,
    val memberUsed: Int = 0,
    val memberLimit: Int = 30,
) : UiState

sealed interface SettingsHomeSideEffect : UiSideEffect

/** 26 설정 홈(동아리장 허브). 동아리 정보 + 구독 상태(인원 초과 경고) 관찰. */
class SettingsHomeViewModel(
    getClubInfo: GetClubInfoUseCase,
    subscription: SubscriptionUseCase,
) : BaseViewModel<SettingsHomeUiState, SettingsHomeSideEffect>(SettingsHomeUiState()) {

    init {
        viewModelScope.launch {
            combine(getClubInfo(), subscription.observe()) { club, sub -> club to sub }.collect { (club, sub) ->
                setState {
                    copy(
                        clubName = club?.name ?: "",
                        clubInitial = club?.name?.take(1) ?: "동",
                        clubImageUrl = club?.imageUrl,
                        memberCount = club?.memberCount ?: sub.memberUsed,
                        joinCode = club?.joinCode ?: "",
                        planName = sub.planName,
                        overLimit = sub.overLimit,
                        memberUsed = sub.memberUsed,
                        memberLimit = sub.memberLimit,
                    )
                }
            }
        }
    }
}
