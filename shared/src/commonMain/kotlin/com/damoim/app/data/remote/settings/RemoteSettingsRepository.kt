package com.damoim.app.data.remote.settings

import com.damoim.app.core.result.DataResult
import com.damoim.app.core.result.getOrNull
import com.damoim.app.data.remote.core.ApiClient
import com.damoim.app.data.remote.core.ApiRoutes
import com.damoim.app.data.remote.core.DataTopic
import com.damoim.app.data.remote.core.RemoteBus
import com.damoim.app.data.remote.core.SharedFlows
import com.damoim.app.data.remote.core.reactiveFlow
import com.damoim.app.domain.model.AdminMember
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
 * [SettingsRepository]의 서버 구현 (G 설정/구독/권한/차단/알림). 변경은 [DataTopic.SETTINGS]만 무효화
 * (운영진 추가/해제는 회원 역할을 바꾸므로 MEMBER·CLUB도). plans()는 인메모리 캐시.
 */
class RemoteSettingsRepository(private val api: ApiClient) : SettingsRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val shared = SharedFlows(scope)
    private var plansCache: List<SubscriptionPlan> = emptyList()

    init {
        scope.launch { primePlans() }
    }

    // ── 구독 ──
    override fun observeSubscription(): Flow<SubscriptionState> = shared.get("subscription") {
        reactiveFlow(DataTopic.SETTINGS, fallback = FREE_STATE) {
            api.getData<SubscriptionStateResponseDto>(ApiRoutes.Subscription.ROOT).getOrNull()?.toDomain()
                ?: FREE_STATE
        }
    }

    override fun plans(): List<SubscriptionPlan> {
        if (plansCache.isEmpty()) scope.launch { primePlans() }
        return plansCache
    }

    override suspend fun subscribe(tier: PlanTier, proof: com.damoim.app.domain.model.PurchaseProof?): DataResult<Unit> =
        api.postUnit(
            ApiRoutes.Subscription.SUBSCRIBE,
            SubscribeRequestDto(
                tier = tier.name,
                channel = if (proof?.platform == "PLAY") "Play Store" else "App Store",
                platform = proof?.platform,
                productId = proof?.productId,
                purchaseToken = proof?.token,
            ),
        ).also { RemoteBus.invalidate(DataTopic.SETTINGS) }

    override suspend fun cancelSubscription(): DataResult<Unit> =
        api.postUnit(ApiRoutes.Subscription.CANCEL).also { RemoteBus.invalidate(DataTopic.SETTINGS) }

    // ── 운영진 권한 ──
    override fun observeAdmins(): Flow<List<AdminMember>> = shared.get("admins") {
        reactiveFlow(DataTopic.SETTINGS, DataTopic.MEMBER, fallback = emptyList()) {
            api.getData<List<AdminMemberResponseDto>>(ApiRoutes.Admins.ROOT).getOrNull()?.map { it.toDomain() }
                ?: emptyList()
        }
    }

    override fun observeAssignableMembers(): Flow<List<Member>> = shared.get("assignable") {
        reactiveFlow(DataTopic.SETTINGS, DataTopic.MEMBER, fallback = emptyList()) {
            api.getData<List<AdminCandidateResponseDto>>(ApiRoutes.Admins.ASSIGNABLE).getOrNull()
                ?.map { it.toMember() } ?: emptyList()
        }
    }

    override suspend fun togglePermission(userId: Long, type: PermissionType): DataResult<Unit> =
        api.postUnit(ApiRoutes.Admins.permissionsToggle(userId), TogglePermissionRequestDto(type.name))
            .also { RemoteBus.invalidate(DataTopic.SETTINGS) }

    override suspend fun addAdmin(memberId: Long, title: String): DataResult<Unit> =
        api.postUnit(ApiRoutes.Admins.ROOT, AddAdminRequestDto(memberId, title))
            .also { RemoteBus.invalidate(DataTopic.SETTINGS, DataTopic.MEMBER, DataTopic.CLUB) }

    override suspend fun removeAdmin(userId: Long): DataResult<Unit> =
        api.deleteUnit(ApiRoutes.Admins.admin(userId))
            .also { RemoteBus.invalidate(DataTopic.SETTINGS, DataTopic.MEMBER, DataTopic.CLUB) }

    override suspend fun changeAdminTitle(userId: Long, title: String): DataResult<Unit> =
        api.patchUnit(ApiRoutes.Admins.title(userId), ChangeTitleRequestDto(title))
            .also { RemoteBus.invalidate(DataTopic.SETTINGS) }

    // ── 알림 설정 ──
    override fun observeNotifSettings(): Flow<NotifSettings> = shared.get("notif-settings") {
        reactiveFlow(DataTopic.SETTINGS, fallback = NotifSettings()) {
            api.getData<NotifSettingsResponseDto>(ApiRoutes.Me.NOTIFICATION_SETTINGS).getOrNull()?.toDomain()
                ?: NotifSettings()
        }
    }

    override suspend fun updateNotifSettings(settings: NotifSettings): DataResult<Unit> =
        api.putUnit(ApiRoutes.Me.NOTIFICATION_SETTINGS, settings.toRequest())
            .also { RemoteBus.invalidate(DataTopic.SETTINGS) }

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
