import Foundation
import StoreKit
import Shared

/// StoreKit 2 기반 구독 결제. 공유 프레임워크(Shared)의 `IosSubscriptionBilling`을 구현해
/// 앱 시작 시 `IosBillingRegistry`에 등록하면, Kotlin `rememberSubscriptionBilling`이 이 구현을 우선 사용한다.
/// (StoreKit 2는 Swift async 전용이라 Kotlin/Native에서 직접 호출 불가 → 이 Swift 브릿지로 연결.)
///
/// ⚠️ 실동작: App Store Connect에 productId(com.damoim.app.subscription.{standard,pro})로 자동갱신 구독을 등록.
///    로컬 시뮬레이터 테스트는 Products.storekit(StoreKit Configuration)로 가능.
/// ⚠️ 결제 성공 시 서버 검증용 JWS 서명 트랜잭션을 함께 넘긴다 → 서버가 App Store로 재검증해야 실제 활성화.
@available(iOS 15.0, *)
final class StoreKit2Billing: IosSubscriptionBilling {

    func purchase(productId: String, onResult: @escaping (BillingResult, String?) -> Void) {
        Task {
            do {
                let products = try await Product.products(for: [productId])
                guard let product = products.first else {
                    await complete(onResult, .failure, nil); return   // 미등록 상품
                }
                let result = try await product.purchase()
                switch result {
                case .success(let verification):
                    // verification.jwsRepresentation = 서버가 검증할 서명 트랜잭션(JWS)
                    let jws = verification.jwsRepresentation
                    if case .verified(let transaction) = verification {
                        await transaction.finish()
                        await complete(onResult, .success, jws)
                    } else {
                        await complete(onResult, .failure, nil)         // 서명 미검증
                    }
                case .userCancelled:
                    await complete(onResult, .cancelled, nil)
                case .pending:
                    break                                                // 승인 대기(가족 공유 등)
                @unknown default:
                    await complete(onResult, .failure, nil)
                }
            } catch {
                await complete(onResult, .failure, nil)
            }
        }
    }

    /// Compose 상태를 건드리므로 콜백은 메인 스레드에서 호출.
    @MainActor
    private func complete(_ cb: @escaping (BillingResult, String?) -> Void, _ result: BillingResult, _ token: String?) {
        cb(result, token)
    }
}

/// 앱 시작 시 호출해 StoreKit 2 구현을 등록. 미호출 시 Kotlin이 StoreKit 1 폴백을 쓴다(서버 검증 불가).
@available(iOS 15.0, *)
func registerStoreKit2Billing() {
    // ⚠️ 생성 enum/프로퍼티 이름은 빌드 시 Shared 프레임워크 헤더 기준으로 자동 매핑된다.
    //    BillingResult.success 등이 안 맞으면 생성된 Shared 스위프트 인터페이스에서 정확한 케이스명 확인.
    IosBillingRegistry.shared.impl = StoreKit2Billing()
}
