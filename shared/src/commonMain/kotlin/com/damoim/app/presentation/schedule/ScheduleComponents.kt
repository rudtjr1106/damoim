package com.damoim.app.presentation.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.domain.model.EventStatus
import com.damoim.app.domain.model.Schedule
import com.damoim.app.domain.model.ScheduleAccent
import com.damoim.app.presentation.component.ChevronRightIcon
import com.damoim.app.presentation.component.ClockIcon
import com.damoim.app.presentation.component.LocationIcon
import com.damoim.app.presentation.component.PlusIcon
import com.damoim.app.presentation.component.koreanWeekday
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus

// ── 날짜 포맷 헬퍼 ──
/** "8월 14일 (목)" */
fun schMidDate(d: LocalDate) = "${d.monthNumber}월 ${d.dayOfMonth}일 (${koreanWeekday(d)})"
/** "8월 14일 목요일" */
fun schFullDate(d: LocalDate) = "${d.monthNumber}월 ${d.dayOfMonth}일 ${koreanWeekday(d)}요일"
/** "2025년 6월" */
fun schMonthTitle(year: Int, month: Int) = "${year}년 ${month}월"
/** "8.7 (목) 오전 10:00" 형태의 짧은 일시. */
fun schShortWhen(d: LocalDate, time: String) = "${d.monthNumber}.${d.dayOfMonth} (${koreanWeekday(d)}) $time"

fun ddayText(date: LocalDate, today: LocalDate): String {
    val diff = date.toEpochDays() - today.toEpochDays()
    return when {
        diff > 0 -> "D-$diff"
        diff == 0L -> "D-DAY"
        else -> DamoimStrings.EVENT_ENDED_BADGE
    }
}

@Composable
private fun accentColor(accent: ScheduleAccent): Color =
    if (accent == ScheduleAccent.SKY) DamoimTheme.colors.accentSky else DamoimTheme.colors.primary

// ── 뷰 전환 세그먼트(캘린더/목록) ──
@Composable
fun ScheduleSegment(listMode: Boolean, onSelect: (listMode: Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SegChip(DamoimStrings.SCHEDULE_SEG_CALENDAR, active = !listMode) { onSelect(false) }
        SegChip(DamoimStrings.SCHEDULE_SEG_LIST, active = listMode) { onSelect(true) }
    }
}

@Composable
private fun SegChip(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Text(
        label,
        style = DamoimTheme.typography.caption.copy(fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold),
        color = if (active) colors.onPrimary else colors.textTertiary,
        modifier = Modifier.clip(RoundedCornerShape(999.dp))
            .background(if (active) colors.textPrimary else colors.surfaceVariant)
            .noRippleClick(onClick).padding(horizontal = 14.dp, vertical = 7.dp),
    )
}

// ── 뱃지 ──
@Composable
fun EventBadge() {
    val colors = DamoimTheme.colors
    Text(
        DamoimStrings.EVENT_BADGE,
        style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp),
        color = colors.onPrimary,
        modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.primary).padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
fun EventStatusBadge(status: EventStatus) {
    val colors = DamoimTheme.colors
    val (label, bg, fg) = when (status) {
        EventStatus.OPEN -> Triple(DamoimStrings.EVENT_OPEN_BADGE, colors.primaryContainer, colors.primaryDark)
        EventStatus.CLOSED -> Triple(DamoimStrings.EVENT_CLOSED_BADGE, colors.surfaceDim, colors.textMuted)
        EventStatus.ENDED -> Triple(DamoimStrings.EVENT_ENDED_BADGE, colors.surfaceDim, colors.textMuted)
    }
    Text(
        label,
        style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp),
        color = fg,
        modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

// ── + FAB (일정 등록) ──
@Composable
fun ScheduleFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DamoimTheme.colors
    Box(
        modifier.size(56.dp).clip(CircleShape).background(colors.primary).noRippleClick(onClick),
        contentAlignment = Alignment.Center,
    ) { PlusIcon(colors.onPrimary, Modifier.size(24.dp)) }
}

// ── 21 캘린더 ──
@Composable
fun ScheduleCalendar(
    year: Int,
    month: Int,
    selected: LocalDate,
    today: LocalDate,
    eventDays: Set<LocalDate>,
    onSelect: (LocalDate) -> Unit,
) {
    val colors = DamoimTheme.colors
    val first = LocalDate(year, month, 1)
    val offset = first.dayOfWeek.isoDayNumber % 7
    val daysInMonth = first.plus(DatePeriod(months = 1)).plus(DatePeriod(days = -1)).dayOfMonth
    val rows = (offset + daysInMonth + 6) / 7
    Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        // 요일 헤더
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            DamoimStrings.PICKER_WEEKDAYS.forEachIndexed { i, w ->
                Text(
                    w,
                    style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    color = when (i) { 0 -> colors.error; 6 -> colors.primary; else -> colors.textMuted },
                    modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                )
            }
        }
        repeat(rows) { row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val dayNum = row * 7 + col - offset + 1
                    Box(Modifier.weight(1f).height(44.dp), contentAlignment = Alignment.Center) {
                        if (dayNum in 1..daysInMonth) {
                            val date = LocalDate(year, month, dayNum)
                            val isSelected = date == selected
                            val isToday = date == today
                            val hasEvent = date in eventDays
                            Box(
                                Modifier.size(34.dp).clip(CircleShape)
                                    .then(
                                        when {
                                            isSelected -> Modifier.background(colors.primary)
                                            isToday -> Modifier.border(1.5.dp, colors.primary, CircleShape)
                                            else -> Modifier
                                        },
                                    )
                                    .noRippleClick { onSelect(date) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "$dayNum",
                                    style = DamoimTheme.typography.body.copy(fontWeight = if (isSelected || isToday) FontWeight.ExtraBold else FontWeight.Normal, fontSize = 13.sp),
                                    color = if (isSelected) colors.onPrimary else colors.textPrimary,
                                )
                                if (hasEvent && !isSelected) {
                                    Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp).size(5.dp).clip(CircleShape).background(colors.primary))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── 21 선택 날짜 일정 카드(좌측 컬러바) ──
@Composable
fun ScheduleBarCard(schedule: Schedule, onClick: () -> Unit, onAddCalendar: () -> Unit) {
    val colors = DamoimTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .border(1.dp, colors.divider, RoundedCornerShape(16.dp))
            .noRippleClick(onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.width(4.dp).height(40.dp).clip(RoundedCornerShape(99.dp)).background(accentColor(schedule.accent)))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(schedule.title, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                if (schedule.isEvent) EventBadge()
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetaItem({ ClockIcon(colors.textMuted, Modifier.size(12.dp)) }, schedule.timeLabel)
                if (schedule.location.isNotBlank()) MetaItem({ LocationIcon(colors.textMuted, Modifier.size(12.dp)) }, schedule.location)
            }
        }
        if (schedule.isEvent) {
            ChevronRightIcon(colors.outlineStrong, Modifier.size(18.dp))
        } else {
            AddCalendarChip(added = schedule.addedToMyCalendar, onClick = onAddCalendar)
        }
    }
}

@Composable
private fun MetaItem(icon: @Composable () -> Unit, text: String) {
    val colors = DamoimTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        icon()
        Text(text, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
    }
}

@Composable
fun AddCalendarChip(added: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Text(
        if (added) DamoimStrings.SCHEDULE_ADDED_MY else DamoimStrings.SCHEDULE_ADD_MY,
        style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
        color = if (added) colors.textMuted else colors.primary,
        modifier = Modifier.clip(RoundedCornerShape(999.dp))
            .background(if (added) colors.surfaceDim else colors.primaryContainer)
            .noRippleClick(onClick).padding(horizontal = 12.dp, vertical = 7.dp),
    )
}

// ── 22 목록: 강조(다크) 카드 ──
@Composable
fun ScheduleEmphasisCard(schedule: Schedule, today: LocalDate, onClick: () -> Unit, onAddCalendar: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.onDarkNavy)
            .noRippleClick(onClick).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    ddayText(schedule.date, today),
                    style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp),
                    color = colors.accentSky,
                    modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.onPrimary.copy(alpha = 0.10f)).padding(horizontal = 10.dp, vertical = 4.dp),
                )
                Spacer(Modifier.weight(1f))
                Text(schShortWhen(schedule.date, schedule.timeLabel), style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.onPrimary.copy(alpha = 0.55f))
            }
            Text(schedule.title, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = colors.onPrimary)
            if (schedule.location.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    LocationIcon(colors.onPrimary.copy(alpha = 0.6f), Modifier.size(13.dp))
                    Text(schedule.location, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp), color = colors.onPrimary.copy(alpha = 0.6f))
                }
            }
        }
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.onPrimary.copy(alpha = 0.14f))
                .noRippleClick(onAddCalendar).padding(vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (schedule.addedToMyCalendar) DamoimStrings.SCHEDULE_ADDED_MY else DamoimStrings.SCHEDULE_ADD_MY,
                style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = colors.onPrimary,
            )
        }
    }
}

// ── 22 목록: 날짜박스 행 ──
@Composable
fun ScheduleDateRow(schedule: Schedule, today: LocalDate, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    val muted = schedule.date < today
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, colors.divider, RoundedCornerShape(16.dp))
            .noRippleClick(onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 날짜 박스
        Column(
            Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(if (muted) colors.surfaceVariant else colors.primaryContainer),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
        ) {
            Text("${schedule.date.monthNumber}월", style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = if (muted) colors.textMuted else colors.primaryDark)
            Text("${schedule.date.dayOfMonth}", style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp), color = colors.textPrimary)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(schedule.title, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = colors.textPrimary)
                if (schedule.isEvent) EventBadge()
            }
            val sub = buildString {
                append(schedule.timeLabel)
                val extra = schedule.event?.meta?.takeIf { it.isNotBlank() } ?: schedule.location
                if (extra.isNotBlank()) append(" · $extra")
                schedule.event?.let { append(" · ${DamoimStrings.eventParticipation(it.appliedCount, it.capacity)}") }
            }
            Text(sub, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
        }
        Text(
            ddayText(schedule.date, today),
            style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold),
            color = if (muted) colors.textDisabled else colors.primaryDark,
        )
    }
}
