package com.damoim.app.presentation.settings.subscription

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.SubscriptionState
import com.damoim.app.domain.usecase.SubscriptionUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.launch

data class SubManageUiState(val sub: SubscriptionState? = null) : UiState

sealed interface SubManageSideEffect : UiSideEffect {
    data class Toast(val message: String) : SubManageSideEffect
}

/** 29 구독 관리. */
class SubscriptionViewModel(
    private val subscription: SubscriptionUseCase,
) : BaseViewModel<SubManageUiState, SubManageSideEffect>(SubManageUiState()) {

    init {
        viewModelScope.launch { subscription.observe().collect { setState { copy(sub = it) } } }
    }

    fun cancel() = viewModelScope.launch {
        subscription.cancel()
        sendEffect(SubManageSideEffect.Toast(DamoimStrings.TOAST_SUB_CANCELED))
    }
}
