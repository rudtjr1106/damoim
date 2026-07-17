import Foundation
import KakaoSDKAuth
import KakaoSDKCommon
import KakaoSDKUser
import Shared

/// 카카오 로그인 실제 구현 (iOS). 공유 프레임워크(Shared)의 `IosKakaoLogin`을 구현해
/// 앱 시작 시 `IosKakaoRegistry`에 등록하면, Kotlin `MainViewController`가 이 구현을 SocialLogin.provider로 쓴다.
/// (카카오 SDK 의존성이 shared에 들어오지 않도록 Swift 브릿지로 분리 — StoreKit2Billing과 동일 구조.)
///
/// 흐름: 카카오톡 설치 시 카카오톡으로 → 취소 외 실패 시 카카오계정(웹)으로 재시도 → 프로필 조회.
/// (androidApp/KakaoLoginProvider.kt와 동일한 분기)
///
/// ⚠️ 네이티브 앱 키는 Info.plist의 $(KAKAO_NATIVE_APP_KEY) → Secrets.xcconfig(Gradle 생성, 커밋 제외)에서 온다.
///    키를 Kotlin으로 넘기지 않는다 — Swift가 Info.plist에서 직접 읽어 initSDK한다.
final class KakaoLoginProvider: IosKakaoLogin {

    /// Info.plist에 주입된 네이티브 앱 키. Secrets.xcconfig가 없으면 빈 문자열(= 미설정).
    /// ⚠️ 치환이 안 되면 "$(KAKAO_NATIVE_APP_KEY)" 리터럴이 그대로 들어오므로 그 경우도 미설정으로 본다.
    static var nativeAppKey: String {
        let raw = (Bundle.main.object(forInfoDictionaryKey: "KAKAO_NATIVE_APP_KEY") as? String) ?? ""
        return raw.hasPrefix("$(") ? "" : raw
    }

    var isConfigured: Bool { !KakaoLoginProvider.nativeAppKey.isEmpty }

    func login(onResult: @escaping (SocialUser?) -> Void) {
        loginForToken { oauthToken in
            guard let token = oauthToken else { onResult(nil); return }
            // 서버 로그인(POST /api/auth/kakao)이 이 토큰을 카카오에 재검증한다 → 반드시 실어 보낸다.
            self.fetchMe(accessToken: token.accessToken, onResult: onResult)
        }
    }

    /// 토큰 취득. 카카오 SDK 콜백은 성공/실패/취소 중 정확히 1회만 호출된다.
    private func loginForToken(_ completion: @escaping (OAuthToken?) -> Void) {
        let accountLogin = {
            UserApi.shared.loginWithKakaoAccount { token, _ in completion(token) }
        }
        // 시뮬레이터엔 카카오톡이 없고 kakaokompassauth 조회도 실패 → 항상 계정(웹) 경로로 간다.
        if UserApi.isKakaoTalkLoginAvailable() {
            UserApi.shared.loginWithKakaoTalk { token, error in
                if let token = token { completion(token); return }
                // 사용자가 명시적으로 취소한 경우 외에는 카카오계정 로그인으로 폴백
                if let e = error as? SdkError, e.isClientFailed,
                   e.getClientError().reason == .Cancelled {
                    completion(nil); return
                }
                accountLogin()
            }
        } else {
            accountLogin()
        }
    }

    private func fetchMe(accessToken: String, onResult: @escaping (SocialUser?) -> Void) {
        UserApi.shared.me { user, _ in
            guard let user = user else { onResult(nil); return }
            // ⚠️ 콘솔 동의항목이 '회원번호'만이면 nickname/email/profileImageUrl은 전부 nil이 정상이다.
            //    닉네임은 빈 문자열로 두고 사용자가 직접 입력한다(안드로이드·서버와 동일 규약).
            //    임의 문자열을 채우면 서버의 신규가입 분기가 오염된다.
            onResult(SocialUser(
                id: user.id ?? 0,
                nickname: user.kakaoAccount?.profile?.nickname ?? "",
                email: user.kakaoAccount?.email,
                profileImageUrl: user.kakaoAccount?.profile?.profileImageUrl?.absoluteString,
                accessToken: accessToken
            ))
        }
    }
}

/// 앱 시작 시 호출 — 카카오 SDK 초기화 + Kotlin 레지스트리 등록.
/// 키가 없으면 아무것도 하지 않는다(= provider 미등록 → 로그인이 KAKAO_NOT_CONFIGURED로 실패).
func registerKakaoLogin() {
    // ⚠️ initSDK 없이 로그인을 호출하면 SDK 내부 scheme()이 try! 강제 언랩이라 즉시 크래시(MustInitAppKey).
    //    따라서 키가 없으면 initSDK도 등록도 하지 않아 로그인 경로 자체가 열리지 않게 한다.
    let key = KakaoLoginProvider.nativeAppKey
    guard !key.isEmpty else { return }
    KakaoSDK.initSDK(appKey: key)
    // ⚠️ 생성 프로퍼티 이름은 빌드 시 Shared 프레임워크 헤더 기준으로 자동 매핑된다.
    IosKakaoRegistry.shared.impl = KakaoLoginProvider()
}
