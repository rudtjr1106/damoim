package com.damoim.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.ChevronRightIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimTheme

/** 설정 계열 공용 탑바(뒤로가기 + 타이틀 + 우측 액션 슬롯). */
@Composable
fun SettingsTopBar(title: String, onBack: () -> Unit, trailing: (@Composable () -> Unit)? = null) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().background(colors.surface)) {
        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(start = 16.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(24.dp).noRippleClick(onBack), contentAlignment = Alignment.Center) { BackChevronIcon(colors.textPrimary, Modifier.size(24.dp)) }
            Spacer(Modifier.width(8.dp))
            Text(title, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
            trailing?.invoke()
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
    }
}

/** 설정 섹션(헤더 라벨 + 흰 카드 래퍼). */
@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    val colors = DamoimTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp), color = colors.textMuted, modifier = Modifier.padding(horizontal = 4.dp))
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(colors.surface).padding(horizontal = 18.dp)) { content() }
    }
}

/** 설정 리스트 행. 우측 슬롯이 없으면 chevron 표시. */
@Composable
fun SettingsRow(
    label: String,
    onClick: () -> Unit = {},
    labelColor: androidx.compose.ui.graphics.Color = DamoimTheme.colors.textPrimary,
    showDivider: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = DamoimTheme.colors
    Column {
        Row(Modifier.fillMaxWidth().noRippleClick(onClick).padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(label, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = labelColor, modifier = Modifier.weight(1f))
            if (trailing != null) trailing() else ChevronRightIcon(colors.outlineStrong, Modifier.size(16.dp))
        }
        if (showDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
    }
}

/** 공용 토글 스위치(44×26). 30·65 등에서 사용. */
@Composable
fun DamoimSwitch(on: Boolean, onToggle: () -> Unit) {
    val colors = DamoimTheme.colors
    Box(
        Modifier.width(44.dp).height(26.dp).clip(RoundedCornerShape(99.dp)).background(if (on) colors.primary else colors.outline)
            .noRippleClick(onToggle).padding(3.dp),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
    ) { Box(Modifier.size(20.dp).clip(CircleShape).background(colors.surface)) }
}
