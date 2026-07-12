package com.damoim.app.data.remote.core

/**
 * 서버 연결 설정. 플랫폼 진입점(MainActivity/MainViewController)에서 앱 시작 시 주입한다.
 * - 기본 baseUrl은 Android 에뮬레이터에서 호스트의 로컬 서버(10.0.2.2:8080).
 * - 실기기/배포 환경은 BuildConfig 등으로 실제 주소를 넣는다.
 */
object RemoteConfig {
    var baseUrl: String = "http://10.0.2.2:8080"
    var enableLogging: Boolean = true
}

/**
 * 원격 계층 런타임 의존성 홀더(수동 DI). [com.damoim.app.core.social.SocialLogin]과 같은 주입 패턴.
 * 플랫폼 진입점에서 [tokenStore]를 영속 구현으로 교체한다.
 */
object RemoteEnv {
    var tokenStore: TokenStore = InMemoryTokenStore()

    /**
     * 로그인 사용자 id. RemoteAuthRepository가 로그인/‘내정보’ 조회 시 갱신, 로그아웃 시 0.
     * 게시글 상세의 authorId 파생(isMine ? currentUserId : 0) 등 세션 신원이 필요한 매퍼에서 사용.
     */
    var currentUserId: Long = 0L
}
