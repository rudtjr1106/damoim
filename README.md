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

> 전체 화면 설계(83개, 8개 그룹 A~H)는 Claude Design으로 제작했으며, 브랜드 컬러 `#2F6DD3` · Pretendard · 390×844 기준입니다.

---

## 🛠 기술 스택

- **Compose Multiplatform 1.11** — Android · iOS 화면 코드 공유 (`commonMain`)
- **Kotlin 2.4.0**, Material 3
- **아키텍처**: 클린 아키텍처(레이어 패키지) + **MVI-lite** (`StateFlow` 상태 / `Channel` 사이드이펙트)
- **폰트**: Pretendard, **카카오 로그인 버튼**: 공식 벡터 에셋
- **사진 피커**: [Peekaboo](https://github.com/onseok/peekaboo) (Android Photo Picker / iOS PHPicker)
- 서버 연동 전까지 **Mock 데이터**로 동작 (임시 수동 DI)

---

## 🏗 아키텍처

`:shared` 모듈 안에서 레이어를 **패키지**로 분리합니다. 상세는 [ARCHITECTURE.md](./ARCHITECTURE.md) 참고.

```
shared/src/commonMain/kotlin/com/damoim/app/
├─ core/          # DataResult(결과 래퍼) · BaseViewModel(MVI) · AppGraph(임시 DI)
├─ domain/        # model · repository(interface) · usecase   (순수 Kotlin)
├─ data/          # mock · repository 구현 (현재 Mock)
└─ presentation/
   ├─ theme/      # DamoimTheme (Color · Type · Shape · DamoimStrings) — Pretendard
   ├─ component/  # 버튼 · 텍스트필드 · 아이콘(벡터) · 토스트 등 공용 UI
   └─ auth/       # 화면(Route/Screen/Preview) + ViewModel + 네비게이션
```

**의존성 방향**: `presentation → domain ← data`, 공통은 `core`.
화면은 **Route(상태·이벤트·네비)** + **Screen(무상태 UI)** + **@Preview** 3종으로 분리하고,
UI 문자열은 `DamoimStrings`, 색·타이포는 `DamoimTheme` 토큰만 사용합니다(하드코딩 금지).

---

## ✅ 구현 현황

서버가 아직 없어 **A. 인증·가입 플로우**를 Mock 데이터로 우선 구현했습니다.

| 그룹 | 상태 | 화면 |
|---|:---:|---|
| **A. 인증 및 가입** | ✅ 구현 | 로그인 → 프로필 설정(사진·이름·연락처) → 시작하기 → 가입 코드 입력 → 신청 완료 / 거절 |
| B. 홈 · 동아리 관리 | ⬜ 예정 | 홈, 동아리 생성, 코드 발급, 신청 관리, 알림 |
| C. 게시판 | ⬜ 예정 | 게시판, 글/댓글, 첨부, 검색, 투표 |
| D. 자료실 | ⬜ 예정 | 자료실 홈, 상세, 업로드 |
| E. 회원·기수 관리 | ⬜ 예정 | 회원 목록/상세, 기수 관리, 내 프로필 |
| F. 일정·이벤트 | ⬜ 예정 | 캘린더, 일정 등록, 이벤트 신청 |
| G. 구독·설정 | ⬜ 예정 | 구독 플랜, 결제, 권한, 알림 설정 |

**A 그룹 하이라이트**
- 카카오 로그인(공식 버튼 에셋), 로그인 후 신규/기존 분기
- 프로필 설정: **프로필 사진 선택(Android/iOS)** · 이름(실명 권장, 10자 카운터) · 연락처
- 6자리 가입 코드 세그먼트 입력, 신청 결과(완료/거절) 분기
- 데모용 Mock 코드 — `DAMOIM`: 신청 완료 · `REJECT`: 거절 · `EXPIRE`: 오류

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

- [x] A. 인증·가입 (Mock)
- [ ] B~G 나머지 화면 그룹
- [ ] 백엔드 연동: `AppGraph`(수동 DI) → Koin, Mock Repository → Ktor
- [ ] 네비게이션: 수동 백스택 → Navigation Compose
- [ ] 프로필 사진 서버 업로드, 정책 화면(약관/개인정보) 웹뷰

---

<sub>화면 설계·기능 명세 기반. 브랜드 컬러 #2F6DD3 · Pretendard · Compose Multiplatform.</sub>
