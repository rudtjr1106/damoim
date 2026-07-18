package com.damoim.app.presentation.settings.home

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.usecase.ClubSessionUseCase
import com.damoim.app.domain.usecase.GetClubInfoUseCase
import com.damoim.app.domain.usecase.SubscriptionUseCase
import com.damoim.app.domain.usecase.WithdrawAccountUseCase
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

sealed interface SettingsHomeSideEffect : UiSideEffect {
    data object WithdrewAccount : SettingsHomeSideEffect          // 51 탈퇴 완료 → 로그인
    data object DeletedToClub : SettingsHomeSideEffect            // 52 삭제 후 잔존 동아리 → 새 홈
    data object DeletedNoClub : SettingsHomeSideEffect            // 52 삭제 후 없음 → 온보딩
    data class ActionFailed(val message: String) : SettingsHomeSideEffect  // 예: 단독 리더 위임 필요
}

/** 26 설정 홈(동아리장 허브). 동아리 정보 + 구독 상태(인원 초과 경고) 관찰. */
class SettingsHomeViewModel(
    getClubInfo: GetClubInfoUseCase,
    subscription: SubscriptionUseCase,
    private val withdrawAccount: WithdrawAccountUseCase,
    private val clubSession: ClubSessionUseCase,
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

    /** 51 회원 탈퇴 — 성공 시 로그인으로, 실패(단독 리더 위임 필요 등) 시 메시지 표면화. */
    fun onWithdrawAccount() = viewModelScope.launch {
        when (val result = withdrawAccount()) {
            is DataResult.Success -> sendEffect(SettingsHomeSideEffect.WithdrewAccount)
            is DataResult.Failure -> sendEffect(SettingsHomeSideEffect.ActionFailed(result.error.message))
        }
    }

    /** 52 동아리 삭제 — 잔존 동아리 여부로 새 홈/온보딩 분기. 실패(회원 남음 등) 시 메시지 표면화. */
    fun onDeleteClub() = viewModelScope.launch {
        when (val result = clubSession.deleteClub()) {
            is DataResult.Success ->
                sendEffect(if (result.data) SettingsHomeSideEffect.DeletedToClub else SettingsHomeSideEffect.DeletedNoClub)
            is DataResult.Failure -> sendEffect(SettingsHomeSideEffect.ActionFailed(result.error.message))
        }
    }
}
