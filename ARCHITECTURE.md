# 다모임 아키텍처

Compose Multiplatform(Android + iOS) 앱. 클린 아키텍처를 `:shared` 모듈 안에서
**패키지 레이어링**으로 구현한다. (별도 Gradle 모듈 분리는 화면이 늘어나면 검토)

현재 구현 범위: **A 인증 및 가입** 7개 화면. 서버가 없어 데이터는 전부 Mock.

## 레이어 구조

```
shared/src/commonMain/kotlin/com/damoim/app/
├─ App.kt                     # 진입 컴포저블 → DamoimTheme { AuthNavHost() }
│
├─ core/                      # 레이어 무관 공통
│  ├─ result/DataResult.kt    # 성공/실패 래퍼 (UMC의 ApiState 대응)
│  ├─ mvi/BaseViewModel.kt    # MVI-lite 베이스 (uiState/sideEffect/handleResult)
│  └─ di/AppGraph.kt          # ★임시 수동 DI (서버·Koin 도입 전까지)
│
├─ domain/                    # 순수 비즈니스 (안드로이드/컴포즈 의존 없음)
│  ├─ model/                  # AuthUser, Club, JoinRequest, KakaoConsent
│  ├─ repository/             # AuthRepository, ClubRepository (인터페이스)
│  └─ usecase/                # LoginWithKakao / UpdateProfile / SubmitJoinCode
│
├─ data/                      # domain.repository 구현
│  ├─ mock/MockData.kt        # ★임시 목 데이터 (코드별 응답 분기)
│  └─ repository/             # MockAuthRepository, MockClubRepository
│
└─ presentation/
   ├─ theme/                  # DamoimTheme (Color/Type/Shape) — Pretendard
   ├─ component/              # 버튼·아이콘·아바타·토스트 등 공통 UI
   └─ auth/                   # A 그룹 화면 + ViewModel + 네비게이션
      ├─ AuthDestination.kt   # 화면 목적지 (sealed)
      ├─ AuthNavHost.kt       # 백스택 관리 + 화면 전환
      ├─ login/               # 01 로그인/온보딩
      ├─ kakao/               # 02 카카오 동의
      ├─ profile/             # 31 프로필 설정
      ├─ start/               # 32 시작하기
      ├─ joincode/            # 03 가입 코드 입력
      └─ result/              # 04 신청 완료 / 38 거절됨
```

### 의존성 방향

```
presentation ──▶ domain ◀── data
        └────────▶ core ◀────────┘
```

- domain은 아무것도 모른다(순수 Kotlin + 코루틴).
- data는 domain의 Repository 인터페이스를 구현한다.
- presentation은 domain(UseCase/Model)에만 의존하고, 구현체는 `AppGraph`가 주입한다.

## 상태 관리 (MVI-lite)

UMC-Product의 `BaseViewModel` 패턴을 CMP로 옮긴 것.

- **UiState**: 화면이 구독하는 단일 immutable 상태 (`StateFlow`)
- **UiSideEffect**: 네비게이션·토스트 같은 일회성 이벤트 (`Channel` → `Flow`)
- ViewModel은 UseCase를 생성자 주입받고, 결과를 `handleResult`로 성공/실패 분기 →
  `setState`(상태 갱신) 또는 `sendEffect`(화면 전환) 호출.

화면 컴포저블은:
1. `viewModel { VM(AppGraph.someUseCase) }` 로 VM 생성
2. `uiState.collectAsState()` 로 상태 구독
3. `LaunchedEffect`에서 `sideEffect` 수집 → 네비게이션 콜백 호출

## 화면 플로우 (A 그룹)

```
01 로그인 ──카카오──▶ 02 동의 ──┬─(신규)─▶ 31 프로필 설정 ─▶ 32 시작하기
                                └─(기존)──────────────────▶ 32 시작하기
32 시작하기 ─┬─ 가입 코드 ─▶ 03 코드 입력 ─┬─ PENDING ─▶ 04 신청 완료
             └─ 동아리 생성(B 그룹, 토스트) └─ REJECTED ─▶ 38 거절됨 ─▶(재시도)─▶ 03
```

### Mock 코드 (화면 03에서 테스트)

| 코드 | 결과 | 도착 화면 |
|---|---|---|
| `DAMOIM` (그 외 6자리) | 신청 접수(PENDING) | 04 신청 완료 |
| `REJECT` | 거절(REJECTED) | 38 거절됨 |
| `EXPIRE` | 무효 코드 실패 | 03에 인라인 에러 |

## 디자인 시스템

- 폰트: **Pretendard** (UMC-Product에서 가져온 regular/semibold/bold TTF).
  `composeResources/font/`에 두어 `Res.font.*`로 로드. 800·900 웨이트는 Bold로 근사.
- 토큰: `~/StudioProjects/damoim-design/docs/design-tokens.md` 기준.
  화면 코드에서 `DamoimTheme.colors / typography / shapes`로 접근.

## ⚠️ 임시(서버 붙으면 교체) 지점

- `core/di/AppGraph.kt` — 수동 Service Locator. → Koin 모듈로 대체
- `data/mock/` + `data/repository/Mock*` — 목 데이터. → Ktor 기반 DataSource/Repository로 교체
- `AuthNavHost` 백스택 — 수동 관리. → Navigation-Compose(멀티플랫폼)로 교체 검토
- 화면 03의 "테스트 코드 안내" 문구 — 실제 연동 시 제거
- 시스템 뒤로가기(안드로이드) 미처리 — BackHandler 추가 예정

## 빌드/실행

- Android: `:androidApp` 실행 (MainActivity → App())
- iOS: `iosApp` Xcode 프로젝트 (MainViewController → App())
