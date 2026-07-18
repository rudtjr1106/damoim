package com.damoim.app.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus

/** 월화수목금토일 — ISO(월=1)를 한 글자 요일로. */
internal fun koreanWeekday(date: LocalDate): String = "월화수목금토일"[date.dayOfWeek.isoDayNumber - 1].toString()

/**
 * 실제 달력 그리드(일요일 시작, 과거 날짜 비활성). 셀 상태 3종: 오늘(테두리), 선택(채운 원), 그 외.
 * 86 마감 피커·61 일정 피커 공용.
 */
@Composable
internal fun CalendarGrid(year: Int, month: Int, selected: LocalDate, today: LocalDate, onSelect: (LocalDate) -> Unit) {
    val colors = DamoimTheme.colors
    val first = LocalDate(year, month, 1)
    val offset = first.dayOfWeek.isoDayNumber % 7               // 일요일 시작(일=0)
    val daysInMonth = first.plus(DatePeriod(months = 1)).plus(DatePeriod(days = -1)).dayOfMonth
    // 항상 6주(6행)를 렌더 — 월마다 주 수(5~6)가 달라져 시트 높이가 변하는 것을 막는다.
    val rows = 6
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        repeat(rows) { row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val dayNum = row * 7 + col - offset + 1
                    Box(Modifier.weight(1f).height(42.dp), contentAlignment = Alignment.Center) {
                        if (dayNum in 1..daysInMonth) {
                            val date = LocalDate(year, month, dayNum)
                            val isPast = date < today
                            val isSelected = date == selected
                            val isToday = date == today
                            val textColor = when {
                                isSelected -> colors.onPrimary
                                isPast -> colors.calOtherMonth
                                col == 0 -> colors.calSunday
                                col == 6 -> colors.calSaturday
                                else -> colors.textPrimary
                            }
                            Box(
                                Modifier.size(38.dp).clip(CircleShape)
                                    .then(if (isSelected) Modifier.background(colors.primary) else Modifier)
                                    .clickable(enabled = !isPast, interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(date) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("$dayNum", style = DamoimTheme.typography.body.copy(fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Normal, fontSize = 14.sp), color = textColor)
                                if (isToday && !isSelected) {
                                    Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp).size(4.dp).clip(CircleShape).background(colors.primary))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 시간 선택 — 드래그로 스크롤되는 스피너 휠(스냅). 오전/오후 · 시 · 분(10분 단위). */
@Composable
internal fun TimeWheel(
    isPm: Boolean, hour12: Int, minute: Int,
    onAmPm: (Boolean) -> Unit, onHour: (Int) -> Unit, onMinute: (Int) -> Unit,
) {
    val colors = DamoimTheme.colors
    val hours = remember { (1..12).map { "$it" } }
    val minutes = remember { (0..50 step 10).map { it.toString().padStart(2, '0') } }
    Box(Modifier.fillMaxWidth().height(132.dp).padding(horizontal = 24.dp)) {
        Box(Modifier.fillMaxWidth().height(44.dp).align(Alignment.Center).clip(RoundedCornerShape(12.dp)).background(colors.surfaceVariant))
        Row(Modifier.fillMaxWidth().height(132.dp)) {
            SpinnerColumn(items = listOf(DamoimStrings.PICKER_AM, DamoimStrings.PICKER_PM), selectedIndex = if (isPm) 1 else 0, onSelect = { onAmPm(it == 1) }, modifier = Modifier.weight(1f))
            SpinnerColumn(items = hours, selectedIndex = hour12 - 1, onSelect = { onHour(it + 1) }, modifier = Modifier.weight(1f))
            Box(Modifier.width(14.dp).height(132.dp), contentAlignment = Alignment.Center) {
                Text(":", style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = colors.textPrimary)
            }
            SpinnerColumn(items = minutes, selectedIndex = (minute / 10).coerceIn(0, minutes.lastIndex), onSelect = { onMinute(it * 10) }, modifier = Modifier.weight(1f))
        }
    }
}

/** 스피너 컬럼 — LazyColumn + 스냅 플링. 44dp × 3행 뷰포트, 상하 44dp 패딩으로 스냅 첫 항목이 중앙에 온다. */
@Composable
private fun SpinnerColumn(items: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = DamoimTheme.colors
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex.coerceIn(0, items.lastIndex))
    val fling = rememberSnapFlingBehavior(lazyListState = listState)
    val centeredIndex by remember {
        derivedStateOf {
            val offsetAdjust = if (listState.firstVisibleItemScrollOffset > 22 * 3) 1 else 0
            (listState.firstVisibleItemIndex + offsetAdjust).coerceIn(0, items.lastIndex)
        }
    }
    LaunchedEffect(listState, items) {
        snapshotFlow { listState.isScrollInProgress to centeredIndex }
            .collect { (scrolling, index) -> if (!scrolling) onSelect(index) }
    }
    LazyColumn(state = listState, flingBehavior = fling, modifier = modifier.height(132.dp), contentPadding = PaddingValues(vertical = 44.dp)) {
        items(items.size) { index ->
            val centered = index == centeredIndex
            Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.Center) {
                Text(
                    items[index],
                    style = DamoimTheme.typography.titleMedium.copy(fontWeight = if (centered) FontWeight.ExtraBold else FontWeight.Normal, fontSize = if (centered) 18.sp else 16.sp),
                    color = if (centered) colors.textPrimary else colors.outlineStrong,
                )
            }
        }
    }
}
