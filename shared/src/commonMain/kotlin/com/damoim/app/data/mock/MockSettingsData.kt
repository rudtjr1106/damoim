package com.damoim.app.data.mock

import com.damoim.app.domain.model.AdminMember
import com.damoim.app.domain.model.BlockedUser
import com.damoim.app.domain.model.PaymentRecord
import com.damoim.app.domain.model.PermissionType
import com.damoim.app.domain.model.PlanFeature
import com.damoim.app.domain.model.PlanTier
import com.damoim.app.domain.model.SubscriptionPlan
import com.damoim.app.domain.model.SubscriptionState

/** G 그룹(구독·권한·차단) 시드 데이터. */
object MockSettingsData {

    const val FREE_LIMIT = 30
    const val STANDARD_LIMIT = 100

    /** 27 구독 플랜 3종. */
    val plans: List<SubscriptionPlan> = listOf(
        SubscriptionPlan(
            tier = PlanTier.FREE, name = "무료", priceKrw = 0, priceLabel = "₩0", memberLimitLabel = "회원 30명 미만",
            features = listOf(
                PlanFeature(true, "회원 30명 미만"),
                PlanFeature(true, "게시판·일정·회원 관리 등 핵심 기능"),
                PlanFeature(false, "파일 저장 용량 1GB"),
            ),
        ),
        SubscriptionPlan(
            tier = PlanTier.STANDARD, name = "스탠다드", priceKrw = 9900, priceLabel = "₩9,900", memberLimitLabel = "회원 100명까지",
            recommended = true,
            features = listOf(
                PlanFeature(true, "회원 100명까지"),
                PlanFeature(true, "모든 핵심 기능 + 공지 확인율 통계"),
                PlanFeature(true, "파일 저장 용량 20GB"),
            ),
        ),
        SubscriptionPlan(
            tier = PlanTier.PRO, name = "프로", priceKrw = 19900, priceLabel = "₩19,900", memberLimitLabel = "회원 무제한",
            features = listOf(
                PlanFeature(true, "회원 무제한"),
                PlanFeature(true, "스탠다드 전체 + 우선 지원"),
                PlanFeature(true, "파일 저장 용량 100GB"),
            ),
        ),
    )

    fun planOf(tier: PlanTier): SubscriptionPlan = plans.first { it.tier == tier }

    /** 무료(기본) 구독 상태 — memberUsed는 현재 동아리 회원 수. */
    fun freeState(memberUsed: Int) = SubscriptionState(
        tier = PlanTier.FREE, planName = "무료 플랜", monthlyPriceLabel = "₩0",
        nextBillingLabel = "-", memberUsed = memberUsed, memberLimit = FREE_LIMIT, payments = emptyList(),
    )

    /** 구독 활성 상태(스탠다드). */
    fun activeState(tier: PlanTier, memberUsed: Int, nextBillingLabel: String, payments: List<PaymentRecord>): SubscriptionState {
        val plan = planOf(tier)
        return SubscriptionState(
            tier = tier, planName = "${plan.name} 플랜", monthlyPriceLabel = plan.priceLabel,
            nextBillingLabel = nextBillingLabel,
            memberUsed = memberUsed, memberLimit = if (tier == PlanTier.PRO) 9999 else STANDARD_LIMIT,
            payments = payments,
        )
    }

    /** 30 운영진 시드 — 데모 명부의 STAFF(최유진·강도윤). */
    fun seedAdmins(): List<AdminMember> = listOf(
        AdminMember(503, "최유진", "유진", "23기", "부회장", setOf(PermissionType.NOTICE_WRITE, PermissionType.JOIN_APPROVE, PermissionType.SCHEDULE_MANAGE, PermissionType.BOARD_MANAGE)),
        AdminMember(506, "강도윤", "도윤", "25기", "총무", setOf(PermissionType.NOTICE_WRITE, PermissionType.SCHEDULE_MANAGE, PermissionType.MEMBER_MANAGE)),
    )

    /** 83 차단 시드. */
    fun seedBlocked(): List<BlockedUser> = listOf(
        BlockedUser(9801, "탈퇴한 사용자", "익명", "2026.06.18 차단", isWithdrawn = true),
        BlockedUser(9802, "박지훈", "지훈", "2026.05.30 차단"),
    )
}
