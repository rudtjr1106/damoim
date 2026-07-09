package com.damoim.app.presentation.notification

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.AppNotification
import com.damoim.app.domain.usecase.GetNotificationsUseCase
import kotlinx.coroutines.launch

data class NotificationUiState(
    val isLoading: Boolean = true,
    val notifications: List<AppNotification> = emptyList(),
) : UiState

/** 알림은 로컬 처리(모두 읽음)만 있어 사이드이펙트 미사용. */
sealed interface NotificationSideEffect : UiSideEffect

/** 화면 37 알림 목록 / 74 빈 상태. */
class NotificationViewModel(
    private val getNotifications: GetNotificationsUseCase,
) : BaseViewModel<NotificationUiState, NotificationSideEffect>(NotificationUiState()) {

    init { load() }

    private fun load() = viewModelScope.launch {
        val result = getNotifications()
        setState { copy(isLoading = false) }
        handleResult(result, onSuccess = { list -> setState { copy(notifications = list) } })
    }

    fun onMarkAllRead() = setState {
        copy(notifications = notifications.map { it.copy(isUnread = false) })
    }
}
