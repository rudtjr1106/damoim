package com.damoim.app.domain.model

/**
 * 인앱 구독 상품 ID(스토어 계약). App Store Connect / Play Console에 이 ID로 **자동갱신 구독** 상품을
 * 등록해야 실제 결제가 이뤄진다(등록 안 하면 상품 조회 실패로 결제 실패 처리).
 *
 * ⚠️ 결제 성공만으로 구독을 활성화하지 말고, 출시 전 서버가 영수증을 스토어로 재검증해야 한다
 * (현재 서버 subscribe는 tier만 신뢰 — 하드닝 2차 항목).
 */
object BillingProducts {
    const val STANDARD = "com.damoim.app.subscription.standard"
    const val PRO = "com.damoim.app.subscription.pro"

    fun forTier(tier: PlanTier): String = when (tier) {
        PlanTier.PRO -> PRO
        else -> STANDARD
    }
}

/**
 * 결제 증빙 — 서버가 스토어로 재검증하기 위한 자료. [token]은 iOS=StoreKit2 서명 트랜잭션(JWS) 또는
 * StoreKit1 앱 영수증(base64), Android=Play purchaseToken. 결제 성공 시에만 채워진다.
 */
data class PurchaseProof(
    val platform: String,   // "APP_STORE" | "PLAY"
    val productId: String,
    val token: String,
)
