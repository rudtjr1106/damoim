package com.damoim.app.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
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

/** 하단 탭 종류. 회원은 설정으로 옮기고(43) 그 자리에 자료실을 둔다. */
enum class MainTab { HOME, BOARD, SCHEDULE, ARCHIVE, SETTINGS }

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
    // background 뒤에 navigationBars 인셋을 적용 — 표면은 시스템 내비바 뒤까지 칠하되 탭은 그 위로 올린다.
    // 3버튼 내비는 인셋이 커서 간격이 생기고, 제스처 내비/iOS는 안전영역만큼만 확보돼 기존과 유사하다.
    Column(modifier = modifier.fillMaxWidth().background(colors.surface).windowInsetsPadding(WindowInsets.navigationBars)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
        Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)) {
            TabItem(MainTab.HOME, DamoimStrings.TAB_HOME, selected, onSelect) { t, m -> HomeIcon(t, m) }
            TabItem(MainTab.BOARD, DamoimStrings.TAB_BOARD, selected, onSelect) { t, m -> BoardIcon(t, m) }
            TabItem(MainTab.SCHEDULE, DamoimStrings.TAB_SCHEDULE, selected, onSelect) { t, m -> CalendarIcon(t, m) }
            TabItem(MainTab.ARCHIVE, DamoimStrings.TAB_ARCHIVE, selected, onSelect) { t, m -> FolderIcon(t, m) }
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
