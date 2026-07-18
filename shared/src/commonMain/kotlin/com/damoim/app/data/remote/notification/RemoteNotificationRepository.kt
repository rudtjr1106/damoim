package com.damoim.app.data.remote.notification

import com.damoim.app.core.result.DataResult
import com.damoim.app.core.result.getOrNull
import com.damoim.app.data.remote.core.ApiClient
import com.damoim.app.data.remote.core.ApiRoutes
import com.damoim.app.data.remote.core.DataTopic
import com.damoim.app.data.remote.core.RemoteBus
import com.damoim.app.data.remote.core.SharedFlows
import com.damoim.app.data.remote.core.reactiveFlow
import com.damoim.app.domain.model.AppNotification
import com.damoim.app.domain.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow

/** [NotificationRepository]의 서버 구현 (B, 화면 37/74). */
class RemoteNotificationRepository(private val api: ApiClient) : NotificationRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val shared = SharedFlows(scope)

    override fun observeNotifications(): Flow<List<AppNotification>> = shared.get("notifications") {
        reactiveFlow(DataTopic.NOTIFICATION, fallback = emptyList()) {
            api.getData<List<NotificationResponseDto>>(ApiRoutes.Me.NOTIFICATIONS).getOrNull()
                ?.map { it.toDomain() } ?: emptyList()
        }
    }

    override suspend fun markAllRead(): DataResult<Unit> =
        api.postUnit(ApiRoutes.Me.NOTIFICATIONS_READ_ALL)
            .also { RemoteBus.invalidate(DataTopic.NOTIFICATION, DataTopic.CLUB) }

    override suspend fun markRead(id: Long): DataResult<Unit> =
        api.postUnit(ApiRoutes.Me.notificationRead(id))
            .also { RemoteBus.invalidate(DataTopic.NOTIFICATION, DataTopic.CLUB) }
}
