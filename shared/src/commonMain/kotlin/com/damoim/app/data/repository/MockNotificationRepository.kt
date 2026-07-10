package com.damoim.app.data.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.data.mock.MockStore
import com.damoim.app.domain.model.AppNotification
import com.damoim.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

/** [NotificationRepository]의 Mock 구현 — [MockStore]에 위임. */
class MockNotificationRepository : NotificationRepository {

    override fun observeNotifications(): Flow<List<AppNotification>> = MockStore.notifications

    override suspend fun markAllRead(): DataResult<Unit> {
        MockStore.markAllNotificationsRead()
        return DataResult.Success(Unit)
    }
}
