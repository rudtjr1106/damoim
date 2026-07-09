package com.damoim.app.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 뒤로가기 버튼만 있는 최소 상단 바. (디자인: 좌측 셰브론)
 */
@Composable
fun BackTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        // 상단 바는 (화면의 safeDrawingPadding이 준) 상태바 인셋으로부터 16dp 아래에 위치
        modifier = modifier.fillMaxWidth().padding(start = 8.dp, top = 16.dp, bottom = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BackIconButton(onBack)
    }
}

/**
 * 뒤로 + 제목 (+ 선택적 우측 액션 텍스트) 상단 바. B 그룹 대부분의 헤더.
 * 디자인: padding 52px 16px 12px, 제목 17/700.
 */
@Composable
fun TitleTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    actionColor: Color = DamoimTheme.colors.primary,
    onAction: () -> Unit = {},
) {
    Row(
        // 상태바 인셋(화면 safeDrawingPadding 제공)으로부터 16dp 아래
        modifier = modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackIconButton(onBack)
        Spacer(Modifier.width(0.dp))
        Text(title, style = DamoimTheme.typography.titleMedium, color = DamoimTheme.colors.textPrimary, modifier = Modifier.weight(1f))
        if (actionText != null) {
            Text(
                actionText,
                style = DamoimTheme.typography.bodyStrong,
                color = actionColor,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onAction,
                ),
            )
        }
    }
}

@Composable
private fun BackIconButton(onBack: () -> Unit) {
    Box(
        modifier = Modifier.size(40.dp).clip(CircleShape).clickable(
            interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onBack,
        ),
        contentAlignment = Alignment.Center,
    ) {
        BackChevronIcon(tint = DamoimTheme.colors.textPrimary, modifier = Modifier.size(24.dp))
    }
}
