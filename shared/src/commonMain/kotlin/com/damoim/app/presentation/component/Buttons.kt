package com.damoim.app.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 풀폭 기본 CTA 버튼. (디자인: primary 배경, radius 14, 15/700 흰 텍스트)
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = DamoimTheme.colors
    val clickable = enabled && !loading
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (clickable) colors.primary else colors.outline)
            .clickable(
                enabled = clickable,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = colors.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text = text, style = DamoimTheme.typography.button, color = colors.onPrimary)
        }
    }
}

/**
 * 카카오 로그인 버튼. (노란 배경 #FEE500, 말풍선 아이콘 + 검은 텍스트)
 */
@Composable
fun KakaoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    val colors = DamoimTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.kakao)
            .clickable(
                enabled = !loading,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = colors.onKakao,
                strokeWidth = 2.dp,
            )
        } else {
            KakaoBubbleIcon(tint = colors.onKakao, modifier = Modifier.size(20.dp))
            Text(
                text = text,
                style = DamoimTheme.typography.button,
                color = colors.onKakao,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

/**
 * 보조 텍스트 버튼 (취소·건너뛰기 등). 배경 없음, 뮤트 텍스트.
 */
@Composable
fun SecondaryTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = DamoimTheme.typography.bodyStrong,
            color = DamoimTheme.colors.textMuted,
        )
    }
}
