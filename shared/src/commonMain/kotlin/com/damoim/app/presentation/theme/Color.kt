package com.damoim.app.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 다모임 색상 팔레트.
 *
 * 값은 디자인 원본(`~/StudioProjects/damoim-design/docs/design-tokens.md`)의
 * oklch → sRGB 변환 결과. 원본이 oklch 기반이라 HEX는 근사치이며, 추후 디자인
 * 확정 시 이 파일만 갱신하면 된다.
 */
@Immutable
data class DamoimColors(
    // Brand
    val primary: Color = Color(0xFF2F6DD3),
    val primaryDark: Color = Color(0xFF1F5DC2),
    val primaryDeep: Color = Color(0xFF2151A1),
    val primaryContainer: Color = Color(0xFFE9F3FF),
    val primaryContainerHigh: Color = Color(0xFFCDE0F9),
    val accentSky: Color = Color(0xFF68B7ED),
    val accentSkySoft: Color = Color(0xFFA4C0E4), // 연한 페리윙클 — 점선/보조 테두리 (oklch 0.8 0.06 255)
    val onDarkNavy: Color = Color(0xFF172846),

    // Neutral (텍스트/선/면)
    val textPrimary: Color = Color(0xFF181C26),
    val textSecondary: Color = Color(0xFF3C4354),
    val textTertiary: Color = Color(0xFF5C6474),
    val textMuted: Color = Color(0xFF8E96A8),
    val textDisabled: Color = Color(0xFFAEB6C6),
    val outlineStrong: Color = Color(0xFFC4CAD6),
    val outline: Color = Color(0xFFD5DBE6),
    val divider: Color = Color(0xFFE6EAF2),
    val dividerLight: Color = Color(0xFFECF0F6),
    val surfaceDim: Color = Color(0xFFF2F4F8),
    val surfaceVariant: Color = Color(0xFFF4F6FA),
    val surfaceInput: Color = Color(0xFFF7F8FB),
    val surface: Color = Color(0xFFFFFFFF),

    // Semantic
    val error: Color = Color(0xFFE5484D),
    val errorSoft: Color = Color(0xFFFF8A8E),
    val errorContainer: Color = Color(0xFFFFF2F2),
    val kakao: Color = Color(0xFFFEE500),
    val onKakao: Color = Color(0xFF191919),
    val toastSurface: Color = Color(0xFF22273A),
    val toastError: Color = Color(0xFF3A2226),

    // C 게시판 전용
    val skeletonBase: Color = Color(0xFFE9EDF4),      // 로딩 스켈레톤 셔머 베이스(72/73)
    val calSunday: Color = Color(0xFFE5766F),         // 달력 일요일/마감 강조(86)
    val calSaturday: Color = Color(0xFF7C93CF),       // 달력 토요일(86)
    val calOtherMonth: Color = Color(0xFFCBD2DE),     // 달력 인접월 날짜(86)
    val imageViewerBg: Color = Color(0xFF0C0E14),     // 이미지 전체화면 뷰어 배경(57)
    val imageViewerTile: Color = Color(0xFF1A1E2A),   // 뷰어 썸네일/플레이스홀더(57)

    // D 자료실 전용
    val surfaceTrack: Color = Color(0xFFEEF1F6),      // 저장공간 바 트랙(67) · 미리보기 스트라이프(68)
    val previewPillText: Color = Color(0xFF93A0BD),   // 68 '문서 미리보기' 알약 텍스트(본문 muted보다 푸른 회색)
    val fileSheet: Color = Color(0xFF1F9D55),         // XLSX 배지(성공색과 동일)
    val filePresentation: Color = Color(0xFFE8833A),  // PPTX 배지
    val fileArchive: Color = Color(0xFF6B7280),       // ZIP 배지

    // On-colors
    val onPrimary: Color = Color(0xFFFFFFFF),
    val onSurface: Color = Color(0xFF181C26),

    // Overlay (바텀시트/다이얼로그 딤)
    val scrim: Color = Color(0x730F1423), // rgba(15,20,35,.45)
)

val LocalDamoimColors = staticCompositionLocalOf { DamoimColors() }
