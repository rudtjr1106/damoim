package com.damoim.app.presentation.notification

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.AppNotification
import com.damoim.app.domain.usecase.GetNotificationsUseCase
import com.damoim.app.domain.usecase.MarkNotificationReadUseCase
import com.damoim.app.domain.usecase.MarkNotificationsReadUseCase
import kotlinx.coroutines.launch

data class NotificationUiState(
    val isLoading: Boolean = true,
    val notifications: List<AppNotification> = emptyList(),
) : UiState

/** 알림은 로컬 처리(모두 읽음)만 있어 사이드이펙트 미사용. */
sealed interface NotificationSideEffect : UiSideEffect

/** 화면 37 알림 목록 / 74 빈 상태. 모두 읽음이 홈 벨 배지에도 반영된다. */
class NotificationViewModel(
    getNotifications: GetNotificationsUseCase,
    private val markAllRead: MarkNotificationsReadUseCase,
    private val markRead: MarkNotificationReadUseCase,
) : BaseViewModel<NotificationUiState, NotificationSideEffect>(NotificationUiState()) {

    init {
        viewModelScope.launch {
            getNotifications().collect { list -> setState { copy(isLoading = false, notifications = list) } }
        }
    }

    fun onMarkAllRead() = viewModelScope.launch { markAllRead() }

    /** 알림 터치 → 그 알림만 읽음. 즉시 낙관적 반영 후 서버 반영(재조회로 확정). */
    fun onMarkRead(id: Long) {
        setState { copy(notifications = notifications.map { if (it.id == id) it.copy(isUnread = false) else it }) }
        viewModelScope.launch { markRead(id) }
    }
}
