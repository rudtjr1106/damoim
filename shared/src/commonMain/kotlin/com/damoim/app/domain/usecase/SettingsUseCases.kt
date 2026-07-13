package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.AdminMember
import com.damoim.app.domain.model.BlockedUser
import com.damoim.app.domain.model.Member
import com.damoim.app.domain.model.NotifSettings
import com.damoim.app.domain.model.PermissionType
import com.damoim.app.domain.model.PlanTier
import com.damoim.app.domain.model.SubscriptionPlan
import com.damoim.app.domain.model.SubscriptionState
import com.damoim.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

/** G 그룹(설정·구독·권한·차단·알림) 유스케이스 묶음. */

/** 27·29·49·50 구독. */
class SubscriptionUseCase(private val repo: SettingsRepository) {
    fun observe(): Flow<SubscriptionState> = repo.observeSubscription()
    fun plans(): List<SubscriptionPlan> = repo.plans()
    suspend fun subscribe(tier: PlanTier, proof: com.damoim.app.domain.model.PurchaseProof? = null): DataResult<Unit> =
        repo.subscribe(tier, proof)
    suspend fun cancel(): DataResult<Unit> = repo.cancelSubscription()
}

/** 30·64 운영진 권한. */
class AdminPermissionUseCase(private val repo: SettingsRepository) {
    fun observeAdmins(): Flow<List<AdminMember>> = repo.observeAdmins()
    fun observeAssignable(): Flow<List<Member>> = repo.observeAssignableMembers()
    suspend fun toggle(userId: Long, type: PermissionType): DataResult<Unit> = repo.togglePermission(userId, type)
    suspend fun add(memberId: Long, title: String): DataResult<Unit> = repo.addAdmin(memberId, title)
    suspend fun remove(userId: Long): DataResult<Unit> = repo.removeAdmin(userId)
    suspend fun changeTitle(userId: Long, title: String): DataResult<Unit> = repo.changeAdminTitle(userId, title)
}

/** 83 차단 관리. */
class BlockedUserUseCase(private val repo: SettingsRepository) {
    fun observe(): Flow<List<BlockedUser>> = repo.observeBlocked()
    suspend fun unblock(id: Long): DataResult<Unit> = repo.unblock(id)
}

/** 65 알림 설정. */
class NotifSettingsUseCase(private val repo: SettingsRepository) {
    fun observe(): Flow<NotifSettings> = repo.observeNotifSettings()
    suspend fun update(settings: NotifSettings): DataResult<Unit> = repo.updateNotifSettings(settings)
}
