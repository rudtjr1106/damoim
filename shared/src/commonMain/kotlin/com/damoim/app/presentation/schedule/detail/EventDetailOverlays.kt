package com.damoim.app.presentation.schedule.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.domain.model.Schedule
import com.damoim.app.presentation.component.ClockIcon
import com.damoim.app.presentation.component.CloseIcon
import com.damoim.app.presentation.component.DamoimBottomSheet
import com.damoim.app.presentation.component.EditIcon
import com.damoim.app.presentation.component.ListIcon
import com.damoim.app.presentation.component.MegaphoneIcon
import com.damoim.app.presentation.component.SheetActionRow
import com.damoim.app.presentation.component.SheetCloseButton
import com.damoim.app.presentation.schedule.EventBadge
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

// ── 62 이벤트 ⋯ 메뉴 ──
@Composable
internal fun EventMenuSheet(
    schedule: Schedule,
    onEdit: () -> Unit,
    onApplicants: () -> Unit,
    onAnnounce: () -> Unit,
    onCloseEarly: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = DamoimTheme.colors
    val event = schedule.event
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 44.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // 이벤트 정보 배너
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.surfaceInput).padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EventBadge()
                Text(
                    "${schedule.title}${event?.let { " · ${DamoimStrings.eventParticipation(it.appliedCount, it.capacity)}" } ?: ""}",
                    style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp), color = colors.textTertiary,
                )
            }
            Column(Modifier.fillMaxWidth()) {
                SheetActionRow(DamoimStrings.EVENT_MENU_EDIT, onEdit, icon = { EditIcon(colors.textSecondary, Modifier.size(19.dp)) })
                SheetActionRow(
                    DamoimStrings.EVENT_MENU_APPLICANTS, onApplicants,
                    icon = { ListIcon(colors.textSecondary, Modifier.size(19.dp)) },
                    trailing = event?.let { { CountBadge("${it.appliedCount}${DamoimStrings.SCHEDULE_CAPACITY_UNIT}") } },
                )
                SheetActionRow(DamoimStrings.EVENT_MENU_ANNOUNCE, onAnnounce, icon = { MegaphoneIcon(colors.textSecondary, Modifier.size(19.dp)) })
                if (event != null) SheetActionRow(DamoimStrings.EVENT_MENU_CLOSE, onCloseEarly, icon = { ClockIcon(colors.textSecondary, Modifier.size(19.dp)) })
                SheetActionRow(DamoimStrings.EVENT_MENU_CANCEL, onCancel, textColor = colors.error, icon = { CloseIcon(colors.error, Modifier.size(19.dp)) }, showDivider = false)
            }
            Text(DamoimStrings.EVENT_CANCEL_DESC, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceInput).padding(horizontal = 14.dp, vertical = 11.dp))
            SheetCloseButton(onDismiss)
        }
    }
}

@Composable
private fun CountBadge(text: String) {
    val colors = DamoimTheme.colors
    Text(text, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp), color = colors.primaryDark, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.primaryContainer).padding(horizontal = 8.dp, vertical = 3.dp))
}
