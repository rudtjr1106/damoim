package com.damoim.app.presentation.schedule.register

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.domain.model.FormQuestion
import com.damoim.app.domain.model.QuestionType
import com.damoim.app.presentation.component.CloseIcon
import com.damoim.app.presentation.component.DamoimBottomSheet
import com.damoim.app.presentation.component.MoreIcon
import com.damoim.app.presentation.component.PlusIcon
import com.damoim.app.presentation.component.dashedBorder
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 46 신청 양식 편집(전체화면 오버레이). 질문 추가/편집은 51 [QuestionEditorSheet]로. */
@Composable
internal fun FormEditorOverlay(initialForm: List<FormQuestion>, onSave: (List<FormQuestion>) -> Unit, onCancel: () -> Unit) {
    val colors = DamoimTheme.colors
    var questions by remember { mutableStateOf(initialForm) }
    var editing by remember { mutableStateOf<FormQuestion?>(null) }
    var editingNew by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(colors.surfaceInput)) {
        Column(Modifier.fillMaxSize()) {
            // 헤더
            Row(
                Modifier.fillMaxWidth().background(colors.surface).windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(DamoimStrings.COMMON_CANCEL, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp), color = colors.textMuted, modifier = Modifier.noRippleClick(onCancel))
                Text(DamoimStrings.FORM_EDIT_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp), color = colors.textPrimary, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text(DamoimStrings.SCHEDULE_SAVE, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp), color = colors.primary, modifier = Modifier.noRippleClick { onSave(questions) })
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
            // 리스트
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("회원이 참여 신청할 때 답하는 질문이에요", style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp), color = colors.textMuted, modifier = Modifier.padding(horizontal = 4.dp))
                questions.forEachIndexed { i, q -> QuestionCard(i + 1, q) { editingNew = false; editing = q } }
                // 질문 추가
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(colors.surface).dashedBorder(colors.accentSkySoft, 1.5.dp, 16.dp)
                        .noRippleClick { editingNew = true; editing = FormQuestion(0, "", QuestionType.SELECT, listOf("", ""), true) }.padding(vertical = 15.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlusIcon(colors.primary, Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(DamoimStrings.FORM_ADD_QUESTION, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = colors.primary)
                }
                Spacer(Modifier.height(40.dp))
            }
        }

        // 51 질문 편집 오버레이
        editing?.let { target ->
            QuestionEditorSheet(
                question = target,
                isNew = editingNew,
                onDone = { edited ->
                    questions = if (editingNew) {
                        val id = (questions.maxOfOrNull { it.id } ?: 9000L) + 1
                        questions + edited.copy(id = id)
                    } else {
                        questions.map { if (it.id == target.id) edited else it }
                    }
                    editing = null
                },
                onDelete = { questions = questions.filterNot { it.id == target.id }; editing = null },
                onDismiss = { editing = null },
            )
        }
    }
}

@Composable
private fun QuestionCard(index: Int, q: FormQuestion, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(colors.surface).border(1.dp, colors.dividerLight, RoundedCornerShape(18.dp)).noRippleClick(onClick).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Q$index", style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp), color = colors.onPrimary, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.primary).padding(horizontal = 9.dp, vertical = 3.dp))
            Text(q.text.ifBlank { "(제목 없음)" }, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
            TypeBadge(q.type)
            MoreIcon(colors.outlineStrong, Modifier.size(16.dp))
        }
        if (q.type != QuestionType.TEXT && q.options.any { it.isNotBlank() }) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                q.options.filter { it.isNotBlank() }.forEach { opt ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(colors.surfaceInput).padding(horizontal = 14.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.size(14.dp).clip(CircleShape).border(1.5.dp, colors.outlineStrong, CircleShape))
                        Text(opt, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.5.sp), color = colors.textSecondary)
                    }
                }
            }
        }
        if (q.required) Text(DamoimStrings.FORM_REQUIRED, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = colors.primaryDark)
    }
}

@Composable
internal fun TypeBadge(type: QuestionType) {
    val colors = DamoimTheme.colors
    val (label, bg, fg) = when (type) {
        QuestionType.SELECT -> Triple(DamoimStrings.FORM_TYPE_SELECT, colors.primaryContainer, colors.primaryDark)
        QuestionType.MULTI -> Triple(DamoimStrings.FORM_TYPE_MULTI, colors.primaryContainer, colors.primaryDark)
        QuestionType.TEXT -> Triple(DamoimStrings.FORM_TYPE_TEXT, colors.surfaceVariant, colors.textTertiary)
    }
    Text(label, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = fg, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp))
}

/** 51 질문 추가/편집 — 바텀시트(뒤 46 리스트가 딤 처리되어 비침). */
@Composable
private fun QuestionEditorSheet(
    question: FormQuestion,
    isNew: Boolean,
    onDone: (FormQuestion) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = DamoimTheme.colors
    var text by remember { mutableStateOf(question.text) }
    var type by remember { mutableStateOf(question.type) }
    var options by remember { mutableStateOf(question.options.ifEmpty { listOf("", "") }) }
    var required by remember { mutableStateOf(question.required) }

    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isNew) DamoimStrings.FORM_QUESTION_ADD_TITLE else DamoimStrings.FORM_QUESTION_EDIT_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = colors.textPrimary, modifier = Modifier.weight(1f))
                Box(Modifier.size(24.dp).noRippleClick(onDismiss), contentAlignment = Alignment.Center) { CloseIcon(colors.textMuted, Modifier.size(20.dp)) }
            }
            // 질문 내용
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(DamoimStrings.FORM_QUESTION_CONTENT, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = colors.textSecondary)
                EditorField(text, DamoimStrings.FORM_QUESTION_PLACEHOLDER) { text = it }
            }
            // 응답 유형
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(DamoimStrings.FORM_ANSWER_TYPE, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = colors.textSecondary)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeOption(DamoimStrings.FORM_TYPE_SELECT, type == QuestionType.SELECT, Modifier.weight(1f)) { type = QuestionType.SELECT }
                    TypeOption(DamoimStrings.FORM_TYPE_TEXT, type == QuestionType.TEXT, Modifier.weight(1f)) { type = QuestionType.TEXT }
                    TypeOption(DamoimStrings.FORM_TYPE_MULTI, type == QuestionType.MULTI, Modifier.weight(1f)) { type = QuestionType.MULTI }
                }
            }
            // 항목(선택형/복수)
            if (type != QuestionType.TEXT) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    options.forEachIndexed { i, opt ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) { EditorField(opt, DamoimStrings.FORM_OPTION_PLACEHOLDER) { v -> options = options.toMutableList().also { it[i] = v } } }
                            if (options.size > 1) Box(Modifier.size(24.dp).noRippleClick { options = options.filterIndexed { idx, _ -> idx != i } }, contentAlignment = Alignment.Center) { CloseIcon(colors.outlineStrong, Modifier.size(15.dp)) }
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).dashedBorder(colors.outline, 1.5.dp, 10.dp).noRippleClick { options = options + "" }.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlusIcon(colors.textMuted, Modifier.size(12.dp)); Spacer(Modifier.width(6.dp))
                        Text(DamoimStrings.FORM_OPTION_ADD, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = colors.textMuted)
                    }
                }
            }
            // 필수
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(DamoimStrings.FORM_REQUIRED, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp), color = colors.textTertiary, modifier = Modifier.weight(1f))
                Toggle(required) { required = !required }
            }
            // 버튼
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!isNew) Box(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(colors.errorContainer).noRippleClick(onDelete).padding(vertical = 15.dp), contentAlignment = Alignment.Center) {
                    Text(DamoimStrings.FORM_QUESTION_DELETE, style = DamoimTheme.typography.bodyStrong, color = colors.error)
                }
                val enabled = text.isNotBlank() && (type == QuestionType.TEXT || options.count { it.isNotBlank() } >= 2)
                Box(
                    Modifier.weight(if (isNew) 1f else 2f).clip(RoundedCornerShape(14.dp)).background(if (enabled) colors.primary else colors.primary.copy(alpha = 0.35f))
                        .then(if (enabled) Modifier.noRippleClick { onDone(FormQuestion(question.id, text.trim(), type, if (type == QuestionType.TEXT) emptyList() else options.filter { it.isNotBlank() }, required)) } else Modifier).padding(vertical = 15.dp),
                    contentAlignment = Alignment.Center,
                ) { Text(DamoimStrings.FORM_QUESTION_DONE, style = DamoimTheme.typography.bodyStrong, color = colors.onPrimary) }
            }
        }
    }
}

@Composable
private fun TypeOption(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(if (selected) colors.primaryContainer else colors.surfaceInput).border(if (selected) 1.5.dp else 1.dp, if (selected) colors.primary else colors.divider, RoundedCornerShape(12.dp)).noRippleClick(onClick).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Text(label, style = DamoimTheme.typography.caption.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold), color = if (selected) colors.primaryDark else colors.textTertiary)
    }
}

@Composable
private fun EditorField(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    val colors = DamoimTheme.colors
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(colors.surfaceInput).border(1.dp, colors.divider, RoundedCornerShape(10.dp)).padding(horizontal = 14.dp, vertical = 12.dp)) {
        if (value.isEmpty()) Text(placeholder, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.5.sp), color = colors.textDisabled)
        BasicTextField(value, onValueChange, textStyle = DamoimTheme.typography.bodySmall.copy(color = colors.textPrimary, fontWeight = FontWeight.Normal, fontSize = 13.5.sp), cursorBrush = SolidColor(colors.primary), modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun Toggle(on: Boolean, onToggle: () -> Unit) {
    val colors = DamoimTheme.colors
    Box(Modifier.width(40.dp).height(24.dp).clip(RoundedCornerShape(99.dp)).background(if (on) colors.primary else colors.outline).noRippleClick(onToggle).padding(3.dp), contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart) {
        Box(Modifier.size(18.dp).clip(CircleShape).background(colors.surface))
    }
}
