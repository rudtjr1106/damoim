package com.damoim.app.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 하단 탭 종류. 홈만 실제 화면이 있고 나머지는 그룹 C~G(미구현). */
enum class MainTab { HOME, BOARD, SCHEDULE, MEMBERS, SETTINGS }

/**
 * 앱 하단 탭바 (디자인: 홈/게시판/일정/회원/설정, 활성=primary·비활성=textDisabled).
 * 상단 1px 구분선 + 홈 인디케이터 영역(하단 26dp) 포함.
 */
@Composable
fun BottomNavBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DamoimTheme.colors
    Column(modifier = modifier.fillMaxWidth().background(colors.surface)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
        Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp, start = 8.dp, end = 8.dp, bottom = 26.dp)) {
            TabItem(MainTab.HOME, DamoimStrings.TAB_HOME, selected, onSelect) { t, m -> HomeIcon(t, m) }
            TabItem(MainTab.BOARD, DamoimStrings.TAB_BOARD, selected, onSelect) { t, m -> BoardIcon(t, m) }
            TabItem(MainTab.SCHEDULE, DamoimStrings.TAB_SCHEDULE, selected, onSelect) { t, m -> CalendarIcon(t, m) }
            TabItem(MainTab.MEMBERS, DamoimStrings.TAB_MEMBERS, selected, onSelect) { t, m -> PeopleIcon(t, m) }
            TabItem(MainTab.SETTINGS, DamoimStrings.TAB_SETTINGS, selected, onSelect) { t, m -> SettingsIcon(t, m) }
        }
    }
}

@Composable
private fun RowScope.TabItem(
    tab: MainTab,
    label: String,
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    icon: @Composable (tint: Color, modifier: Modifier) -> Unit,
) {
    val colors = DamoimTheme.colors
    val active = tab == selected
    val tint = if (active) colors.primary else colors.textDisabled
    Column(
        modifier = Modifier.weight(1f).clickable(
            interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { onSelect(tab) },
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        icon(tint, Modifier.size(21.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = DamoimTheme.typography.labelSmall.copy(fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold),
            color = tint,
        )
    }
}
