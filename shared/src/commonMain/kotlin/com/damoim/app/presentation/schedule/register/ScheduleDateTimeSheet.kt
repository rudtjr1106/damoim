package com.damoim.app.presentation.schedule.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.CalendarGrid
import com.damoim.app.presentation.component.ChevronRightIcon
import com.damoim.app.presentation.component.DamoimBottomSheet
import com.damoim.app.presentation.component.TimeWheel
import com.damoim.app.presentation.component.koreanWeekday
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.schedule.schMonthTitle
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

/** 61 날짜·시간 피커. 시작/종료/마감 컨텍스트 제목을 받고, 확정 시 (날짜, 시, 분)을 돌려준다. */
@Composable
internal fun ScheduleDateTimeSheet(
    title: String,
    initialDate: LocalDate?,
    initialHour24: Int,
    initialMinute: Int,
    onConfirm: (LocalDate, Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = DamoimTheme.colors
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    // 현재 시각 — 오늘 날짜에 과거 시간을 고르는 것을 막기 위해 확정 시 비교한다.
    val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
    val start = initialDate ?: today.plus(DatePeriod(days = 1))
    var year by remember { mutableStateOf(start.year) }
    var month by remember { mutableStateOf(start.monthNumber) }
    var selected by remember { mutableStateOf(start) }
    var isPm by remember { mutableStateOf(initialHour24 >= 12) }
    var hour12 by remember { mutableStateOf((initialHour24 % 12).let { if (it == 0) 12 else it }) }
    var minute by remember { mutableStateOf(initialMinute) }
    var timeTab by remember { mutableStateOf(false) }

    val confirmLabel = "${selected.monthNumber}월 ${selected.dayOfMonth}일 (${koreanWeekday(selected)}) ${if (isPm) DamoimStrings.PICKER_PM else DamoimStrings.PICKER_AM} $hour12:${minute.toString().padStart(2, '0')}"
    val selectedH24 = (hour12 % 12) + if (isPm) 12 else 0
    // 현재 시각 이후만 확정 가능 — 오늘 날짜에 지나간 시간을 고르면 비활성.
    val canConfirm = selected.atTime(selectedH24, minute) > now

    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(bottom = 34.dp)) {
            Text(title, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp), color = colors.textPrimary, modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp), textAlign = TextAlign.Center)
            // 날짜/시간 탭
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tab(DamoimStrings.PICKER_TAB_DATE, active = !timeTab, Modifier.weight(1f)) { timeTab = false }
                Tab(DamoimStrings.PICKER_TAB_TIME, active = timeTab, Modifier.weight(1f)) { timeTab = true }
            }
            // 날짜/시간 탭 영역을 고정 높이로 감싸 탭 전환·월 이동에도 시트 크기가 일정하게 유지된다.
            Box(Modifier.fillMaxWidth().height(332.dp)) {
                if (!timeTab) {
                    Column(Modifier.fillMaxWidth().align(Alignment.TopCenter)) {
                        // 월 이동
                        Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(schMonthTitle(year, month), style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(colors.surfaceVariant).noRippleClick { if (month == 1) { year--; month = 12 } else month-- }, contentAlignment = Alignment.Center) { BackChevronIcon(colors.textTertiary, Modifier.size(15.dp)) }
                                Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(colors.surfaceVariant).noRippleClick { if (month == 12) { year++; month = 1 } else month++ }, contentAlignment = Alignment.Center) { ChevronRightIcon(colors.textTertiary, Modifier.size(15.dp)) }
                            }
                        }
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                            DamoimStrings.PICKER_WEEKDAYS.forEachIndexed { i, w ->
                                Text(w, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = when (i) { 0 -> colors.calSunday; 6 -> colors.calSaturday; else -> colors.textMuted }, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            }
                        }
                        CalendarGrid(year, month, selected, today) { selected = it }
                    }
                } else {
                    Box(Modifier.fillMaxWidth().align(Alignment.Center)) {
                        TimeWheel(isPm, hour12, minute, onAmPm = { isPm = it }, onHour = { hour12 = it }, onMinute = { minute = it })
                    }
                }
            }
            // 확정 버튼 — 현재 시각 이후만 활성
            Box(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp).clip(RoundedCornerShape(14.dp)).background(if (canConfirm) colors.primary else colors.surfaceVariant)
                    .noRippleClick { if (canConfirm) onConfirm(selected, selectedH24, minute) }.padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(DamoimStrings.pickerConfirm(confirmLabel), style = DamoimTheme.typography.button, color = if (canConfirm) colors.onPrimary else colors.textDisabled)
            }
        }
    }
}

@Composable
private fun Tab(label: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Box(
        modifier.clip(RoundedCornerShape(12.dp)).background(if (active) colors.primaryContainer else colors.surfaceVariant).noRippleClick(onClick).padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = if (active) colors.primaryDark else colors.textTertiary)
    }
}
