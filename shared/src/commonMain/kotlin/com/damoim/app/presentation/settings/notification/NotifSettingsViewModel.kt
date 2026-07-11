package com.damoim.app.presentation.settings.notification

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.NotifSettings
import com.damoim.app.domain.usecase.NotifSettingsUseCase
import kotlinx.coroutines.launch

data class NotifUiState(val settings: NotifSettings = NotifSettings()) : UiState

sealed interface NotifSideEffect : UiSideEffect

/** 65 알림 설정. 토글/선택 변경 시 즉시 저장(낙관적). */
class NotifSettingsViewModel(
    private val notif: NotifSettingsUseCase,
) : BaseViewModel<NotifUiState, NotifSideEffect>(NotifUiState()) {

    init {
        viewModelScope.launch { notif.observe().collect { setState { copy(settings = it) } } }
    }

    fun update(settings: NotifSettings) = viewModelScope.launch { notif.update(settings) }
}
