package com.damoim.app.domain.model

// ══════════ 구독(27·29·49·50) ══════════

enum class PlanTier { FREE, STANDARD, PRO }

/** 27 구독 플랜 카드 한 장. */
data class SubscriptionPlan(
    val tier: PlanTier,
    val name: String,
    val priceKrw: Int,               // 월 요금(₩), FREE=0
    val priceLabel: String,          // "₩9,900"
    val memberLimitLabel: String,    // "회원 100명까지"
    val features: List<PlanFeature>,
    val recommended: Boolean = false,
)

data class PlanFeature(val included: Boolean, val text: String)

/** 29 구독 관리 상태. */
data class SubscriptionState(
    val tier: PlanTier,
    val planName: String,            // "무료 플랜" / "스탠다드 플랜"
    val monthlyPriceLabel: String,   // "₩9,900" / "₩0"
    val nextBillingLabel: String,    // "2026.08.07" (무료면 "-")
    val memberUsed: Int,
    val memberLimit: Int,            // FREE=30
    val payments: List<PaymentRecord> = emptyList(),
) {
    val active: Boolean get() = tier != PlanTier.FREE
    val overLimit: Boolean get() = memberUsed > memberLimit
    val usageRatio: Float get() = if (memberLimit <= 0) 0f else (memberUsed.toFloat() / memberLimit).coerceIn(0f, 1f)
}

/** 결제 내역 한 줄. */
data class PaymentRecord(val title: String, val dateLabel: String, val amountLabel: String, val channel: String = "App Store")

// ══════════ 운영진 권한(30·64) ══════════

/** 위임 가능한 권한 종류 — 지금까지 구현한 권한 필요 기능 전부. */
enum class PermissionType(val label: String) {
    NOTICE_WRITE("공지 작성"),
    JOIN_APPROVE("가입 승인·거절"),
    BOARD_MANAGE("게시판·자료 관리"),
    MEMBER_MANAGE("회원·기수 관리"),
    SCHEDULE_MANAGE("일정·이벤트 관리"),
    CLUB_SETTINGS("동아리 정보·코드"),
}

/** 30 운영진 카드 — 회원 + 직함 + 권한 셋. */
data class AdminMember(
    val userId: Long,
    val name: String,
    val initials: String,
    val cohortLabel: String,         // "23기"
    val title: String,               // "부회장" / "총무"
    val permissions: Set<PermissionType>,
    val imageUrl: String? = null,
)

// ══════════ 알림 설정(65) ══════════

/** 65 알림 설정 — 토글 묶음 + 리마인드/방해금지. '운영' 섹션은 동아리장·운영진만. */
data class NotifSettings(
    val push: Boolean = true,
    val newPost: Boolean = true,
    val comment: Boolean = true,
    val scheduleReminder: Boolean = true,
    val reminderLabel: String = "1일 전 · 1시간 전",
    val joinRequest: Boolean = true,      // 운영
    val eventApply: Boolean = true,       // 운영
    val dndEnabled: Boolean = false,
    val dndRangeLabel: String = "23:00 ~ 08:00",
)
