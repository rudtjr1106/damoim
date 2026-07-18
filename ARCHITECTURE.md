# 다모임 아키텍처

Compose Multiplatform(Android + iOS) 앱. 클린 아키텍처를 단일 `:shared` 모듈 안에서
**패키지 레이어링**으로 구현한다. 개요는 [README](./README.md)의 "아키텍처" 절을 참고하고,
이 문서는 데이터 흐름·상태관리·네비게이션·서버 통합을 더 깊이 다룬다.

> 현재 범위: 디자인 **A~G 전 그룹 구현 + Ktor 서버 통합 완료**. 데이터는 전부 실서버에서 오며
> Mock 저장소는 소스에서 제거되었다.

## 레이어 구조

```
shared/src/commonMain/kotlin/com/damoim/app/
├─ App.kt                     # 진입 컴포저블 → DamoimTheme { RootNavHost() }
│
├─ core/                      # 레이어 무관 공통 인프라 (프레임워크 비의존)
│  ├─ result/DataResult.kt    # 성공/실패 래퍼 + DataError
│  ├─ mvi/BaseViewModel.kt    # MVI-lite 베이스 (uiState / sideEffect / handleResult)
│  ├─ di/AppGraph.kt          # 수동 Service Locator (Koin 도입 전)
│  ├─ social/                 # SocialLoginProvider (카카오 주입점)
│  └─ deeplink/               # 딥링크 소비
│
├─ domain/                    # 순수 비즈니스 (안드로이드/컴포즈 의존 0)
│  ├─ model/                  # 도메인 모델 15
│  ├─ repository/             # 저장소 인터페이스 8
│  └─ usecase/                # UseCase 39
│
├─ data/remote/               # domain.repository 구현 = 서버 통합 계층
│  ├─ core/                   # ApiClient · ApiEnvelope · ApiRoutes · TokenStore
│  │                          #  RemoteBus · SessionEvents · RawHttp · HttpClientProvider …
│  └─ {auth,board,club,notification,report,resource,schedule,settings}/
│                             #  Remote*Repository(인터페이스 유일 구현) + *Dtos(@Serializable)
│
├─ platform/                  # expect 선언 (androidMain / iosMain 에 actual)
│
└─ presentation/
   ├─ RootNavHost.kt          # AppFlow(Loading/Auth/Main) 최상위 라우팅 판정
   ├─ MainNavHost.kt          # MainDestination 33개 수동 백스택
   ├─ theme/                  # DamoimTheme (Color/Type/Shape) — Pretendard
   ├─ component/              # 공용 UI (버튼·시트·다이얼로그·NetworkImage …)
   └─ <feature>/              # auth · home · board · club · member · profile
                              #  schedule · settings · resource · notification …
```

### 의존성 방향

```
presentation ──▶ domain ◀── data/remote
        └────────▶ core ◀────────┘
```

- **domain**은 아무것도 모른다(순수 Kotlin + 코루틴).
- **data/remote**는 domain의 Repository 인터페이스를 구현하고, 서버 JSON을 도메인 모델로 변환한다.
- **presentation**은 domain(UseCase/Model)에만 의존하고, 구현체는 `AppGraph`가 주입한다.

## 상태 관리 (MVI-lite)

외부 MVI 프레임워크 없이 `core/mvi/BaseViewModel`(60여 줄) 하나로 패턴을 강제한다.

- **UiState**: 화면이 구독하는 단일 immutable 상태 (`StateFlow`)
- **UiSideEffect**: 네비게이션·토스트 같은 일회성 이벤트 (`Channel` → `Flow`)
- ViewModel은 UseCase를 생성자 주입받고, 결과를 `handleResult(DataResult, onSuccess, onFailure)`로
  성공/실패 분기 → `setState`(상태 갱신) 또는 `sendEffect`(화면 전환/토스트) 호출.

화면 컴포저블은 **3분할 규칙**을 따른다:
1. `XxxViewModel.kt` — State + SideEffect + VM
2. `XxxScreen.kt` — 무상태 `Route`(상태 구독 + 콜백 배선) 위임 + 순수 `Screen` + `private @Preview`
3. 바텀시트·다이얼로그·전체화면 오버레이는 **새 네비 목적지가 아니라** `XxxOverlays.kt`의
   `sealed interface`를 **호출 화면 로컬 상태**로 렌더한다.

36개 화면 ViewModel 전부가 이 베이스를 상속한다.

## 서버 통합 (Ktor)

```
ViewModel → UseCase → Repository(interface)
                          └▶ Remote*Repository(data/remote)
                                └▶ ApiClient ── Ktor HttpClient ── 서버
```

- **공통 응답 봉투** — 서버의 `{ success, data, error }`를 `ApiEnvelope<T>`로 역직렬화하고
  `ApiClient`의 reified inline 래퍼가 `DataResult`로 언랩한다. `expectSuccess=false`라 4xx/5xx도
  예외가 아닌 **에러 봉투**로 일관 처리한다.
- **인증** — Ktor `Auth` 플러그인이 매 요청에 JWT Bearer를 주입하고, 401 시 refresh 토큰으로
  **회전(rotation)**한다. 리프레시 실패 시 전역 `SessionEvents`로 재로그인을 유도한다.
  토큰은 `TokenStore`가 Android=`SharedPreferences` / iOS=`NSUserDefaults`에 영속한다.
- **반응형 캐시 무효화(`RemoteBus`)** — REST에 push가 없는 한계를, 도메인 단위 무효화 버스
  (`DataTopic` 7종: CLUB·MEMBER·NOTIFICATION·BOARD·RESOURCE·SCHEDULE·SETTINGS)로 우회한다.
  변경된 topic만 무효화하고 화면 재진입 시 의존 topic만 재조회한다.
- **presigned 업로드 분리** — 이미지/자료 업로드는 서버 presigned URL에 **인증 헤더 없는
  별도 클라이언트(`RawHttp`)**로 직접 PUT 해, 앱 JWT가 스토리지 서명과 충돌하지 않게 한다.
- **HTTP 엔진만 플랫폼 분기** — 공통 로직은 `commonMain`, 엔진만 Android=OkHttp / iOS=Darwin.

REST 경로는 `ApiRoutes` 한 파일이 단일 출처(10개 도메인 · 약 68개 라우트)다.

## 네비게이션 (수동 백스택)

Navigation-Compose 대신 `SnapshotStateList` 기반 수동 백스택을 직접 구현한다.

```
App → RootNavHost ─┬─ Loading (콜드스타트 세션 판정)
                   ├─ AuthNavHost  (미로그인/미가입 플로우)
                   └─ MainNavHost  (MainDestination 33개, 로그인·가입 완료 후)
```

- 목적지는 `sealed interface`로 타입 안전하게 표현한다.
- 콜드스타트와 로그인 직후를 하나의 `resolveFlow()`로 판정한다(라우팅 판정은 공유 flow가 아닌
  일회성 조회로 처리해 재구독 부작용을 피한다).
- 시스템 뒤로가기는 `PlatformBackHandler`(expect/actual)로 통합한다.

## 플랫폼 브릿지 (expect/actual)

`platform/`의 expect 선언 8종을 각 플랫폼 actual로 구현한다:
카메라 · 문서 피커 · 시스템 공유 · 캘린더 추가 · 인앱결제 · 이메일 작성기 · 시스템 뒤로가기 · 이미지 압축.

- Android는 Intent/ActivityResult, iOS는 UIKit/EventKit/StoreKit 델리게이트로 각각 관용 구현.
- 카카오 SDK·StoreKit2처럼 `shared`에 넣기 어려운 네이티브 의존성은 **레지스트리 주입 패턴**으로
  분리한다 — Swift가 Kotlin 인터페이스를 구현해 앱 시작 시 등록하고, `commonMain`은 SDK 의존성 0.

## 디자인 시스템

- 폰트: **Pretendard**. `composeResources/font/`에서 `Res.font.*`로 로드.
- 토큰: `~/StudioProjects/damoim-design/docs/design-tokens.md` 기준.
  화면 코드에서 `DamoimTheme.colors / typography / shapes` 토큰만 사용(하드코딩 금지).
- 브랜드 컬러 `#2F6DD3`, 390×844 기준.

## 환경설정 · 시크릿

- 서버 base URL은 `local.properties`(`server.base.url`) 단일 출처에서
  Android=`BuildConfig`, iOS=Gradle 코드생성(`IosBuildConfig`)으로 주입.
- 카카오 앱 키는 `local.properties → Gradle 태스크 → xcconfig → Info.plist` 파이프라인으로 흘려
  시크릿을 커밋에서 배제.

## 현재 상태 · 로드맵 (README와 동기)

- **DI** — `object AppGraph` 수동 Service Locator. → Koin 검토
- **네비게이션** — 수동 백스택. → `rememberSaveable` 복원 / Navigation-Compose 검토
- **토큰 저장** — 평문 영속. → Keychain / EncryptedSharedPreferences
- **iOS 함정** — Kotlin/Native가 Obj-C 상속 `object`의 코드생성을 미지원 → **링크 단계**에서만
  드러난다. 검증 시 컴파일이 아닌 링크까지 돌리고, 해당 지점은 `class + lazy 싱글턴`으로 회피한다.

## 빌드/실행

- Android: `:androidApp` 실행 (MainActivity → App())
- iOS: `iosApp` Xcode 프로젝트 (MainViewController → App()) · SPM `kakao-ios-sdk` 2.28.0
- 자세한 명령은 [README](./README.md)의 "빌드 & 실행" 참고.
