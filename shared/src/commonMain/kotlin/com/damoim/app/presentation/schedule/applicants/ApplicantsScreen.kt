package com.damoim.app.presentation.schedule.applicants

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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.ApplicantStatus
import com.damoim.app.domain.model.EventApplicant
import com.damoim.app.domain.model.EventStatus
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.DamoimBottomSheet
import com.damoim.app.presentation.component.NetworkAvatar
import com.damoim.app.presentation.component.SheetCloseButton
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.schedule.ddayText
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme
import com.damoim.app.platform.rememberShareText

@Composable
fun ApplicantsRoute(
    scheduleId: Long,
    viewModel: ApplicantsViewModel = viewModel(key = "applicants_$scheduleId") {
        ApplicantsViewModel(scheduleId, AppGraph.getScheduleDetailUseCase, AppGraph.scheduleActionUseCase)
    },
    onBack: () -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val share = rememberShareText()
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { if (it is ApplicantsSideEffect.Toast) onToast(it.message) }
    }
    ApplicantsScreen(state = state, onBack = onBack, onCloseEarly = viewModel::closeEarly, onExport = { text -> share(text); onToast(DamoimStrings.SCHEDULE_MY_APPS_ACTION) })
}

@Composable
fun ApplicantsScreen(
    state: ApplicantsUiState = ApplicantsUiState(isLoading = false),
    onBack: () -> Unit = {},
    onCloseEarly: () -> Unit = {},
    onExport: (String) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    val schedule = state.schedule ?: return
    val event = schedule.event ?: return
    var selected by remember { mutableStateOf<EventApplicant?>(null) }

    fun exportText(): String = buildString {
        appendLine("${schedule.title} ${DamoimStrings.APPLICANTS_TITLE}")
        event.activeApplicants.forEachIndexed { i, a ->
            appendLine("${i + 1}. ${a.name} · ${a.answers.joinToString(", ") { it.answer }}")
        }
    }.trim()

    Box(Modifier.fillMaxSize().background(colors.surfaceInput)) {
        Column(Modifier.fillMaxSize()) {
            // 헤더
            Row(Modifier.fillMaxWidth().background(colors.surface).windowInsetsPadding(WindowInsets.statusBars).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(24.dp).noRippleClick(onBack), contentAlignment = Alignment.Center) { BackChevronIcon(colors.textPrimary, Modifier.size(24.dp)) }
                Spacer(Modifier.size(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(DamoimStrings.APPLICANTS_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp), color = colors.textPrimary)
                    Text(schedule.title, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
                }
                Text(DamoimStrings.APPLICANTS_EXPORT, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = colors.primary, modifier = Modifier.noRippleClick { onExport(exportText()) })
            }
            // 통계
            Row(Modifier.fillMaxWidth().background(colors.surface).padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat("${event.appliedCount}", DamoimStrings.APPLICANTS_STAT_APPLIED, colors.primaryContainer, colors.primaryDeep, colors.textTertiary, Modifier.weight(1f))
                Stat("${event.remaining}", DamoimStrings.APPLICANTS_STAT_REMAINING, colors.primaryContainer, colors.primaryDeep, colors.textTertiary, Modifier.weight(1f))
                Stat(if (event.status == EventStatus.OPEN) ddayText(event.deadlineDate, state.today) else DamoimStrings.EVENT_CLOSED_BADGE, DamoimStrings.APPLICANTS_STAT_DEADLINE, colors.warnContainer, colors.warn, colors.onWarnContainer, Modifier.weight(1f))
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
            // 리스트
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                event.applicants.forEach { a -> ApplicantCard(a) { if (a.status == ApplicantStatus.APPLIED) selected = a } }
            }
            // 조기 마감 CTA
            if (event.status == EventStatus.OPEN) {
                Box(Modifier.fillMaxWidth().background(colors.surface).padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp)) {
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).border(1.5.dp, colors.error, RoundedCornerShape(14.dp)).noRippleClick(onCloseEarly).padding(vertical = 15.dp), contentAlignment = Alignment.Center) {
                        Text(DamoimStrings.APPLICANTS_CLOSE_CTA, style = DamoimTheme.typography.bodyStrong.copy(fontWeight = FontWeight.Bold), color = colors.error)
                    }
                }
            }
        }
        // G2 신청자 응답 상세
        selected?.let { a -> ApplicantAnswersSheet(a, onDismiss = { selected = null }) }
    }
}

@Composable
private fun Stat(value: String, label: String, bg: Color, valueColor: Color, labelColor: Color, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(bg).padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, style = DamoimTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp), color = valueColor)
        Text(label, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp), color = labelColor)
    }
}

@Composable
private fun ApplicantCard(a: EventApplicant, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    val canceled = a.status == ApplicantStatus.CANCELED
    Row(
        Modifier.fillMaxWidth().alpha(if (canceled) 0.55f else 1f).clip(RoundedCornerShape(16.dp)).background(colors.surface)
            .then(if (canceled) Modifier else Modifier.noRippleClick(onClick)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        NetworkAvatar(url = a.imageUrl, size = 42.dp) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(if (canceled) colors.surfaceDim else colors.primaryContainerHigh), contentAlignment = Alignment.Center) {
                Text(a.initials, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 12.sp), color = if (canceled) colors.textMuted else colors.primaryDeep)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(a.name, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = colors.textPrimary)
            Text(
                if (canceled) DamoimStrings.APPLICANTS_CANCELED_LABEL else a.answers.take(2).joinToString(" · ") { it.answer },
                style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted,
            )
        }
        if (canceled) {
            Text(DamoimStrings.APPLICANTS_STATUS_CANCELED, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp), color = colors.textMuted, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.surfaceDim).padding(horizontal = 9.dp, vertical = 4.dp))
        } else {
            Text(a.appliedLabel, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp), color = colors.textDisabled)
        }
    }
}

// ── G2 신청자 응답 상세(신설) ──
@Composable
private fun ApplicantAnswersSheet(a: EventApplicant, onDismiss: () -> Unit) {
    val colors = DamoimTheme.colors
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 44.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NetworkAvatar(url = a.imageUrl, size = 42.dp) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(colors.primaryContainerHigh), contentAlignment = Alignment.Center) {
                        Text(a.initials, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 12.sp), color = colors.primaryDeep)
                    }
                }
                Column {
                    Text(a.name, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 17.sp), color = colors.textPrimary)
                    Text("${a.appliedLabel} 신청", style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
                }
            }
            Text(DamoimStrings.APPLICANT_ANSWERS_TITLE, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp), color = colors.textMuted)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                a.answers.forEach { qa ->
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceInput).padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(qa.question, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = colors.textTertiary)
                        Text(qa.answer.ifBlank { "-" }, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = colors.textPrimary)
                    }
                }
            }
            SheetCloseButton(onDismiss)
        }
    }
}
