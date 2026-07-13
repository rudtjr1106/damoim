import SwiftUI

@main
struct iOSApp: App {
    init() {
        // StoreKit 2 구독 결제 구현을 공유 모듈에 등록(iOS 15+). 미호출 시 Kotlin이 StoreKit 1 폴백 사용.
        if #available(iOS 15.0, *) {
            registerStoreKit2Billing()
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}