# 다모임 (Damoim)

> 동아리 종합 커뮤니티 플랫폼 — 동아리 운영에 필요한 모든 것을 한곳에서.

**다모임**은 카카오톡 오픈채팅·네이버 카페 등으로 파편화된 동아리 운영을,
가입 코드 · 기수 관리 · 게시판 · 일정/이벤트 · 구독까지 표준화된 하나의 전용 앱으로 통합하는
**Compose Multiplatform (Android · iOS)** 모바일 앱입니다.

<p>
  <img alt="platform" src="https://img.shields.io/badge/platform-Android%20%7C%20iOS-3F51B5">
  <img alt="framework" src="https://img.shields.io/badge/Compose%20Multiplatform-1.11-4285F4">
  <img alt="kotlin" src="https://img.shields.io/badge/Kotlin-2.4.0-7F52FF">
</p>

---

## 📌 프로젝트 소개

| | |
|---|---|
| **한 줄 정의** | 동아리 종합 커뮤니티 플랫폼 |
| **타겟 사용자** | 학교·직장·취미 동아리의 **동아리장**과 **회원** (IT 활용에 익숙한 20~30대) |
| **디바이스** | 모바일 앱 (Android · iOS) |
| **카테고리** | 생산성 / 업무 |

**해결하는 문제** — 동아리 운영자는 파편화된 소통 채널로 정보 공유·회원 관리에 어려움을 겪습니다.
다모임은 동아리에 특화된 기능(기수별 회원 관리, 가입 코드, 동아리장 전용 관리)을 통합 제공해
운영 효율을 극대화하고, 회원이 단일 공간에서 편리하게 소통·공유하도록 지원합니다.

**사용자 시나리오** — 신입 회원이 **가입 코드**로 접속해 **카카오 로그인**으로 합류 → 동아리장이 승인하고 **기수**를 배정 →
회원은 **자유·모집·공지 게시판**과 **자료실**에서 소통·공유하고, **캘린더/이벤트**로 MT·스터디 일정을 확인·신청합니다.

---

## ✨ 주요 기능 (PRD)

| # | 기능 | 중요도 | 핵심 내용 |
|---|---|:---:|---|
| 1 | **카카오 로그인 및 동아리 가입** | 🔴 높음 | 카카오 간편 로그인, 가입 코드로 가입 신청, 동아리장 승인/거절 |
| 2 | **동아리 생성 및 관리** | 🔴 높음 | 동아리 생성·정보 설정, 가입 코드 발급/변경/비활성화, 권한 관리 |
| 3 | **게시판** | 🔴 높음 | 자유·모집·공지 게시판, 글/댓글/첨부, 검색·필터 |
| 4 | **회원 및 기수 관리** | 🔴 높음 | 회원 목록·검색, 기수 배정/변경, 강퇴·탈퇴 처리 |
| 5 | **일정 및 이벤트 관리** | 🟡 중간 | 캘린더, 일정 등록, 이벤트 참여 신청·신청 내역 |
| 6 | **동아리 구독 플랜** | 🔴 높음 | 30명 미만 무료, 이상 유료 구독·결제, 플랜 변경 |

> 전체 화면 설계(86개, 그룹 A~H)는 Claude Design으로 제작했으며, 브랜드 컬러 `#2F6DD3` · Pretendard · 390×844 기준입니다.

---

## 🛠 기술 스택

- **Compose Multiplatform 1.11** — Android · iOS 화면 코드 공유 (`commonMain`)
- **Kotlin 2.4.0**, Material 3
- **아키텍처**: 클린 아키텍처(레이어 패키지) + **MVI-lite** (`StateFlow` 상태 / `Channel` 사이드이펙트)
- **폰트**: Pretendard, **카카오 로그인 버튼**: 공식 벡터 에셋 · **카카오 SDK** v2 (네이티브 앱 키)
- **사진 피커**: [Peekaboo](https://github.com/onseok/peekaboo) (Android Photo Picker / iOS PHPicker)
- **플랫폼 브릿지**(expect/actual): 카메라·문서 피커, 시스템 공유·이메일 작성기, 인앱결제, 시스템 뒤로가기
- **날짜/시간**: kotlinx-datetime (실제 달력·D-day 계산)
- 서버 연동 전까지 앱 전역 인메모리 **`MockStore`**(단일 소스 `StateFlow`)로 실동작 — 서버 도입 시 Mock 리포지토리만 Ktor로 교체

---

## 🏗 아키텍처

`:shared` 모듈 안에서 레이어를 **패키지**로 분리합니다. 상세는 [ARCHITECTURE.md](./ARCHITECTURE.md) 참고.

```
shared/src/commonMain/kotlin/com/damoim/app/
├─ core/          # DataResult(결과 래퍼) · BaseViewModel(MVI) · AppGraph(임시 DI) · SocialLogin
├─ domain/        # model · repository(interface) · usecase   (순수 Kotlin)
├─ data/          # mock(MockStore = 앱 전역 인메모리 단일 소스) · repository 구현
├─ platform/      # 카메라·문서 피커·공유·이메일·인앱결제(expect/actual)
└─ presentation/
   ├─ theme/      # DamoimTheme (Color · Type · Shape · DamoimStrings) — Pretendard
   ├─ component/  # 버튼 · 텍스트필드 · 아이콘(벡터) · 오버레이 · 날짜피커 등 공용 UI
   └─ <feature>/  # auth · home · board · resource · member · profile ·
                  #  schedule · settings … 화면별 서브패키지(예: board/{home,list,detail,write,search})
```

**의존성 방향**: `presentation → domain ← data`, 공통은 `core`.
화면은 **Route(상태·이벤트·네비)** + **Screen(무상태 UI)** + **@Preview** 3종으로 분리하고,
멀티스크린 feature는 **화면별 서브패키지**로 나눕니다.
UI 문자열은 `DamoimStrings`, 색·타이포는 `DamoimTheme` 토큰만 사용합니다(하드코딩 금지).
바텀시트·다이얼로그·전체화면 오버레이는 새 nav 목적지가 아니라 **호출 화면의 로컬 상태**로 렌더합니다.

---

## ✅ 구현 현황

디자인의 **A~G 전 화면 그룹을 구현 완료**했습니다. 서버가 아직 없어 앱 전역 인메모리 저장소
(`MockStore`)로 **UI 목업이 아닌 실제 동작**까지 구현했으며(로그인→가입→게시글 CRUD→일정 신청→구독 결제),
서버가 붙으면 **Mock 리포지토리 구현만 Ktor로 교체**하면 되는 상태입니다.

| 그룹 | 상태 | 핵심 화면·기능 |
|---|:---:|---|
| **A. 인증 및 가입** | ✅ | 카카오 로그인 → 프로필 설정 → 가입 코드 입력 → 신청 완료/거절 |
| **B. 홈 · 동아리 관리** | ✅ | 홈(장/원 분기), 동아리 생성, 코드 발급/공유, 가입 승인/거절, 알림 |
| **C. 게시판** | ✅ | 자유·공지·모집 게시판, 글/댓글/답글, 첨부·투표·모집 신청, 검색 |
| **D. 자료실** | ✅ | 자료실 홈(저장공간), 상세, 업로드(문서 피커·기수 공개범위) |
| **E. 회원·기수 관리** | ✅ | 회원 목록/상세, 기수 관리, 내 프로필, 멀티 동아리 전환 |
| **F. 일정·이벤트** | ✅ | 캘린더/목록, 일정·이벤트 등록(동적 신청폼), 참여 신청, 내 신청 내역 |
| **G. 구독·설정** | ✅ | 설정 홈, 구독 플랜·인앱결제, 구독 관리, 운영진 권한, 알림 설정, 문의하기, 차단 관리 |

**전면 기능화 하이라이트** (전부 에뮬레이터 실동작 검증)
- **카카오 로그인** E2E(공식 버튼 에셋, 네이티브 앱 키) · 프로필이 댓글 작성자·홈 인사말에 반영
- **게시판**: 좋아요·댓글/답글·투표·모집 신청·글 작성/수정, 링크·댓글·코드 **클립보드 실복사**, 실필터 검색
- **일정/이벤트**: 실제 달력(월 이동), 동적 신청폼 빌더(선택형/주관식/복수), **참여 신청 시 정원·아바타 실반영**(정원 차면 자동 마감)
- **구독**: 27 구독 플랜 → **네이티브 인앱결제**(Play Billing 자리) → 성공 시 구독 활성화가 설정·구독관리에 실시간 반영
- **운영진 권한**: 구현한 권한 기능 6종을 운영진별 토글로 위임 관리
- 사진/카메라(Peekaboo·플랫폼 브릿지), 시스템 공유·이메일 작성기, 시스템 뒤로가기 지원

---

## 🚀 빌드 & 실행

**요구 사항**
- **Android Studio Narwhal (2025.1) 이상**, JDK 17+
- 이 프로젝트는 IDE 호환을 위해 **AGP 8.12.2 / Gradle 8.14**를 사용합니다. (AGP 9.x로 올리지 마세요 — 현재 IDE에서 sync 불가)

**Android**
```bash
./gradlew :androidApp:assembleDebug     # 빌드
# 또는 Android Studio에서 :androidApp 실행
```

**iOS**
```bash
# shared 프레임워크 컴파일 확인
./gradlew :shared:compileKotlinIosSimulatorArm64
# 앱 실행은 iosApp/ 을 Xcode로 열어서 실행
```

**테스트**
```bash
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
```

---

## 🗺 로드맵

- [x] A~G 전 화면 그룹 (Mock 전면 기능화)
- [ ] 백엔드 연동: `AppGraph`(수동 DI) → Koin, Mock Repository → Ktor
- [ ] 네비게이션: 수동 백스택 → Navigation Compose
- [ ] iOS actual: 카메라·문서 피커·StoreKit, 실기기 카카오 로그인·Play Billing
- [ ] 프로필 사진 서버 업로드, 정책 화면(약관/개인정보) 웹뷰

---

<sub>화면 설계·기능 명세 기반. 브랜드 컬러 #2F6DD3 · Pretendard · Compose Multiplatform.</sub>
