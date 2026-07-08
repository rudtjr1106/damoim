package com.damoim.app.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * 다모임 모양 토큰. 디자인에서 반복되는 radius 값(docs/design-tokens.md §3).
 */
@Immutable
data class DamoimShapes(
    val small: Shape = RoundedCornerShape(10.dp),
    val medium: Shape = RoundedCornerShape(14.dp),   // 버튼·카드·토스트 (최다)
    val large: Shape = RoundedCornerShape(16.dp),    // 인풋·정보 박스
    val extraLarge: Shape = RoundedCornerShape(18.dp),
    val card: Shape = RoundedCornerShape(24.dp),
    val sheet: Shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    val pill: Shape = RoundedCornerShape(999.dp),
)

val LocalDamoimShapes = staticCompositionLocalOf { DamoimShapes() }
