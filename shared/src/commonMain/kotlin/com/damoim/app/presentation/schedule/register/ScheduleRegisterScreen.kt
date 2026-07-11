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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.presentation.component.ChevronRightIcon
import com.damoim.app.presentation.component.DamoimTextField
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.schedule.schMidDate
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme
import kotlinx.datetime.LocalDate

private enum class PickTarget { START, END, DEADLINE }

private fun fmtDateTime(date: LocalDate, hour: Int, minute: Int): String {
    val ampm = if (hour < 12) DamoimStrings.PICKER_AM else DamoimStrings.PICKER_PM
    val h12 = (hour % 12).let { if (it == 0) 12 else it }
    return "${schMidDate(date)} $ampm $h12:${minute.toString().padStart(2, '0')}"
}

@Composable
fun ScheduleRegisterRoute(
    editId: Long? = null,
    viewModel: ScheduleRegisterViewModel = viewModel(key = "scheduleRegister_${editId ?: 0}") {
        ScheduleRegisterViewModel(editId, AppGraph.getScheduleDetailUseCase, AppGraph.submitScheduleUseCase)
    },
    onCancel: () -> Unit = {},
    onDone: (edited: Boolean) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { if (it is ScheduleRegisterSideEffect.Saved) onDone(it.edited) }
    }
    ScheduleRegisterScreen(
        state = state,
        onCancel = onCancel,
        onSetTitle = viewModel::setTitle,
        onSetLocation = viewModel::setLocation,
        onSetMemo = viewModel::setMemo,
        onSetCapacity = viewModel::setCapacity,
        onToggleEvent = viewModel::toggleEvent,
        onSetForm = viewModel::setForm,
        onSetStart = viewModel::setStart,
        onSetEnd = viewModel::setEnd,
        onSetDeadline = viewModel::setDeadline,
        onSave = viewModel::save,
    )
}

@Composable
fun ScheduleRegisterScreen(
    state: ScheduleRegisterUiState = ScheduleRegisterUiState(),
    onCancel: () -> Unit = {},
    onSetTitle: (String) -> Unit = {},
    onSetLocation: (String) -> Unit = {},
    onSetMemo: (String) -> Unit = {},
    onSetCapacity: (String) -> Unit = {},
    onToggleEvent: () -> Unit = {},
    onSetForm: (List<com.damoim.app.domain.model.FormQuestion>) -> Unit = {},
    onSetStart: (LocalDate, Int, Int) -> Unit = { _, _, _ -> },
    onSetEnd: (LocalDate, Int, Int) -> Unit = { _, _, _ -> },
    onSetDeadline: (LocalDate, Int, Int) -> Unit = { _, _, _ -> },
    onSave: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    var pickTarget by remember { mutableStateOf<PickTarget?>(null) }
    var formOpen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(colors.surfaceInput)) {
        Column(Modifier.fillMaxSize()) {
            // 헤더
            Row(Modifier.fillMaxWidth().background(colors.surface).windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(DamoimStrings.COMMON_CANCEL, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp), color = colors.textMuted, modifier = Modifier.noRippleClick(onCancel))
                Text(if (state.isEdit) DamoimStrings.SCHEDULE_EDIT_TITLE else DamoimStrings.SCHEDULE_REGISTER_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp), color = colors.textPrimary, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Text(DamoimStrings.SCHEDULE_SAVE, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp), color = if (state.canSave) colors.primary else colors.textDisabled, modifier = Modifier.then(if (state.canSave) Modifier.noRippleClick(onSave) else Modifier))
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))

            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Field(DamoimStrings.SCHEDULE_FIELD_TITLE) {
                    DamoimTextField(state.title, onSetTitle, placeholder = DamoimStrings.SCHEDULE_TITLE_PLACEHOLDER)
                }
                PickerRow(DamoimStrings.SCHEDULE_FIELD_START, if (state.hasStart && state.startDate != null) fmtDateTime(state.startDate, state.startHour, state.startMinute) else DamoimStrings.SCHEDULE_DATE_PLACEHOLDER, filled = state.hasStart) { pickTarget = PickTarget.START }
                PickerRow(DamoimStrings.SCHEDULE_FIELD_END, if (state.hasEnd && state.endDate != null) fmtDateTime(state.endDate, state.endHour, state.endMinute) else DamoimStrings.SCHEDULE_DATE_PLACEHOLDER, filled = state.hasEnd) { pickTarget = PickTarget.END }
                Field(DamoimStrings.SCHEDULE_FIELD_LOCATION) {
                    DamoimTextField(state.location, onSetLocation, placeholder = DamoimStrings.SCHEDULE_LOCATION_PLACEHOLDER)
                }
                Field(DamoimStrings.SCHEDULE_FIELD_MEMO) {
                    DamoimTextField(state.memo, onSetMemo, placeholder = DamoimStrings.SCHEDULE_MEMO_PLACEHOLDER, singleLine = false, modifier = Modifier.heightIn(min = 90.dp))
                }
                // 이벤트 토글
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(colors.surface).border(1.dp, colors.divider, RoundedCornerShape(16.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(DamoimStrings.SCHEDULE_MAKE_EVENT, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = colors.textPrimary)
                        Text(DamoimStrings.SCHEDULE_MAKE_EVENT_DESC, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
                    }
                    Toggle(state.isEvent, onToggleEvent)
                }
                if (state.isEvent) {
                    Field(DamoimStrings.SCHEDULE_FIELD_CAPACITY) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DamoimTextField(state.capacity, onSetCapacity, placeholder = "0", singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                            Text(DamoimStrings.SCHEDULE_CAPACITY_UNIT, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold), color = colors.textSecondary)
                        }
                    }
                    PickerRow(DamoimStrings.SCHEDULE_FIELD_DEADLINE, if (state.hasDeadline && state.deadlineDate != null) fmtDateTime(state.deadlineDate, state.deadlineHour, state.deadlineMinute) else DamoimStrings.SCHEDULE_DATE_PLACEHOLDER, filled = state.hasDeadline) { pickTarget = PickTarget.DEADLINE }
                    PickerRow(DamoimStrings.SCHEDULE_FIELD_FORM, DamoimStrings.scheduleFormSummary(state.form.size), filled = state.form.isNotEmpty()) { formOpen = true }
                }
                Spacer(Modifier.height(40.dp))
            }
        }

        // 61 날짜·시간 시트
        pickTarget?.let { target ->
            val (title, date, hour, minute, setter) = when (target) {
                PickTarget.START -> PickerArgs(DamoimStrings.PICKER_START_TITLE, state.startDate, state.startHour, state.startMinute, onSetStart)
                PickTarget.END -> PickerArgs(DamoimStrings.PICKER_END_TITLE, state.endDate, state.endHour, state.endMinute, onSetEnd)
                PickTarget.DEADLINE -> PickerArgs(DamoimStrings.PICKER_DEADLINE_TITLE, state.deadlineDate, state.deadlineHour, state.deadlineMinute, onSetDeadline)
            }
            ScheduleDateTimeSheet(
                title = title, initialDate = date, initialHour24 = hour, initialMinute = minute,
                onConfirm = { d, h, m -> setter(d, h, m); pickTarget = null },
                onDismiss = { pickTarget = null },
            )
        }
        // 46 신청 양식 편집(전체화면 오버레이)
        if (formOpen) {
            FormEditorOverlay(initialForm = state.form, onSave = { onSetForm(it); formOpen = false }, onCancel = { formOpen = false })
        }
    }
}

private data class PickerArgs(val title: String, val date: LocalDate?, val hour: Int, val minute: Int, val setter: (LocalDate, Int, Int) -> Unit)

@Composable
private fun Field(label: String, content: @Composable () -> Unit) {
    val colors = DamoimTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = colors.textSecondary)
        content()
    }
}

@Composable
private fun PickerRow(label: String, value: String, filled: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = colors.textSecondary)
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(colors.surface).border(1.5.dp, colors.outline, RoundedCornerShape(16.dp)).noRippleClick(onClick).padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(value, style = DamoimTheme.typography.body.copy(fontWeight = if (filled) FontWeight.SemiBold else FontWeight.Normal), color = if (filled) colors.textPrimary else colors.textDisabled, modifier = Modifier.weight(1f))
            ChevronRightIcon(colors.outlineStrong, Modifier.size(18.dp))
        }
    }
}

@Composable
private fun Toggle(on: Boolean, onToggle: () -> Unit) {
    val colors = DamoimTheme.colors
    Box(Modifier.width(44.dp).height(26.dp).clip(RoundedCornerShape(99.dp)).background(if (on) colors.primary else colors.outline).noRippleClick(onToggle).padding(3.dp), contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart) {
        Box(Modifier.size(20.dp).clip(CircleShape).background(colors.surface))
    }
}
