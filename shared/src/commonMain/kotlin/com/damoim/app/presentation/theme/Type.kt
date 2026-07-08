package com.damoim.app.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import damoim.shared.generated.resources.Res
import damoim.shared.generated.resources.pretendard_bold
import damoim.shared.generated.resources.pretendard_regular
import damoim.shared.generated.resources.pretendard_semibold

/**
 * Pretendard 폰트 패밀리.
 *
 * UMC-Product에서 가져온 TTF는 3종(regular/semibold/bold)이라 400/600/700만 실제
 * 파일이 있고, 디자인에서 쓰는 800·900은 Bold(700)로 근사 매핑한다. 무게가 더
 * 필요해지면 Pretendard ExtraBold/Black TTF를 composeResources/font에 추가하고
 * 아래 매핑만 늘리면 된다.
 */
@Composable
fun rememberPretendardFamily(): FontFamily = FontFamily(
    Font(Res.font.pretendard_regular, FontWeight.Normal),
    Font(Res.font.pretendard_semibold, FontWeight.SemiBold),
    Font(Res.font.pretendard_bold, FontWeight.Bold),
    // 800·900 → Bold TTF로 근사 (전용 웨이트 추가 전까지)
    Font(Res.font.pretendard_bold, FontWeight.ExtraBold),
    Font(Res.font.pretendard_bold, FontWeight.Black),
)

/**
 * 다모임 타입 스케일. 디자인 토큰(docs/design-tokens.md)의 역할별 스타일을 그대로 옮긴 것.
 */
@Immutable
data class DamoimTypography(
    val display: TextStyle,       // 34~36 / 900 — 스플래시 로고
    val headline: TextStyle,      // 24 / 800 — 페이지 타이틀
    val titleLarge: TextStyle,    // 20 / 800 — 섹션 헤더
    val titleMedium: TextStyle,   // 16~18 / 700 — 리스트/시트 제목
    val button: TextStyle,        // 15 / 700 — CTA 버튼
    val body: TextStyle,          // 14 / 400~600 — 본문
    val bodyStrong: TextStyle,    // 14 / 700 — 본문 강조
    val bodySmall: TextStyle,     // 13 / 600 — 보조 본문
    val caption: TextStyle,       // 12 / 600 — 메타/설명
    val label: TextStyle,         // 11 / 600 — 뱃지/타임스탬프
    val labelSmall: TextStyle,    // 10 / 700 — 탭바 라벨
)

@Composable
fun rememberDamoimTypography(): DamoimTypography {
    val f = rememberPretendardFamily()
    return DamoimTypography(
        display = TextStyle(fontFamily = f, fontWeight = FontWeight.Black, fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = (-0.7).sp),
        headline = TextStyle(fontFamily = f, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, lineHeight = 33.sp, letterSpacing = (-0.3).sp),
        titleLarge = TextStyle(fontFamily = f, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 27.sp, letterSpacing = (-0.2).sp),
        titleMedium = TextStyle(fontFamily = f, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 23.sp, letterSpacing = (-0.2).sp),
        button = TextStyle(fontFamily = f, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
        body = TextStyle(fontFamily = f, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
        bodyStrong = TextStyle(fontFamily = f, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 21.sp),
        bodySmall = TextStyle(fontFamily = f, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 19.sp),
        caption = TextStyle(fontFamily = f, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 18.sp),
        label = TextStyle(fontFamily = f, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 15.sp),
        labelSmall = TextStyle(fontFamily = f, fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 13.sp),
    )
}

val LocalDamoimTypography = staticCompositionLocalOf<DamoimTypography> {
    error("DamoimTypography가 제공되지 않았습니다. DamoimTheme 안에서 사용하세요.")
}
