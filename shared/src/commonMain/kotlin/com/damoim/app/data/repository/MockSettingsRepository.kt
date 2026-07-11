package com.damoim.app.data.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.data.mock.MockStore
import com.damoim.app.domain.model.AdminMember
import com.damoim.app.domain.model.BlockedUser
import com.damoim.app.domain.model.Member
import com.damoim.app.domain.model.NotifSettings
import com.damoim.app.domain.model.PermissionType
import com.damoim.app.domain.model.PlanTier
import com.damoim.app.domain.model.SubscriptionPlan
import com.damoim.app.domain.model.SubscriptionState
import com.damoim.app.domain.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/** [SettingsRepository]의 Mock 구현 — [MockStore]에 위임. */
class MockSettingsRepository : SettingsRepository {

    override fun observeSubscription(): Flow<SubscriptionState> = MockStore.subscriptionFlow()
    override fun plans(): List<SubscriptionPlan> = MockStore.subscriptionPlans()

    override suspend fun subscribe(tier: PlanTier): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.subscribe(tier)
        return DataResult.Success(Unit)
    }

    override suspend fun cancelSubscription(): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.cancelSubscription()
        return DataResult.Success(Unit)
    }

    override fun observeAdmins(): Flow<List<AdminMember>> = MockStore.adminsFlow()
    override fun observeAssignableMembers(): Flow<List<Member>> = MockStore.assignableMembersFlow()

    override suspend fun togglePermission(userId: Long, type: PermissionType): DataResult<Unit> {
        MockStore.togglePermission(userId, type)
        return DataResult.Success(Unit)
    }

    override suspend fun addAdmin(memberId: Long, title: String): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.addAdmin(memberId, title)
        return DataResult.Success(Unit)
    }

    override suspend fun removeAdmin(userId: Long): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.removeAdmin(userId)
        return DataResult.Success(Unit)
    }

    override suspend fun changeAdminTitle(userId: Long, title: String): DataResult<Unit> {
        MockStore.changeAdminTitle(userId, title)
        return DataResult.Success(Unit)
    }

    override fun observeBlocked(): Flow<List<BlockedUser>> = MockStore.blockedFlow()

    override suspend fun unblock(id: Long): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.unblock(id)
        return DataResult.Success(Unit)
    }

    override fun observeNotifSettings(): Flow<NotifSettings> = MockStore.notifSettingsFlow()

    override suspend fun updateNotifSettings(settings: NotifSettings): DataResult<Unit> {
        MockStore.updateNotifSettings(settings)
        return DataResult.Success(Unit)
    }

    private companion object {
        const val WRITE_DELAY_MS = 350L
    }
}
