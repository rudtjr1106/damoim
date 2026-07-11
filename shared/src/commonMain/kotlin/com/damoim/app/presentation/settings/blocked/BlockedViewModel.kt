package com.damoim.app.presentation.settings.blocked

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.BlockedUser
import com.damoim.app.domain.usecase.BlockedUserUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.launch

data class BlockedUiState(val isLoading: Boolean = true, val users: List<BlockedUser> = emptyList()) : UiState {
    val isEmpty: Boolean get() = !isLoading && users.isEmpty()
}

sealed interface BlockedSideEffect : UiSideEffect {
    data class Toast(val message: String) : BlockedSideEffect
}

/** 83 차단한 사용자 관리. */
class BlockedViewModel(
    private val blocked: BlockedUserUseCase,
) : BaseViewModel<BlockedUiState, BlockedSideEffect>(BlockedUiState()) {

    init {
        viewModelScope.launch { blocked.observe().collect { setState { copy(isLoading = false, users = it) } } }
    }

    fun unblock(id: Long) = viewModelScope.launch {
        blocked.unblock(id)
        sendEffect(BlockedSideEffect.Toast(DamoimStrings.TOAST_UNBLOCKED))
    }
}
