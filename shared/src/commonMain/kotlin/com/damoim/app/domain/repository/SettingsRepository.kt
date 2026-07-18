package com.damoim.app.domain.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.AdminMember
import com.damoim.app.domain.model.PurchaseProof
import com.damoim.app.domain.model.Member
import com.damoim.app.domain.model.NotifSettings
import com.damoim.app.domain.model.PermissionType
import com.damoim.app.domain.model.PlanTier
import com.damoim.app.domain.model.SubscriptionPlan
import com.damoim.app.domain.model.SubscriptionState
import kotlinx.coroutines.flow.Flow

/** G 그룹(구독·권한·알림설정) 저장소. */
interface SettingsRepository {
    // 구독(27·29·49·50)
    fun observeSubscription(): Flow<SubscriptionState>
    fun plans(): List<SubscriptionPlan>
    suspend fun subscribe(tier: PlanTier, proof: PurchaseProof? = null): DataResult<Unit>
    suspend fun cancelSubscription(): DataResult<Unit>

    // 운영진 권한(30·64)
    fun observeAdmins(): Flow<List<AdminMember>>
    /** 30 운영진 추가용 — 아직 운영진이 아닌 일반 회원 목록. */
    fun observeAssignableMembers(): Flow<List<Member>>
    suspend fun togglePermission(userId: Long, type: PermissionType): DataResult<Unit>
    suspend fun addAdmin(memberId: Long, title: String): DataResult<Unit>
    suspend fun removeAdmin(userId: Long): DataResult<Unit>
    suspend fun changeAdminTitle(userId: Long, title: String): DataResult<Unit>

    // 알림 설정(65)
    fun observeNotifSettings(): Flow<NotifSettings>
    suspend fun updateNotifSettings(settings: NotifSettings): DataResult<Unit>
}
