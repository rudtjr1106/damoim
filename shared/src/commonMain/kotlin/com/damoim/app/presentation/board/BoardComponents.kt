package com.damoim.app.presentation.board

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.presentation.component.MegaphoneIcon
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 카테고리 → 한국어 라벨. */
fun boardCategoryLabel(category: BoardCategory): String = when (category) {
    BoardCategory.NOTICE -> DamoimStrings.BOARD_NOTICE
    BoardCategory.FREE -> DamoimStrings.BOARD_FREE
    BoardCategory.RECRUIT -> DamoimStrings.BOARD_RECRUIT
}

/**
 * 카테고리 뱃지(작은 알약). 디자인: 11px/800, primaryContainer 배경 + primaryDark 텍스트.
 * padding 3~4 × 9~10.
 */
@Composable
fun CategoryBadge(
    category: BoardCategory,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 9.dp,
    verticalPadding: Dp = 3.dp,
) {
    val colors = DamoimTheme.colors
    Text(
        boardCategoryLabel(category),
        style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold),
        color = colors.primaryDark,
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.primaryContainer)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
    )
}

/** 채워진 강조 뱃지(모집중·필독 등). white 텍스트 + [bg] 배경. megaphone=필독. */
@Composable
fun SolidBadge(
    text: String,
    bg: Color,
    modifier: Modifier = Modifier,
    textColor: Color = DamoimTheme.colors.onPrimary,
    leadingMegaphone: Boolean = false,
    horizontalPadding: Dp = 10.dp,
    verticalPadding: Dp = 4.dp,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingMegaphone) {
            MegaphoneIcon(tint = textColor, modifier = Modifier.size(11.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold), color = textColor)
    }
}

/** 이니셜 원형 아바타. 디자인: primaryContainerHigh 배경 + primaryDeep 텍스트. */
@Composable
fun InitialAvatar(
    initials: String,
    size: Dp,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
) {
    val colors = DamoimTheme.colors
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(colors.primaryContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials,
            style = DamoimTheme.typography.caption.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (fontSize != androidx.compose.ui.unit.TextUnit.Unspecified) fontSize else DamoimTheme.typography.caption.fontSize,
            ),
            color = colors.primaryDeep,
        )
    }
}

/**
 * 사진 첨부 플레이스홀더 박스. (디자인의 45° 사선 스트라이프 근사 — 실제 이미지 로더 도입 전까지 surfaceInput 면 + 라벨)
 */
@Composable
fun PhotoPlaceholder(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    label: String = "photo",
) {
    val colors = DamoimTheme.colors
    Box(
        modifier = modifier.clip(RoundedCornerShape(cornerRadius)).background(colors.surfaceInput),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = DamoimTheme.typography.label, color = colors.textDisabled)
    }
}

/** 셔머 펄스 스켈레톤 블록(72/73). 크기는 [modifier]로 지정. */
@Composable
internal fun SkeletonBlock(modifier: Modifier = Modifier, height: Dp = 12.dp, radius: Dp = 8.dp) {
    val colors = DamoimTheme.colors
    val transition = rememberInfiniteTransition(label = "sk")
    val a by transition.animateFloat(1f, 0.4f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "a")
    Box(modifier.height(height).clip(RoundedCornerShape(radius)).alpha(a).background(colors.skeletonBase))
}
