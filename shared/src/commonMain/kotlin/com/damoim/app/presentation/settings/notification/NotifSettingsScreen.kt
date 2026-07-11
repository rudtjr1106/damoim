package com.damoim.app.presentation.settings.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.NotifSettings
import com.damoim.app.presentation.component.ChevronRightIcon
import com.damoim.app.presentation.component.DamoimBottomSheet
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.settings.DamoimSwitch
import com.damoim.app.presentation.settings.SettingsSection
import com.damoim.app.presentation.settings.SettingsTopBar
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

@Composable
fun NotifSettingsRoute(
    isLeaderOrStaff: Boolean,
    viewModel: NotifSettingsViewModel = viewModel(key = "notifSettings") { NotifSettingsViewModel(AppGraph.notifSettingsUseCase) },
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    NotifSettingsScreen(state.settings, isLeaderOrStaff, onBack, viewModel::update)
}

private enum class PickSheet { REMINDER, DND }

@Composable
fun NotifSettingsScreen(
    s: NotifSettings = NotifSettings(),
    isLeaderOrStaff: Boolean = true,
    onBack: () -> Unit = {},
    onUpdate: (NotifSettings) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    var sheet by remember { mutableStateOf<PickSheet?>(null) }

    Box(Modifier.fillMaxSize().background(colors.surfaceInput)) {
        Column(Modifier.fillMaxSize()) {
            SettingsTopBar(DamoimStrings.NOTIF_TITLE, onBack)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingsSection(DamoimStrings.NOTIF_SEC_ACTIVITY) {
                    ToggleRow(DamoimStrings.NOTIF_PUSH, DamoimStrings.NOTIF_PUSH_SUB, s.push) { onUpdate(s.copy(push = !s.push)) }
                    ToggleRow(DamoimStrings.NOTIF_NEW_POST, null, s.newPost) { onUpdate(s.copy(newPost = !s.newPost)) }
                    ToggleRow(DamoimStrings.NOTIF_COMMENT, null, s.comment) { onUpdate(s.copy(comment = !s.comment)) }
                    ToggleRow(DamoimStrings.NOTIF_SCHEDULE, null, s.scheduleReminder) { onUpdate(s.copy(scheduleReminder = !s.scheduleReminder)) }
                    ValueRow(DamoimStrings.NOTIF_REMINDER_TIMING, s.reminderLabel, showDivider = false) { sheet = PickSheet.REMINDER }
                }
                if (isLeaderOrStaff) {
                    SettingsSection(DamoimStrings.NOTIF_SEC_ADMIN) {
                        ToggleRow(DamoimStrings.NOTIF_JOIN, null, s.joinRequest) { onUpdate(s.copy(joinRequest = !s.joinRequest)) }
                        ToggleRow(DamoimStrings.NOTIF_EVENT, null, s.eventApply, showDivider = false) { onUpdate(s.copy(eventApply = !s.eventApply)) }
                    }
                }
                SettingsSection(DamoimStrings.NOTIF_SEC_DND) {
                    ToggleRow(DamoimStrings.NOTIF_DND, DamoimStrings.NOTIF_DND_SUB, s.dndEnabled, showDivider = s.dndEnabled) { onUpdate(s.copy(dndEnabled = !s.dndEnabled)) }
                    if (s.dndEnabled) ValueRow(DamoimStrings.NOTIF_DND, s.dndRangeLabel, showDivider = false) { sheet = PickSheet.DND }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        when (sheet) {
            PickSheet.REMINDER -> OptionSheet(DamoimStrings.NOTIF_REMINDER_SHEET_TITLE, DamoimStrings.NOTIF_REMINDER_OPTIONS, s.reminderLabel, onSelect = { onUpdate(s.copy(reminderLabel = it)); sheet = null }, onDismiss = { sheet = null })
            PickSheet.DND -> OptionSheet(DamoimStrings.NOTIF_DND_SHEET_TITLE, DamoimStrings.NOTIF_DND_OPTIONS, s.dndRangeLabel, onSelect = { onUpdate(s.copy(dndRangeLabel = it)); sheet = null }, onDismiss = { sheet = null })
            null -> {}
        }
    }
}

@Composable
private fun ToggleRow(label: String, sub: String?, on: Boolean, showDivider: Boolean = true, onToggle: () -> Unit) {
    val colors = DamoimTheme.colors
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = colors.textPrimary)
                if (sub != null) Text(sub, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Normal, fontSize = 11.5.sp), color = colors.textMuted)
            }
            DamoimSwitch(on, onToggle)
        }
        if (showDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
    }
}

@Composable
private fun ValueRow(label: String, value: String, showDivider: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Column {
        Row(Modifier.fillMaxWidth().noRippleClick(onClick).padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
            Text(value, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold), color = colors.textMuted)
            ChevronRightIcon(colors.outlineStrong, Modifier.size(16.dp))
        }
        if (showDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
    }
}

@Composable
private fun OptionSheet(title: String, options: List<String>, selected: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    val colors = DamoimTheme.colors
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 44.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = colors.textPrimary, modifier = Modifier.padding(start = 6.dp, bottom = 6.dp))
            options.forEach { opt ->
                val on = opt == selected
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (on) colors.primaryContainer else colors.surface).noRippleClick { onSelect(opt) }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(opt, style = DamoimTheme.typography.bodySmall.copy(fontWeight = if (on) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp), color = if (on) colors.primaryDark else colors.textPrimary, modifier = Modifier.weight(1f))
                    if (on) Box(Modifier.size(8.dp).clip(RoundedCornerShape(99.dp)).background(colors.primary))
                }
            }
        }
    }
}
