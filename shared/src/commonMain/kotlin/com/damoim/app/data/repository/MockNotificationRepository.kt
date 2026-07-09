package com.damoim.app.data.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.data.mock.MockData
import com.damoim.app.domain.model.AppNotification
import com.damoim.app.domain.repository.NotificationRepository
import kotlinx.coroutines.delay

/** [NotificationRepository]의 Mock 구현. */
class MockNotificationRepository : NotificationRepository {
    override suspend fun getNotifications(): DataResult<List<AppNotification>> {
        delay(400L)
        return DataResult.Success(MockData.notifications)
    }
}
