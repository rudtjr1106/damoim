package com.damoim.app.presentation.schedule.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.EventStatus
import com.damoim.app.domain.model.QuestionAnswer
import com.damoim.app.domain.model.Schedule
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.CalendarIcon
import com.damoim.app.presentation.component.ClockIcon
import com.damoim.app.presentation.component.LocationIcon
import com.damoim.app.presentation.component.MoreIcon
import com.damoim.app.presentation.component.NetworkAvatar
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.schedule.ApplyFormSheet
import com.damoim.app.presentation.schedule.EventConfirmDialog
import com.damoim.app.presentation.schedule.ddayText
import com.damoim.app.presentation.schedule.schMidDate
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

@Composable
fun EventDetailRoute(
    scheduleId: Long,
    isLeader: Boolean,
    viewModel: EventDetailViewModel = viewModel(key = "eventDetail_$scheduleId") {
        EventDetailViewModel(scheduleId, AppGraph.getScheduleDetailUseCase, AppGraph.eventApplicationUseCase, AppGraph.scheduleActionUseCase)
    },
    onBack: () -> Unit = {},
    onEdit: (Long) -> Unit = {},
    onApplicants: (Long) -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var closeSheet by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { e ->
            when (e) {
                is EventDetailSideEffect.Toast -> onToast(e.message)
                EventDetailSideEffect.Deleted -> { onToast(DamoimStrings.TOAST_EVENT_CANCELED); onBack() }
                EventDetailSideEffect.Applied -> closeSheet = true
            }
        }
    }
    EventDetailScreen(
        state = state,
        isLeader = isLeader,
        closeSheetSignal = closeSheet,
        onSheetClosed = { closeSheet = false },
        onBack = onBack,
        onApply = viewModel::apply,
        onEdit = { onEdit(scheduleId) },
        onApplicants = { onApplicants(scheduleId) },
        onAnnounce = viewModel::announce,
        onCloseEarly = viewModel::closeEarly,
        onCancelEvent = viewModel::cancelEvent,
    )
}

private sealed interface DetailOverlay {
    data object Menu : DetailOverlay
    data object Apply : DetailOverlay
    data object ConfirmCancel : DetailOverlay
    data object ConfirmClose : DetailOverlay
}

@Composable
fun EventDetailScreen(
    state: EventDetailUiState = EventDetailUiState(isLoading = false),
    isLeader: Boolean = false,
    closeSheetSignal: Boolean = false,
    onSheetClosed: () -> Unit = {},
    onBack: () -> Unit = {},
    onApply: (List<QuestionAnswer>) -> Unit = {},
    onEdit: () -> Unit = {},
    onApplicants: () -> Unit = {},
    onAnnounce: () -> Unit = {},
    onCloseEarly: () -> Unit = {},
    onCancelEvent: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    val schedule = state.schedule ?: return
    val event = schedule.event
    var overlay by remember { mutableStateOf<DetailOverlay?>(null) }
    LaunchedEffect(closeSheetSignal) { if (closeSheetSignal) { overlay = null; onSheetClosed() } }

    Box(Modifier.fillMaxSize().background(colors.surface)) {
        Column(Modifier.fillMaxSize()) {
            // 히어로
            Column(
                Modifier.fillMaxWidth().background(colors.onDarkNavy)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(24.dp).noRippleClick(onBack), contentAlignment = Alignment.Center) { BackChevronIcon(colors.onPrimary, Modifier.size(24.dp)) }
                    Spacer(Modifier.weight(1f))
                    if (isLeader) Box(Modifier.size(24.dp).noRippleClick { overlay = DetailOverlay.Menu }, contentAlignment = Alignment.Center) { MoreIcon(colors.onPrimary, Modifier.size(20.dp)) }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            ddayText(schedule.date, state.today),
                            style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp),
                            color = colors.onDarkNavy,
                            modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.accentSky).padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                        if (event != null) {
                            Text(
                                when (event.status) { EventStatus.OPEN -> DamoimStrings.EVENT_OPEN_BADGE; EventStatus.CLOSED -> DamoimStrings.EVENT_CLOSED_BADGE; EventStatus.ENDED -> DamoimStrings.EVENT_ENDED_BADGE },
                                style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp),
                                color = colors.onPrimary,
                                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.onPrimary.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                    Text(schedule.title, style = DamoimTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 26.sp), color = colors.onPrimary)
                    if (event?.meta?.isNotBlank() == true) {
                        Text(event.meta, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp), color = colors.onPrimary.copy(alpha = 0.65f))
                    }
                }
            }

            // 본문(스크롤)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(colors.surfaceInput).padding(horizontal = 18.dp)) {
                    InfoRow({ CalendarIcon(colors.primary, Modifier.size(17.dp)) }, DamoimStrings.EVENT_INFO_WHEN, "${schMidDate(schedule.date)} ${schedule.timeLabel}${schedule.endLabel?.let { " $it" } ?: ""}", divider = schedule.location.isNotBlank() || event != null)
                    if (schedule.location.isNotBlank()) InfoRow({ LocationIcon(colors.primary, Modifier.size(17.dp)) }, DamoimStrings.EVENT_INFO_WHERE, schedule.location, divider = event != null)
                    if (event != null) InfoRow({ ClockIcon(colors.primary, Modifier.size(17.dp)) }, DamoimStrings.EVENT_INFO_DEADLINE, "${event.deadlineLabel}까지", divider = false)
                }
                if (event != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                            Text(DamoimStrings.EVENT_PARTICIPANTS, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 14.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
                            Text("${event.appliedCount}/${event.capacity}${DamoimStrings.SCHEDULE_CAPACITY_UNIT}", style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold, fontSize = 13.sp), color = colors.primaryDark)
                        }
                        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)).background(colors.surfaceTrack)) {
                            val frac = if (event.capacity <= 0) 0f else (event.appliedCount.toFloat() / event.capacity).coerceIn(0f, 1f)
                            Box(Modifier.fillMaxWidth(frac).height(8.dp).clip(RoundedCornerShape(99.dp)).background(colors.primary))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val shown = event.activeApplicants.take(3)
                            shown.forEachIndexed { i, a -> Box(Modifier.offset(x = (i * -8).dp)) { MiniAvatar(a.initials, imageUrl = a.imageUrl) } }
                            val extra = event.appliedCount - shown.size
                            if (extra > 0) Box(Modifier.offset(x = (shown.size * -8).dp)) { MiniAvatar("+$extra", muted = true) }
                        }
                    }
                }
                if (schedule.memo.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(DamoimStrings.EVENT_INFO_NOTICE, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 14.sp), color = colors.textPrimary)
                        Text(schedule.memo, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.Normal, lineHeight = 24.sp), color = colors.textSecondary)
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // 하단 CTA
            Box(Modifier.fillMaxWidth().background(colors.surface).padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp)) {
                BottomCta(
                    schedule = schedule,
                    onApply = { overlay = DetailOverlay.Apply },
                    onApplicants = onApplicants,
                )
            }
        }

        // 오버레이(로컬 렌더)
        when (overlay) {
            DetailOverlay.Menu -> EventMenuSheet(
                schedule = schedule,
                onEdit = { overlay = null; onEdit() },
                onApplicants = { overlay = null; onApplicants() },
                onAnnounce = { overlay = null; onAnnounce() },
                onCloseEarly = { overlay = DetailOverlay.ConfirmClose },
                onCancel = { overlay = DetailOverlay.ConfirmCancel },
                onDismiss = { overlay = null },
            )
            DetailOverlay.Apply -> if (event != null) ApplyFormSheet(schedule = schedule, existingAnswers = emptyList(), onSubmit = onApply, onDismiss = { overlay = null })
            DetailOverlay.ConfirmCancel -> EventConfirmDialog(
                title = DamoimStrings.EVENT_CANCEL_TITLE, desc = DamoimStrings.EVENT_CANCEL_DESC,
                confirm = DamoimStrings.EVENT_CANCEL_CONFIRM, destructive = true,
                onConfirm = { overlay = null; onCancelEvent() }, onDismiss = { overlay = null },
            )
            DetailOverlay.ConfirmClose -> EventConfirmDialog(
                title = DamoimStrings.EVENT_CLOSE_TITLE, desc = DamoimStrings.EVENT_CLOSE_DESC,
                confirm = DamoimStrings.EVENT_CLOSE_CONFIRM, destructive = false,
                onConfirm = { overlay = null; onCloseEarly() }, onDismiss = { overlay = null },
            )
            null -> {}
        }
    }
}

@Composable
private fun BottomCta(schedule: Schedule, onApply: () -> Unit, onApplicants: () -> Unit) {
    val colors = DamoimTheme.colors
    val event = schedule.event
    // 모집장(작성자)은 신청 버튼 대신 신청자 목록 확인 — 자기 모집글에는 신청할 수 없다.
    if (event != null && event.isMine) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.primary)
                .noRippleClick(onApplicants).padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(DamoimStrings.APPLICANTS_TITLE, style = DamoimTheme.typography.button, color = colors.onPrimary)
        }
        return
    }
    val (label, enabled) = when {
        event == null -> DamoimStrings.SCHEDULE_ADD_MY to false
        event.status == EventStatus.ENDED -> DamoimStrings.EVENT_ENDED_CTA to false
        event.appliedByMe -> DamoimStrings.EVENT_APPLIED_CTA to false
        event.status == EventStatus.CLOSED -> DamoimStrings.EVENT_CLOSED_CTA to false
        else -> DamoimStrings.EVENT_APPLY_CTA to true
    }
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(if (enabled) colors.primary else colors.surfaceDim)
            .then(if (enabled) Modifier.noRippleClick(onApply) else Modifier)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = DamoimTheme.typography.button, color = if (enabled) colors.onPrimary else colors.textDisabled)
    }
}

@Composable
private fun InfoRow(icon: @Composable () -> Unit, label: String, value: String, divider: Boolean) {
    val colors = DamoimTheme.colors
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            icon()
            Text(label, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp), color = colors.textMuted, modifier = Modifier.width(44.dp))
            Text(value, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp), color = colors.textPrimary)
        }
        if (divider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
    }
}

@Composable
internal fun MiniAvatar(text: String, muted: Boolean = false, imageUrl: String? = null) {
    val colors = DamoimTheme.colors
    Box(Modifier.size(32.dp).clip(CircleShape).background(colors.surface).padding(2.dp)) {
        NetworkAvatar(url = imageUrl, size = 28.dp) {
            Box(Modifier.fillMaxSize().clip(CircleShape).background(if (muted) colors.surfaceDim else colors.primaryContainerHigh), contentAlignment = Alignment.Center) {
                Text(text, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp), color = if (muted) colors.textMuted else colors.primaryDeep)
            }
        }
    }
}
