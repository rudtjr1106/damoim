import SwiftUI
import KakaoSDKAuth

@main
struct iOSApp: App {
    init() {
        // StoreKit 2 구독 결제 구현을 공유 모듈에 등록(iOS 15+). 미호출 시 Kotlin이 StoreKit 1 폴백 사용.
        if #available(iOS 15.0, *) {
            registerStoreKit2Billing()
        }
        // 카카오 SDK 초기화 + 로그인 구현 등록. 네이티브 앱 키(local.properties)가 없으면 no-op.
        registerKakaoLogin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                // 카카오톡 앱에서 인증 후 돌아오는 콜백 처리(실기기 전용).
                // 카카오계정(웹) 로그인은 ASWebAuthenticationSession이 콜백을 자체 인터셉트하므로
                // 이 경로를 타지 않는다 = 시뮬레이터는 이 핸들러 없이도 동작한다.
                .onOpenURL { url in
                    if AuthApi.isKakaoTalkLoginUrl(url) {
                        _ = AuthController.handleOpenUrl(url: url)
                    }
                }
        }
    }
}
