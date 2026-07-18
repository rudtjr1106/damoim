package com.damoim.app.data.remote.settings

import com.damoim.app.domain.model.AdminMember
import com.damoim.app.domain.model.Member
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.domain.model.MemberStatus
import com.damoim.app.domain.model.NotifSettings
import com.damoim.app.domain.model.PaymentRecord
import com.damoim.app.domain.model.PermissionType
import com.damoim.app.domain.model.PlanFeature
import com.damoim.app.domain.model.PlanTier
import com.damoim.app.domain.model.SubscriptionPlan
import com.damoim.app.domain.model.SubscriptionState
import kotlinx.serialization.Serializable

/** G 설정/구독/권한/차단/알림 그룹 DTO. 서버 settings 패키지 DTO와 JSON 계약 1:1. */

// ── 요청 ──
@Serializable
data class SubscribeRequestDto(
    val tier: String,
    val channel: String = "App Store",
    val platform: String? = null,        // "APP_STORE" | "PLAY" — 서버 영수증 검증용
    val productId: String? = null,
    val purchaseToken: String? = null,   // iOS=JWS/영수증, Android=purchaseToken
)

@Serializable
data class AddAdminRequestDto(val memberId: Long, val title: String)

@Serializable
data class TogglePermissionRequestDto(val type: String)

@Serializable
data class ChangeTitleRequestDto(val title: String)

@Serializable
data class UpdateNotifSettingsRequestDto(
    val push: Boolean = true,
    val newPost: Boolean = true,
    val comment: Boolean = true,
    val scheduleReminder: Boolean = true,
    val reminderLabel: String = "",
    val joinRequest: Boolean = true,
    val eventApply: Boolean = true,
    val dndEnabled: Boolean = false,
    val dndRangeLabel: String = "",
)

// ── 응답 ──
@Serializable
data class PaymentRecordResponseDto(
    val title: String = "",
    val dateLabel: String = "",
    val amountLabel: String = "",
    val channel: String = "App Store",
)

@Serializable
data class SubscriptionStateResponseDto(
    val tier: String = "FREE",
    val planName: String = "",
    val monthlyPriceLabel: String = "",
    val nextBillingLabel: String = "-",
    val memberUsed: Int = 0,
    val memberLimit: Int = 30,
    val payments: List<PaymentRecordResponseDto> = emptyList(),
    val canceled: Boolean = false,
)

@Serializable
data class PlanFeatureResponseDto(val included: Boolean = false, val text: String = "")

@Serializable
data class SubscriptionPlanResponseDto(
    val tier: String,
    val name: String = "",
    val priceKrw: Int = 0,
    val priceLabel: String = "",
    val memberLimitLabel: String = "",
    val features: List<PlanFeatureResponseDto> = emptyList(),
    val recommended: Boolean = false,
)

@Serializable
data class AdminMemberResponseDto(
    val userId: Long,
    val name: String = "",
    val initials: String = "",
    val cohortLabel: String = "",
    val title: String = "운영진",
    val permissions: List<String> = emptyList(),
    val imageUrl: String? = null,
)

@Serializable
data class AdminCandidateResponseDto(
    val memberId: Long,
    val name: String = "",
    val initials: String = "",
    val cohortLabel: String = "",
    val imageUrl: String? = null,
)

@Serializable
data class NotifSettingsResponseDto(
    val push: Boolean = true,
    val newPost: Boolean = true,
    val comment: Boolean = true,
    val scheduleReminder: Boolean = true,
    val reminderLabel: String = "",
    val joinRequest: Boolean = true,
    val eventApply: Boolean = true,
    val dndEnabled: Boolean = false,
    val dndRangeLabel: String = "",
)

// ── 매퍼 ──
internal fun planTierOf(s: String): PlanTier =
    runCatching { PlanTier.valueOf(s) }.getOrDefault(PlanTier.FREE)

internal fun SubscriptionStateResponseDto.toDomain(): SubscriptionState = SubscriptionState(
    tier = planTierOf(tier),
    planName = planName,
    monthlyPriceLabel = monthlyPriceLabel,
    nextBillingLabel = nextBillingLabel,
    memberUsed = memberUsed,
    memberLimit = memberLimit,
    payments = payments.map { PaymentRecord(it.title, it.dateLabel, it.amountLabel, it.channel) },
    canceled = canceled,
)

internal fun SubscriptionPlanResponseDto.toDomain(): SubscriptionPlan = SubscriptionPlan(
    tier = planTierOf(tier),
    name = name,
    priceKrw = priceKrw,
    priceLabel = priceLabel,
    memberLimitLabel = memberLimitLabel,
    features = features.map { PlanFeature(it.included, it.text) },
    recommended = recommended,
)

internal fun AdminMemberResponseDto.toDomain(): AdminMember = AdminMember(
    userId = userId,
    name = name,
    initials = initials,
    cohortLabel = cohortLabel,
    title = title,
    permissions = permissions.mapNotNull { p -> runCatching { PermissionType.valueOf(p) }.getOrNull() }.toSet(),
    imageUrl = imageUrl,
)

/** 운영진 후보 → Member(손실 매핑): 서버가 name/initials만 제공, cohortId 등은 기본값. */
internal fun AdminCandidateResponseDto.toMember(): Member = Member(
    id = memberId,
    name = name,
    initials = initials,
    cohortId = 0L, // 서버가 cohortId 미전송(cohortLabel만) — 후보 피커는 이름/이니셜만 표시
    role = MemberRole.MEMBER,
    status = MemberStatus.ACTIVE,
    profileImageUrl = imageUrl,
)

internal fun NotifSettingsResponseDto.toDomain(): NotifSettings = NotifSettings(
    push = push,
    newPost = newPost,
    comment = comment,
    scheduleReminder = scheduleReminder,
    reminderLabel = reminderLabel,
    joinRequest = joinRequest,
    eventApply = eventApply,
    dndEnabled = dndEnabled,
    dndRangeLabel = dndRangeLabel,
)

internal fun NotifSettings.toRequest(): UpdateNotifSettingsRequestDto = UpdateNotifSettingsRequestDto(
    push = push,
    newPost = newPost,
    comment = comment,
    scheduleReminder = scheduleReminder,
    reminderLabel = reminderLabel,
    joinRequest = joinRequest,
    eventApply = eventApply,
    dndEnabled = dndEnabled,
    dndRangeLabel = dndRangeLabel,
)
