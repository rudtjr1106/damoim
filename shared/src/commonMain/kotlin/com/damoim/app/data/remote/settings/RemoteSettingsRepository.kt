package com.damoim.app.data.remote.settings

import com.damoim.app.core.result.DataResult
import com.damoim.app.core.result.getOrNull
import com.damoim.app.data.remote.core.ApiClient
import com.damoim.app.data.remote.core.ApiRoutes
import com.damoim.app.data.remote.core.RemoteBus
import com.damoim.app.data.remote.core.reactiveFlow
import com.damoim.app.domain.model.AdminMember
import com.damoim.app.domain.model.BlockedUser
import com.damoim.app.domain.model.Member
import com.damoim.app.domain.model.NotifSettings
import com.damoim.app.domain.model.PermissionType
import com.damoim.app.domain.model.PlanTier
import com.damoim.app.domain.model.SubscriptionPlan
import com.damoim.app.domain.model.SubscriptionState
import com.damoim.app.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * [SettingsRepository]의 서버 구현 (G 설정/구독/권한/차단/알림).
 *
 * plans()가 동기 계약이라 정적 플랜(V2 시드)을 인메모리로 캐시(init 프라임 + 비었으면 재프라임).
 * subscribe/cancel/updateNotif는 서버가 상태를 반환하나 인터페이스가 Unit이라 폐기 + 전역 무효화로 재조회.
 */
class RemoteSettingsRepository(private val api: ApiClient) : SettingsRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var plansCache: List<SubscriptionPlan> = emptyList()

    init {
        scope.launch { primePlans() }
    }

    // ── 구독 ──
    override fun observeSubscription(): Flow<SubscriptionState> = reactiveFlow(FREE_STATE) {
        api.getData<SubscriptionStateResponseDto>(ApiRoutes.Subscription.ROOT).getOrNull()?.toDomain() ?: FREE_STATE
    }

    override fun plans(): List<SubscriptionPlan> {
        if (plansCache.isEmpty()) scope.launch { primePlans() } // 콜드 재시도
        return plansCache
    }

    override suspend fun subscribe(tier: PlanTier): DataResult<Unit> =
        api.postUnit(ApiRoutes.Subscription.SUBSCRIBE, SubscribeRequestDto(tier = tier.name))
            .also { RemoteBus.invalidate() }

    override suspend fun cancelSubscription(): DataResult<Unit> =
        api.postUnit(ApiRoutes.Subscription.CANCEL).also { RemoteBus.invalidate() }

    // ── 운영진 권한 ──
    override fun observeAdmins(): Flow<List<AdminMember>> = reactiveFlow(emptyList()) {
        api.getData<List<AdminMemberResponseDto>>(ApiRoutes.Admins.ROOT).getOrNull()?.map { it.toDomain() }
            ?: emptyList()
    }

    override fun observeAssignableMembers(): Flow<List<Member>> = reactiveFlow(emptyList()) {
        api.getData<List<AdminCandidateResponseDto>>(ApiRoutes.Admins.ASSIGNABLE).getOrNull()
            ?.map { it.toMember() } ?: emptyList()
    }

    override suspend fun togglePermission(userId: Long, type: PermissionType): DataResult<Unit> =
        api.postUnit(ApiRoutes.Admins.permissionsToggle(userId), TogglePermissionRequestDto(type.name))
            .also { RemoteBus.invalidate() }

    override suspend fun addAdmin(memberId: Long, title: String): DataResult<Unit> =
        api.postUnit(ApiRoutes.Admins.ROOT, AddAdminRequestDto(memberId, title)).also { RemoteBus.invalidate() }

    override suspend fun removeAdmin(userId: Long): DataResult<Unit> =
        api.deleteUnit(ApiRoutes.Admins.admin(userId)).also { RemoteBus.invalidate() }

    override suspend fun changeAdminTitle(userId: Long, title: String): DataResult<Unit> =
        api.patchUnit(ApiRoutes.Admins.title(userId), ChangeTitleRequestDto(title)).also { RemoteBus.invalidate() }

    // ── 차단 ──
    override fun observeBlocked(): Flow<List<BlockedUser>> = reactiveFlow(emptyList()) {
        api.getData<List<BlockedUserResponseDto>>(ApiRoutes.Blocked.ROOT).getOrNull()?.map { it.toDomain() }
            ?: emptyList()
    }

    override suspend fun unblock(id: Long): DataResult<Unit> =
        api.deleteUnit(ApiRoutes.Blocked.byId(id)).also { RemoteBus.invalidate() }

    // ── 알림 설정 ──
    override fun observeNotifSettings(): Flow<NotifSettings> = reactiveFlow(NotifSettings()) {
        api.getData<NotifSettingsResponseDto>(ApiRoutes.Me.NOTIFICATION_SETTINGS).getOrNull()?.toDomain()
            ?: NotifSettings()
    }

    override suspend fun updateNotifSettings(settings: NotifSettings): DataResult<Unit> =
        api.putUnit(ApiRoutes.Me.NOTIFICATION_SETTINGS, settings.toRequest()).also { RemoteBus.invalidate() }

    private suspend fun primePlans() {
        api.getData<List<SubscriptionPlanResponseDto>>(ApiRoutes.Subscription.PLANS).getOrNull()
            ?.let { plansCache = it.map { dto -> dto.toDomain() } }
    }

    private companion object {
        val FREE_STATE = SubscriptionState(
            tier = PlanTier.FREE,
            planName = "무료 플랜",
            monthlyPriceLabel = "₩0",
            nextBillingLabel = "-",
            memberUsed = 0,
            memberLimit = 30,
        )
    }
}
