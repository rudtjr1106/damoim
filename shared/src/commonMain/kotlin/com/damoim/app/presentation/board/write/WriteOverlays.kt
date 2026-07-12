package com.damoim.app.presentation.board.write

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.CameraIcon
import com.damoim.app.presentation.component.ChartIcon
import com.damoim.app.presentation.component.ChevronRightIcon
import com.damoim.app.presentation.component.CommentIcon
import com.damoim.app.presentation.component.DamoimBottomSheet
import com.damoim.app.presentation.component.CalendarGrid
import com.damoim.app.presentation.component.TimeWheel
import com.damoim.app.presentation.component.koreanWeekday
import com.damoim.app.presentation.component.ImageIcon
import com.damoim.app.presentation.component.LinkIcon
import com.damoim.app.presentation.component.LockIcon
import com.damoim.app.presentation.component.MegaphoneIcon
import com.damoim.app.presentation.component.PaperclipIcon
import com.damoim.app.presentation.component.PersonPlusIcon
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

// ── 52/53 카테고리 선택 시트 ──

/** 카테고리 확정 결과. 공지(운영진)면 필독/푸시 옵션 포함. */
data class CategoryChoice(val category: BoardCategory, val pinned: Boolean = false, val pushNotify: Boolean = false)

@Composable
internal fun CategorySheet(isAdmin: Boolean, current: BoardCategory, onConfirm: (CategoryChoice) -> Unit, onDismiss: () -> Unit) {
    val colors = DamoimTheme.colors
    var selected by remember { mutableStateOf(current) }
    var pin by remember { mutableStateOf(true) }
    var push by remember { mutableStateOf(true) }
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(DamoimStrings.CATEGORY_SHEET_TITLE, style = DamoimTheme.typography.titleLarge, color = colors.textPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text(DamoimStrings.CATEGORY_SHEET_SUBTITLE, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
                }
                if (isAdmin) Text(DamoimStrings.ADMIN_BADGE, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold), color = colors.primaryDark, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.primaryContainer).padding(horizontal = 10.dp, vertical = 5.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryOption(DamoimStrings.BOARD_LIST_FREE, DamoimStrings.CATEGORY_DESC_FREE, selected == BoardCategory.FREE, locked = false, icon = { c -> CommentIcon(c, Modifier.size(19.dp)) }) {
                    if (isAdmin) selected = BoardCategory.FREE else onConfirm(CategoryChoice(BoardCategory.FREE))
                }
                CategoryOption(DamoimStrings.BOARD_LIST_RECRUIT, DamoimStrings.CATEGORY_DESC_RECRUIT, selected == BoardCategory.RECRUIT, locked = false, icon = { c -> PersonPlusIcon(c, Modifier.size(19.dp)) }) {
                    if (isAdmin) selected = BoardCategory.RECRUIT else onConfirm(CategoryChoice(BoardCategory.RECRUIT))
                }
                // 공지: 회원=잠금, 운영진=선택 가능(+필독/푸시 확장)
                if (isAdmin) {
                    NoticeAdminOption(selected == BoardCategory.NOTICE, pin, push, onPin = { pin = it }, onPush = { push = it }) { selected = BoardCategory.NOTICE }
                } else {
                    CategoryOption(DamoimStrings.BOARD_LIST_NOTICE, DamoimStrings.CATEGORY_DESC_NOTICE, selected = false, locked = true, icon = { c -> MegaphoneIcon(c, Modifier.size(19.dp)) }) {}
                }
            }
            if (isAdmin) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.primary).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                    onConfirm(CategoryChoice(selected, pinned = selected == BoardCategory.NOTICE && pin, pushNotify = selected == BoardCategory.NOTICE && push))
                }.padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    Text(DamoimStrings.CATEGORY_CONFIRM, style = DamoimTheme.typography.button, color = colors.onPrimary)
                }
            } else {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceInput).padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    Text(DamoimStrings.CATEGORY_MEMBER_HINT, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
                }
            }
        }
    }
}

@Composable
private fun CategoryOption(title: String, desc: String, selected: Boolean, locked: Boolean, icon: @Composable (Color) -> Unit, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    val iconTint = if (locked) colors.textDisabled else colors.primaryDark
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(if (selected) colors.primaryContainer else if (locked) colors.surfaceInput else colors.surface)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) colors.primary else colors.divider, RoundedCornerShape(16.dp))
            .then(if (locked) Modifier else Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(if (locked) colors.surfaceDim else colors.primaryContainer), contentAlignment = Alignment.Center) { icon(iconTint) }
        Column(Modifier.weight(1f)) {
            Text(title, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = if (locked) colors.textMuted else colors.textPrimary)
            Text(desc, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
        }
        if (locked) LockIcon(colors.textDisabled, Modifier.size(17.dp))
    }
}

@Composable
private fun NoticeAdminOption(selected: Boolean, pin: Boolean, push: Boolean, onPin: (Boolean) -> Unit, onPush: (Boolean) -> Unit, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(if (selected) colors.primaryContainer else colors.surface)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) colors.primary else colors.divider, RoundedCornerShape(16.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(if (selected) colors.primary else colors.primaryContainer), contentAlignment = Alignment.Center) {
                MegaphoneIcon(if (selected) colors.onPrimary else colors.primaryDark, Modifier.size(19.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(DamoimStrings.BOARD_LIST_NOTICE, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp), color = colors.textPrimary)
                Text(DamoimStrings.CATEGORY_DESC_NOTICE, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
            }
        }
        if (selected) {
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surface).padding(horizontal = 14.dp)) {
                ToggleRow(DamoimStrings.CATEGORY_PIN, DamoimStrings.CATEGORY_PIN_DESC, pin, onToggle = { onPin(!pin) }, showDivider = true)
                ToggleRow(DamoimStrings.CATEGORY_PUSH, DamoimStrings.CATEGORY_PUSH_DESC, push, onToggle = { onPush(!push) }, showDivider = false)
            }
        }
    }
}

@Composable
private fun ToggleRow(title: String, desc: String, on: Boolean, onToggle: () -> Unit, showDivider: Boolean) {
    val colors = DamoimTheme.colors
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp), color = colors.textPrimary)
                Text(desc, style = DamoimTheme.typography.label.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Normal), color = colors.textMuted)
            }
            MiniToggle(on, onToggle)
        }
        if (showDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
    }
}

@Composable
internal fun MiniToggle(on: Boolean, onToggle: () -> Unit) {
    val colors = DamoimTheme.colors
    Box(
        Modifier.width(40.dp).height(24.dp).clip(RoundedCornerShape(99.dp)).background(if (on) colors.primary else colors.outline)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onToggle).padding(3.dp),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
    ) { Box(Modifier.size(18.dp).clip(CircleShape).background(colors.surface)) }
}

// ── 71 첨부 방식 시트 — 각 항목이 실제 피커/모드로 연결된다 ──
@Composable
internal fun AttachSheet(
    onPhoto: () -> Unit,
    onCamera: () -> Unit,
    onDocument: () -> Unit,
    onLink: () -> Unit,
    onPoll: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = DamoimTheme.colors
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 40.dp)) {
            Text(DamoimStrings.ATTACH_SHEET_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = colors.textPrimary, modifier = Modifier.padding(start = 6.dp, bottom = 8.dp))
            AttachRow(DamoimStrings.ATTACH_PHOTO, DamoimStrings.ATTACH_PHOTO_DESC, false, { ImageIcon(it, Modifier.size(20.dp)) }, showDivider = true, onClick = onPhoto)
            AttachRow(DamoimStrings.ATTACH_CAMERA, DamoimStrings.ATTACH_CAMERA_DESC, false, { CameraIcon(it, Modifier.size(20.dp)) }, showDivider = true, onClick = onCamera)
            AttachRow(DamoimStrings.ATTACH_DOC, DamoimStrings.ATTACH_DOC_DESC, true, { PaperclipIcon(it, Modifier.size(20.dp)) }, showDivider = true, onClick = onDocument)
            AttachRow(DamoimStrings.ATTACH_LINK, DamoimStrings.ATTACH_LINK_DESC, false, { LinkIcon(it, Modifier.size(20.dp)) }, showDivider = true, onClick = onLink)
            AttachRow(DamoimStrings.ATTACH_POLL, DamoimStrings.ATTACH_POLL_DESC, false, { ChartIcon(it, Modifier.size(20.dp)) }, showDivider = false, onClick = onPoll)
        }
    }
}

@Composable
private fun AttachRow(title: String, desc: String, highlight: Boolean, icon: @Composable (Color) -> Unit, showDivider: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth()
                .then(if (highlight) Modifier.clip(RoundedCornerShape(12.dp)).background(colors.primaryContainer.copy(alpha = 0.4f)) else Modifier)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
                .padding(horizontal = if (highlight) 12.dp else 6.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(if (highlight) colors.primary else colors.primaryContainer), contentAlignment = Alignment.Center) { icon(if (highlight) colors.onPrimary else colors.primaryDark) }
            Column(Modifier.weight(1f)) {
                Text(title, style = DamoimTheme.typography.bodySmall.copy(fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold, fontSize = 15.sp), color = colors.textPrimary)
                Text(desc, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
            }
        }
        if (showDivider) Box(Modifier.fillMaxWidth().padding(start = 6.dp, end = 6.dp).height(1.dp).background(colors.surfaceDim))
    }
}

// ══════════ 86 날짜·시간 선택기 (실동작) ══════════

/** 피커 확정 결과 — 표시 라벨 + 실제 날짜/시각. */
data class PickedDeadline(
    val label: String,          // "6.12 (목) 오후 6:00" / "6.10 (화) 자정"
    val date: LocalDate,
    val hour24: Int,
    val minute: Int,
)

/** 피커 확정값 → 절대 순간(epoch ms). 사용자의 시스템 타임존으로 해석해 서버에 절대 시각으로 전달. */
internal fun PickedDeadline.toEpochMillis(tz: TimeZone = TimeZone.currentSystemDefault()): Long =
    date.atTime(hour24, minute).toInstant(tz).toEpochMilliseconds()

/** epoch ms → 피커 초기값 복원(편집/임시저장 프리필 시 initial로 넘긴다). */
internal fun pickedDeadlineFromMillis(millis: Long, tz: TimeZone = TimeZone.currentSystemDefault()): PickedDeadline {
    val ldt = Instant.fromEpochMilliseconds(millis).toLocalDateTime(tz)
    val isPm = ldt.hour >= 12
    val hour12 = (ldt.hour % 12).let { if (it == 0) 12 else it }
    return PickedDeadline(deadlineLabel(ldt.date, isPm, hour12, ldt.minute), ldt.date, ldt.hour, ldt.minute)
}

/** 오늘부터 [date]까지의 D-day 라벨. */
internal fun ddayLabel(date: LocalDate, today: LocalDate): String? {
    val diff = date.toEpochDays() - today.toEpochDays()
    return when {
        diff > 0 -> "D-$diff"
        diff == 0L -> "D-DAY"
        else -> null
    }
}


private fun deadlineLabel(date: LocalDate, isPm: Boolean, hour12: Int, minute: Int): String =
    "${date.monthNumber}.${date.dayOfMonth} (${koreanWeekday(date)}) " +
        "${if (isPm) "오후" else "오전"} $hour12:${minute.toString().padStart(2, '0')}"

private fun midnightLabel(date: LocalDate): String =
    "${date.monthNumber}.${date.dayOfMonth} (${koreanWeekday(date)}) 자정"

/**
 * 화면 86 마감일 설정 바텀시트. 실제 달력(월 이동·과거 비활성)·시간 선택·프리셋이 동작하고
 * 확정 시 [PickedDeadline]을 돌려준다. 호출한 작성 화면 위 오버레이로 뜬다.
 */
@Composable
internal fun DatePickerSheet(
    initial: PickedDeadline? = null,
    onConfirm: (PickedDeadline) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = DamoimTheme.colors
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val initialDate = initial?.date ?: today.plus(DatePeriod(days = 1))
    var year by remember { mutableStateOf(initialDate.year) }
    var month by remember { mutableStateOf(initialDate.monthNumber) }
    var selected by remember { mutableStateOf(initialDate) }
    var isPm by remember { mutableStateOf(initial?.let { it.hour24 >= 12 } ?: true) }
    var hour12 by remember { mutableStateOf(initial?.let { (it.hour24 % 12).let { h -> if (h == 0) 12 else h } } ?: 6) }
    var minute by remember { mutableStateOf(initial?.minute ?: 0) }

    fun confirmCurrent() {
        val h24 = (hour12 % 12) + if (isPm) 12 else 0
        onConfirm(PickedDeadline(deadlineLabel(selected, isPm, hour12, minute), selected, h24, minute))
    }

    DamoimBottomSheet(onDismiss = onDismiss, showGrabber = true) {
        Column(Modifier.fillMaxWidth().padding(bottom = 34.dp)) {
            // 헤더
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(DamoimStrings.COMMON_CANCEL, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp), color = colors.textMuted, modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss))
                Text(DamoimStrings.PICKER_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp), color = colors.textPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text(DamoimStrings.COMMON_DONE, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp), color = colors.primary, modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { confirmCurrent() })
            }
            // 월 이동
            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(DamoimStrings.pickerMonth(year, month), style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(colors.surfaceVariant)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                if (month == 1) { year--; month = 12 } else month--
                            },
                        contentAlignment = Alignment.Center,
                    ) { BackChevronIcon(colors.textTertiary, Modifier.size(15.dp)) }
                    Box(
                        Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(colors.surfaceVariant)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                if (month == 12) { year++; month = 1 } else month++
                            },
                        contentAlignment = Alignment.Center,
                    ) { ChevronRightIcon(colors.textTertiary, Modifier.size(15.dp)) }
                }
            }
            // 요일
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                DamoimStrings.PICKER_WEEKDAYS.forEachIndexed { i, w ->
                    Text(w, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = when (i) { 0 -> colors.calSunday; 6 -> colors.calSaturday; else -> colors.textMuted }, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            // 날짜 그리드 — 실제 달력 계산 (일요일 시작, 과거 날짜 비활성)
            CalendarGrid(year, month, selected, today) { selected = it }
            Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp).height(1.dp).background(colors.surfaceDim))
            // 시간 선택
            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(DamoimStrings.PICKER_TIME, style = DamoimTheme.typography.bodyStrong.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary, modifier = Modifier.weight(1f))
                Text(
                    "${if (isPm) DamoimStrings.PICKER_PM else DamoimStrings.PICKER_AM} $hour12:${minute.toString().padStart(2, '0')}",
                    style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.primary,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(colors.primaryContainer).padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            TimeWheel(
                isPm = isPm, hour12 = hour12, minute = minute,
                onAmPm = { isPm = it }, onHour = { hour12 = it }, onMinute = { minute = it },
            )
            // 프리셋 — 실제 날짜로 즉시 확정
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PresetChip(DamoimStrings.PICKER_PRESETS[0]) {
                    onConfirm(PickedDeadline(midnightLabel(today), today, 23, 59))
                }
                PresetChip(DamoimStrings.PICKER_PRESETS[1]) {
                    val d = today.plus(DatePeriod(days = 1))
                    onConfirm(PickedDeadline(deadlineLabel(d, isPm = true, hour12 = 6, minute = 0), d, 18, 0))
                }
                PresetChip(DamoimStrings.PICKER_PRESETS[2]) {
                    val d = today.plus(DatePeriod(days = 7))
                    val h24 = (hour12 % 12) + if (isPm) 12 else 0
                    onConfirm(PickedDeadline(deadlineLabel(d, isPm, hour12, minute), d, h24, minute))
                }
            }
        }
    }
}

@Composable
private fun PresetChip(label: String, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Text(
        label, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold), color = colors.textTertiary,
        modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.surfaceVariant)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}
