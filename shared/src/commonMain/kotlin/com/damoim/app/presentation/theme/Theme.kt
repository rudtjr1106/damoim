package com.damoim.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * 다모임 디자인 시스템 테마.
 *
 * Material3 위에 커스텀 토큰(색·타이포·모양)을 CompositionLocal로 얹는 구조.
 * 화면 코드에서는 [DamoimTheme.colors] / [DamoimTheme.typography] / [DamoimTheme.shapes]로 접근한다.
 */
@Composable
fun DamoimTheme(
    colors: DamoimColors = DamoimColors(),
    shapes: DamoimShapes = DamoimShapes(),
    content: @Composable () -> Unit,
) {
    val typography = rememberDamoimTypography()
    CompositionLocalProvider(
        LocalDamoimColors provides colors,
        LocalDamoimTypography provides typography,
        LocalDamoimShapes provides shapes,
    ) {
        // Material3 기본값(리플·셀렉션 등)도 브랜드색/Pretendard를 따르도록 최소 매핑
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = colors.primary,
                onPrimary = colors.onPrimary,
                primaryContainer = colors.primaryContainer,
                background = colors.surface,
                onBackground = colors.textPrimary,
                surface = colors.surface,
                onSurface = colors.textPrimary,
                error = colors.error,
                outline = colors.outline,
            ),
            typography = Typography(
                bodyLarge = typography.body,
                labelLarge = typography.button,
            ),
            content = content,
        )
    }
}

/** 화면 코드용 토큰 접근자: `DamoimTheme.colors.primary` 형태로 사용. */
object DamoimTheme {
    val colors: DamoimColors
        @Composable @ReadOnlyComposable get() = LocalDamoimColors.current
    val typography: DamoimTypography
        @Composable @ReadOnlyComposable get() = LocalDamoimTypography.current
    val shapes: DamoimShapes
        @Composable @ReadOnlyComposable get() = LocalDamoimShapes.current
}
