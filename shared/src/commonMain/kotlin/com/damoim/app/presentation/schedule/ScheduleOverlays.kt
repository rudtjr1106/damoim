package com.damoim.app.presentation.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.domain.model.FormQuestion
import com.damoim.app.domain.model.QuestionAnswer
import com.damoim.app.domain.model.QuestionType
import com.damoim.app.domain.model.Schedule
import com.damoim.app.presentation.component.DamoimBottomSheet
import com.damoim.app.presentation.component.DamoimDialog
import com.damoim.app.presentation.component.DialogButton
import com.damoim.app.presentation.component.WarningIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 일정/이벤트 공용 오버레이 — 24 상세와 48 내 신청 내역이 함께 쓴다. (feature 루트 유지)
 */

// ── 25 이벤트 참여 신청(신규 또는 48 응답 수정) ──
@Composable
fun ApplyFormSheet(
    schedule: Schedule,
    existingAnswers: List<QuestionAnswer>,
    isEdit: Boolean = false,
    onSubmit: (List<QuestionAnswer>) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = DamoimTheme.colors
    val questions = schedule.event?.form.orEmpty()
    val choices = remember { mutableStateMapOf<Long, Set<String>>() }
    val texts = remember { mutableStateMapOf<Long, String>() }
    remember(existingAnswers) {
        questions.forEach { q ->
            val prev = existingAnswers.firstOrNull { it.question == q.text }?.answer
            if (prev != null) {
                if (q.type == QuestionType.TEXT) texts[q.id] = prev
                else choices[q.id] = prev.split(", ").filter { it.isNotBlank() }.toSet()
            }
        }
        true
    }

    fun canSubmit(): Boolean = questions.all { q ->
        !q.required || (if (q.type == QuestionType.TEXT) texts[q.id]?.isNotBlank() == true else choices[q.id]?.isNotEmpty() == true)
    }

    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 44.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${schedule.title} ${DamoimStrings.EVENT_APPLY_SHEET_TITLE}", style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 19.sp), color = colors.textPrimary)
                Text("${schMidDate(schedule.date)}${schedule.location.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""}", style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp), color = colors.textMuted)
            }
            questions.forEach { q ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row {
                        Text(q.text, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = colors.textSecondary)
                        if (q.required) Text(" ${DamoimStrings.REQUIRED_MARK}", style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.primary)
                    }
                    when (q.type) {
                        QuestionType.TEXT -> AnswerTextBox(texts[q.id].orEmpty()) { texts[q.id] = it }
                        else -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            q.options.forEach { opt ->
                                val selected = choices[q.id]?.contains(opt) == true
                                OptionChip(opt, selected, Modifier.weight(1f)) {
                                    choices[q.id] = if (q.type == QuestionType.MULTI) {
                                        val cur = choices[q.id].orEmpty()
                                        if (selected) cur - opt else cur + opt
                                    } else setOf(opt)
                                }
                            }
                        }
                    }
                }
            }
            val enabled = canSubmit()
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (enabled) colors.primary else colors.primary.copy(alpha = 0.35f))
                    .then(if (enabled) Modifier.noRippleClick {
                        onSubmit(questions.map { q -> QuestionAnswer(q.text, if (q.type == QuestionType.TEXT) texts[q.id].orEmpty() else choices[q.id].orEmpty().joinToString(", ")) })
                    } else Modifier)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (isEdit) DamoimStrings.EVENT_APPLY_UPDATE else DamoimStrings.EVENT_APPLY_SUBMIT, style = DamoimTheme.typography.button, color = colors.onPrimary)
            }
        }
    }
}

@Composable
private fun OptionChip(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Box(
        modifier.clip(RoundedCornerShape(14.dp))
            .background(if (selected) colors.primaryContainer else colors.surface)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) colors.primary else colors.divider, RoundedCornerShape(14.dp))
            .noRippleClick(onClick).padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = DamoimTheme.typography.bodySmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold, fontSize = 14.sp), color = if (selected) colors.primaryDark else colors.textTertiary)
    }
}

@Composable
private fun AnswerTextBox(value: String, onValueChange: (String) -> Unit) {
    val colors = DamoimTheme.colors
    Box(Modifier.fillMaxWidth().heightIn(min = 60.dp).clip(RoundedCornerShape(14.dp)).border(1.dp, colors.divider, RoundedCornerShape(14.dp)).padding(horizontal = 16.dp, vertical = 14.dp)) {
        if (value.isEmpty()) Text(DamoimStrings.EVENT_APPLY_TEXT_PLACEHOLDER, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.Normal), color = colors.textDisabled)
        BasicTextField(
            value = value, onValueChange = onValueChange,
            textStyle = DamoimTheme.typography.body.copy(color = colors.textPrimary, fontWeight = FontWeight.Normal),
            cursorBrush = SolidColor(colors.primary),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ── 63 / 조기마감 / 신청취소 확인 다이얼로그 ──
@Composable
fun EventConfirmDialog(
    title: String,
    desc: String,
    confirm: String,
    destructive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = DamoimTheme.colors
    val accent = if (destructive) colors.error else colors.primary
    DamoimDialog(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(999.dp)).background(if (destructive) colors.errorContainer else colors.primaryContainer), contentAlignment = Alignment.Center) {
                WarningIcon(accent, Modifier.size(26.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(title, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = colors.textPrimary)
            Text(desc, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, modifier = Modifier.padding(bottom = 8.dp), textAlign = TextAlign.Center)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogButton(DamoimStrings.COMMON_CANCEL, colors.surfaceVariant, colors.textTertiary, Modifier.weight(1f), onDismiss)
                DialogButton(confirm, accent, colors.onPrimary, Modifier.weight(1f), onConfirm)
            }
        }
    }
}
