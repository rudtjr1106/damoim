package com.damoim.app.presentation.schedule.myapplications

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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.ApplicationStatus
import com.damoim.app.domain.model.MyApplication
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.CalendarIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.schedule.detail.ApplyFormSheet
import com.damoim.app.presentation.schedule.detail.EventConfirmDialog
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

@Composable
fun MyApplicationsRoute(
    viewModel: MyApplicationsViewModel = viewModel(key = "myApplications") {
        MyApplicationsViewModel(AppGraph.getMyApplicationsUseCase, AppGraph.getSchedulesUseCase, AppGraph.eventApplicationUseCase)
    },
    onBack: () -> Unit = {},
    onOpenEvent: (Long) -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { if (it is MyApplicationsSideEffect.Toast) onToast(it.message) }
    }
    MyApplicationsScreen(state = state, onBack = onBack, onOpenEvent = onOpenEvent, onCancel = viewModel::cancel, onUpdateAnswers = viewModel::updateAnswers)
}

@Composable
fun MyApplicationsScreen(
    state: MyApplicationsUiState = MyApplicationsUiState(isLoading = false),
    onBack: () -> Unit = {},
    onOpenEvent: (Long) -> Unit = {},
    onCancel: (Long) -> Unit = {},
    onUpdateAnswers: (Long, List<com.damoim.app.domain.model.QuestionAnswer>) -> Unit = { _, _ -> },
) {
    val colors = DamoimTheme.colors
    var editing by remember { mutableStateOf<MyApplication?>(null) }
    var canceling by remember { mutableStateOf<MyApplication?>(null) }

    Box(Modifier.fillMaxSize().background(colors.surface)) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(24.dp).noRippleClick(onBack), contentAlignment = Alignment.Center) { BackChevronIcon(colors.textPrimary, Modifier.size(24.dp)) }
                Spacer(Modifier.size(8.dp))
                Text(DamoimStrings.MY_APPS_TITLE, style = DamoimTheme.typography.titleMedium, color = colors.textPrimary)
            }
            if (state.isEmpty) {
                EmptyState(onBack)
            } else {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.applications.forEach { app ->
                        ApplicationCard(app, onOpen = { onOpenEvent(app.eventId) }, onEdit = { editing = app }, onCancel = { canceling = app })
                    }
                }
            }
        }
        // 응답 수정(25 재사용, 편집 모드)
        editing?.let { app ->
            state.schedulesById[app.eventId]?.let { schedule ->
                ApplyFormSheet(schedule = schedule, existingAnswers = app.answers, isEdit = true, onSubmit = { answers -> onUpdateAnswers(app.eventId, answers); editing = null }, onDismiss = { editing = null })
            }
        }
        // 신청 취소 확인
        canceling?.let { app ->
            EventConfirmDialog(
                title = DamoimStrings.MY_APPS_CANCEL_TITLE, desc = DamoimStrings.MY_APPS_CANCEL_DESC,
                confirm = DamoimStrings.MY_APPS_CANCEL, destructive = true,
                onConfirm = { canceling = null; onCancel(app.eventId) }, onDismiss = { canceling = null },
            )
        }
    }
}

@Composable
private fun ApplicationCard(app: MyApplication, onOpen: () -> Unit, onEdit: () -> Unit, onCancel: () -> Unit) {
    val colors = DamoimTheme.colors
    val ended = app.status == ApplicationStatus.ENDED
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(colors.surface).border(1.dp, colors.divider, RoundedCornerShape(16.dp)).noRippleClick(onOpen).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(app.title, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = colors.textPrimary)
                Text(app.dateLabel, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
            }
            val (label, bg, fg) = if (ended) Triple(DamoimStrings.MY_APPS_STATUS_ENDED, colors.surfaceDim, colors.textMuted) else Triple(DamoimStrings.MY_APPS_STATUS_APPLIED, colors.primaryContainer, colors.primaryDark)
            Text(label, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp), color = fg, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp))
        }
        if (!ended) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(colors.surfaceVariant).noRippleClick(onEdit).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text(DamoimStrings.MY_APPS_EDIT, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = colors.textTertiary)
                }
                Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).border(1.dp, colors.divider, RoundedCornerShape(12.dp)).noRippleClick(onCancel).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text(DamoimStrings.MY_APPS_CANCEL, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = colors.error)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onBrowse: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(colors.surfaceVariant), contentAlignment = Alignment.Center) { CalendarIcon(colors.textDisabled, Modifier.size(30.dp)) }
        Spacer(Modifier.height(16.dp))
        Text(DamoimStrings.MY_APPS_EMPTY_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
        Spacer(Modifier.height(6.dp))
        Text(DamoimStrings.MY_APPS_EMPTY_SUBTITLE, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Box(Modifier.clip(RoundedCornerShape(12.dp)).background(colors.primaryContainer).noRippleClick(onBrowse).padding(horizontal = 20.dp, vertical = 12.dp)) {
            Text(DamoimStrings.MY_APPS_EMPTY_CTA, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.primary)
        }
    }
}
