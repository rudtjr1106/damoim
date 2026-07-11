package com.damoim.app.presentation.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.presentation.component.DamoimBottomSheet
import com.damoim.app.presentation.component.DamoimTextField
import com.damoim.app.presentation.component.DialogButton
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 44 새 기수 추가 시트. 기수 번호·표시 이름을 입력해 실제로 기수를 추가한다(활동시작·기본배정은 표시용). */
@Composable
internal fun AddCohortSheet(onDismiss: () -> Unit, onConfirm: (short: String, name: String) -> Unit) {
    CohortFormSheet(
        title = DamoimStrings.COHORT_ADD_TITLE,
        confirmLabel = DamoimStrings.COHORT_ADD_CONFIRM,
        initialShort = "", initialName = "", showExtras = true,
        onDismiss = onDismiss, onConfirm = onConfirm,
    )
}

/** 19 기수 이름 변경 시트(신설). */
@Composable
internal fun RenameCohortSheet(currentShort: String, currentName: String, onDismiss: () -> Unit, onConfirm: (short: String, name: String) -> Unit) {
    CohortFormSheet(
        title = DamoimStrings.COHORT_RENAME_TITLE,
        confirmLabel = DamoimStrings.COHORT_RENAME_CONFIRM,
        initialShort = currentShort, initialName = currentName, showExtras = false,
        onDismiss = onDismiss, onConfirm = onConfirm,
    )
}

@Composable
private fun CohortFormSheet(
    title: String,
    confirmLabel: String,
    initialShort: String,
    initialName: String,
    showExtras: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    val colors = DamoimTheme.colors
    var short by remember { mutableStateOf(initialShort) }
    var name by remember { mutableStateOf(initialName) }
    var start by remember { mutableStateOf("") }
    var defaultAssign by remember { mutableStateOf(true) }
    val valid = short.isNotBlank()
    val fieldStyle = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text(title, style = DamoimTheme.typography.titleLarge.copy(fontSize = 19.sp), color = colors.textPrimary)
            Field(DamoimStrings.COHORT_FIELD_SHORT, required = true) {
                DamoimTextField(short, { short = it }, placeholder = DamoimStrings.COHORT_FIELD_SHORT_HINT, strokeColor = colors.divider, cornerRadius = 14.dp, borderWidth = 1.dp, textStyle = fieldStyle)
            }
            Field(DamoimStrings.COHORT_FIELD_NAME) {
                DamoimTextField(name, { name = it }, placeholder = DamoimStrings.COHORT_FIELD_NAME_HINT, strokeColor = colors.divider, cornerRadius = 14.dp, borderWidth = 1.dp, textStyle = fieldStyle)
            }
            if (showExtras) {
                Field(DamoimStrings.COHORT_FIELD_START) {
                    DamoimTextField(start, { start = it }, placeholder = DamoimStrings.COHORT_FIELD_START_HINT, strokeColor = colors.divider, cornerRadius = 14.dp, borderWidth = 1.dp, textStyle = fieldStyle)
                }
                // 기본 배정 토글(표시용)
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(DamoimStrings.COHORT_DEFAULT_ASSIGN, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                        Text(DamoimStrings.COHORT_DEFAULT_ASSIGN_SUB, style = DamoimTheme.typography.caption, color = colors.textMuted)
                    }
                    ToggleSwitch(defaultAssign) { defaultAssign = !defaultAssign }
                }
            }
            DialogButton(
                confirmLabel, bg = colors.primary, fg = colors.onPrimary,
                modifier = Modifier.fillMaxWidth().alpha(if (valid) 1f else 0.35f),
                onClick = { if (valid) onConfirm(short.trim(), name.trim().ifBlank { short.trim() }) },
            )
        }
    }
}

/** 44×26 토글 스위치. */
@Composable
internal fun ToggleSwitch(on: Boolean, onToggle: () -> Unit) {
    val colors = DamoimTheme.colors
    Box(
        Modifier.size(width = 44.dp, height = 26.dp).clip(RoundedCornerShape(999.dp)).background(if (on) colors.primary else colors.outline).noRippleClick(onToggle),
    ) {
        Box(Modifier.align(if (on) Alignment.CenterEnd else Alignment.CenterStart).padding(horizontal = 3.dp).size(20.dp).clip(CircleShape).background(colors.surface))
    }
}

@Composable
private fun Field(label: String, required: Boolean = false, content: @Composable () -> Unit) {
    val colors = DamoimTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row {
            Text(label, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.textSecondary)
            if (required) Text(" ${DamoimStrings.REQUIRED_MARK}", style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.primary)
        }
        content()
    }
}
