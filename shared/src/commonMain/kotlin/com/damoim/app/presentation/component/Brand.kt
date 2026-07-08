package com.damoim.app.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 다모임 로고 마크 — 두 사람(원 두 개)과 웃는 입(아크). 디자인 01/스플래시의 심볼.
 *
 * @param onBrand 브랜드 배경 위에 올릴 때(true)는 흰 카드+파란 심볼, 아니면 파란 카드.
 */
@Composable
fun DamoimLogoMark(
    modifier: Modifier = Modifier.size(88.dp),
    onBrand: Boolean = true,
) {
    val colors = DamoimTheme.colors
    val cardColor = if (onBrand) colors.surface else colors.primary
    val faceA = colors.primary
    val faceB = colors.accentSky
    val mouth = colors.primaryDeep
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(cardColor),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(if (onBrand) 48.dp else 44.dp)) {
            val w = size.width
            val h = size.height
            drawCircle(color = faceA, radius = w * 0.17f, center = Offset(w * 0.35f, h * 0.42f))
            drawCircle(color = faceB, radius = w * 0.17f, center = Offset(w * 0.65f, h * 0.42f))
            val smile = Path().apply {
                moveTo(w * 0.21f, h * 0.79f)
                cubicTo(w * 0.29f, h * 0.66f, w * 0.71f, h * 0.66f, w * 0.79f, h * 0.79f)
            }
            drawPath(
                path = smile,
                color = mouth,
                style = Stroke(width = w * 0.085f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}

/**
 * 작은 로고 배지 — primary 배경 + 흰 심볼. 상단 헤더의 "다모임" 옆 아이콘 등에 사용(디자인 32).
 */
@Composable
fun DamoimLogoBadge(
    modifier: Modifier = Modifier.size(30.dp),
    cornerRadius: androidx.compose.ui.unit.Dp = 10.dp,
) {
    val colors = DamoimTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(colors.primary),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize(0.56f)) {
            val w = size.width
            val h = size.height
            drawCircle(Color.White, radius = w * 0.17f, center = Offset(w * 0.35f, h * 0.42f))
            drawCircle(Color.White.copy(alpha = 0.6f), radius = w * 0.17f, center = Offset(w * 0.65f, h * 0.42f))
            val smile = Path().apply {
                moveTo(w * 0.21f, h * 0.79f)
                cubicTo(w * 0.29f, h * 0.66f, w * 0.71f, h * 0.66f, w * 0.79f, h * 0.79f)
            }
            drawPath(
                path = smile,
                color = Color.White,
                style = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}

/**
 * 이니셜 아바타(원형). 프로필 설정/리스트에서 사용.
 */
@Composable
fun InitialAvatar(
    initial: String,
    modifier: Modifier = Modifier.size(96.dp),
) {
    val colors = DamoimTheme.colors
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(colors.primaryContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial.take(2),
            style = DamoimTheme.typography.headline,
            color = colors.primaryDeep,
        )
    }
}
