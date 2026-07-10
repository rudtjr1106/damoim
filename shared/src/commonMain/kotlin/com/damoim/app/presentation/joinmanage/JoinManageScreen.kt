package com.damoim.app.presentation.joinmanage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.JoinApplicant
import com.damoim.app.presentation.component.TitleTopBar
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 화면 09 가입 신청 관리 — Route. */
@Composable
fun JoinManageRoute(
    viewModel: JoinManageViewModel = viewModel { JoinManageViewModel(AppGraph.getJoinApplicantsUseCase, AppGraph.decideApplicantUseCase) },
    onBack: () -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { if (it is JoinManageSideEffect.Toast) onToast(it.message) }
    }
    JoinManageScreen(state = state, onBack = onBack, onDecide = viewModel::onDecide, onSelectTab = viewModel::onSelectTab)
}

@Composable
fun JoinManageScreen(
    state: JoinManageUiState = JoinManageUiState(isLoading = false, pending = previewApplicants()),
    onBack: () -> Unit = {},
    onDecide: (JoinApplicant, Boolean) -> Unit = { _, _ -> },
    onSelectTab: (JoinManageTab) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surfaceInput).safeDrawingPadding()) {
        // 헤더 + 탭 (흰 배경)
        Column(Modifier.fillMaxWidth().background(colors.surface)) {
            TitleTopBar(DamoimStrings.JOINMANAGE_TITLE, onBack)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Tab(DamoimStrings.joinTabPending(state.pending.size), selected = state.tab == JoinManageTab.PENDING) { onSelectTab(JoinManageTab.PENDING) }
                Tab(DamoimStrings.joinTabDone(state.processed.size), selected = state.tab == JoinManageTab.DONE) { onSelectTab(JoinManageTab.DONE) }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
        }

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (state.tab) {
                JoinManageTab.PENDING -> state.pending.forEach { ApplicantCard(it, onDecide) }
                JoinManageTab.DONE -> state.processed.forEach { ProcessedCard(it) }
            }
        }
    }
}

@Composable
private fun Tab(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Text(
        text,
        style = DamoimTheme.typography.bodySmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold),
        color = if (selected) colors.onPrimary else colors.textTertiary,
        modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(if (selected) colors.textPrimary else colors.surfaceVariant)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

/** 처리 완료 카드 — 대기 카드와 같은 레이아웃에 승인됨/거절됨 뱃지. */
@Composable
private fun ProcessedCard(item: com.damoim.app.domain.model.ProcessedApplicant) {
    val colors = DamoimTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(colors.surface).padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(46.dp).clip(CircleShape).background(colors.primaryContainerHigh), contentAlignment = Alignment.Center) {
            Text(item.applicant.initial, style = DamoimTheme.typography.titleMedium.copy(fontSize = 15.sp), color = colors.primaryDeep)
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.applicant.name, style = DamoimTheme.typography.bodyStrong, color = colors.textPrimary)
            Spacer(Modifier.height(2.dp))
            Text(DamoimStrings.applicantMeta(item.applicant.desiredGisu, item.decidedLabel), style = DamoimTheme.typography.caption, color = colors.textMuted)
        }
        Text(
            if (item.approved) DamoimStrings.JOINMANAGE_APPROVED else DamoimStrings.JOINMANAGE_REJECTED,
            style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold),
            color = if (item.approved) colors.primaryDark else colors.error,
            modifier = Modifier.clip(RoundedCornerShape(999.dp))
                .background(if (item.approved) colors.primaryContainer else colors.errorContainer)
                .padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun ApplicantCard(applicant: JoinApplicant, onDecide: (JoinApplicant, Boolean) -> Unit) {
    val colors = DamoimTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(colors.surface).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(46.dp).clip(CircleShape).background(colors.primaryContainerHigh), contentAlignment = Alignment.Center) {
                Text(applicant.initial, style = DamoimTheme.typography.titleMedium.copy(fontSize = 15.sp), color = colors.primaryDeep)
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(applicant.name, style = DamoimTheme.typography.bodyStrong, color = colors.textPrimary)
                Spacer(Modifier.height(2.dp))
                Text(DamoimStrings.applicantMeta(applicant.desiredGisu, applicant.appliedDate), style = DamoimTheme.typography.caption, color = colors.textMuted)
            }
            Text(applicant.timeAgo, style = DamoimTheme.typography.label, color = colors.textDisabled)
        }
        applicant.message?.let {
            Text(
                it, style = DamoimTheme.typography.bodySmall, color = colors.textTertiary,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceInput).padding(horizontal = 14.dp, vertical = 12.dp),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DecideButton(DamoimStrings.JOINMANAGE_APPROVE, colors.primary, colors.onPrimary, Modifier.weight(1f)) { onDecide(applicant, true) }
            DecideButton(DamoimStrings.JOINMANAGE_REJECT, colors.surfaceVariant, colors.textTertiary, Modifier.weight(1f)) { onDecide(applicant, false) }
        }
    }
}

@Composable
private fun DecideButton(text: String, bg: Color, textColor: Color, modifier: Modifier, onClick: () -> Unit) {
    Text(
        text,
        style = DamoimTheme.typography.bodyStrong,
        color = textColor,
        textAlign = TextAlign.Center,
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(bg)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 13.dp),
    )
}

internal fun previewApplicants() = listOf(
    JoinApplicant(1, "김준호", "준호", "25기 희망", "6.03 신청", "방금 전", "\"백엔드 공부하고 있는 신입생입니다. 열심히 활동하겠습니다!\""),
    JoinApplicant(2, "박지우", "지우", "25기 희망", "6.02 신청", "1일 전"),
)

@Preview
@Composable
private fun JoinManageScreenPreview() {
    DamoimTheme { JoinManageScreen() }
}
