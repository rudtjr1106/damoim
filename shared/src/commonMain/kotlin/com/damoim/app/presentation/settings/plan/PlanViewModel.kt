package com.damoim.app.presentation.settings.plan

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.PlanTier
import com.damoim.app.domain.model.SubscriptionPlan
import com.damoim.app.domain.usecase.SubscriptionUseCase
import kotlinx.coroutines.launch

data class PlanUiState(
    val plans: List<SubscriptionPlan> = emptyList(),
    val currentTier: PlanTier = PlanTier.FREE,
    val memberCount: Int = 0,
) : UiState

sealed interface PlanSideEffect : UiSideEffect

/** 27 구독 플랜 안내. 결제는 플랫폼 인앱결제(화면에서 트리거)로 처리하고 성공 시 [subscribe]로 반영. */
class PlanViewModel(
    private val subscription: SubscriptionUseCase,
) : BaseViewModel<PlanUiState, PlanSideEffect>(PlanUiState(plans = subscription.plans())) {

    init {
        viewModelScope.launch {
            subscription.observe().collect { sub -> setState { copy(currentTier = sub.tier, memberCount = sub.memberUsed) } }
        }
    }

    fun subscribe(tier: PlanTier) = viewModelScope.launch { subscription.subscribe(tier) }
}
