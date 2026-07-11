package com.damoim.app.presentation.member.cohort

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.Cohort
import com.damoim.app.platform.PlatformBackHandler
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.EditIcon
import com.damoim.app.presentation.component.InfoIcon
import com.damoim.app.presentation.component.PlusIcon
import com.damoim.app.presentation.component.TitleTopBar
import com.damoim.app.presentation.component.dashedBorder
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.member.list.previewCohorts
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

private sealed interface CohortOverlay {
    data object Add : CohortOverlay
    data class Rename(val cohort: Cohort) : CohortOverlay
}

/** 화면 19 기수 관리 — Route. */
@Composable
fun CohortManageRoute(
    viewModel: CohortManageViewModel = viewModel(key = "cohort_manage") {
        CohortManageViewModel(AppGraph.getCohortsUseCase, AppGraph.cohortActionUseCase)
    },
    onBack: () -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { if (it is CohortManageSideEffect.Toast) onToast(it.message) }
    }
    CohortManageScreen(state, onBack, viewModel::onAdd, viewModel::onRename)
}

@Composable
fun CohortManageScreen(
    state: CohortManageUiState = CohortManageUiState(isLoading = false, cohorts = previewCohorts()),
    onBack: () -> Unit = {},
    onAdd: (String, String) -> Unit = { _, _ -> },
    onRename: (Long, String, String) -> Unit = { _, _, _ -> },
) {
    val colors = DamoimTheme.colors
    var overlay by remember { mutableStateOf<CohortOverlay?>(null) }
    PlatformBackHandler(enabled = overlay != null) { overlay = null }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(colors.surfaceInput).safeDrawingPadding()) {
            Column(Modifier.fillMaxWidth().background(colors.surface)) {
                TitleTopBar(DamoimStrings.COHORT_MANAGE_TITLE, onBack)
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
            }
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AddTile { overlay = CohortOverlay.Add }
                state.cohorts.forEachIndexed { i, c ->
                    CohortRow(c, recruiting = i == 0, onEdit = { overlay = CohortOverlay.Rename(c) })
                }
                InfoBanner()
            }
        }

        when (val o = overlay) {
            CohortOverlay.Add -> AddCohortSheet(onDismiss = { overlay = null }, onConfirm = { s, n -> overlay = null; onAdd(s, n) })
            is CohortOverlay.Rename -> RenameCohortSheet(o.cohort.short, o.cohort.label, onDismiss = { overlay = null }, onConfirm = { s, n -> overlay = null; onRename(o.cohort.id, s, n) })
            null -> Unit
        }
    }
}

@Composable
private fun AddTile(onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(colors.surface)
            .dashedBorder(colors.accentSkySoft, 1.5.dp, 18.dp).noRippleClick(onClick).padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlusIcon(colors.primary, Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(DamoimStrings.COHORT_ADD, style = DamoimTheme.typography.bodyStrong, color = colors.primary)
    }
}

@Composable
private fun CohortRow(cohort: Cohort, recruiting: Boolean, onEdit: () -> Unit) {
    val colors = DamoimTheme.colors
    Row(
        Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(colors.surface).padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(if (recruiting) colors.primary else colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(cohort.short, style = DamoimTheme.typography.bodyStrong.copy(fontWeight = FontWeight.ExtraBold), color = if (recruiting) colors.onPrimary else colors.primaryDeep)
        }
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(cohort.label.substringBefore(" (").trim(), style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = colors.textPrimary)
                if (recruiting) Text(DamoimStrings.COHORT_RECRUITING, style = DamoimTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold), color = colors.onPrimary, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.primary).padding(horizontal = 8.dp, vertical = 2.dp))
            }
            Spacer(Modifier.height(2.dp))
            Text(DamoimStrings.memberCountLabel(cohort.memberCount), style = DamoimTheme.typography.caption, color = colors.textMuted)
        }
        Box(Modifier.size(24.dp).noRippleClick(onEdit), contentAlignment = Alignment.Center) {
            EditIcon(colors.textMuted, Modifier.size(18.dp))
        }
    }
}

@Composable
private fun InfoBanner() {
    val colors = DamoimTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.primaryContainer).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        InfoIcon(colors.primary, Modifier.size(18.dp))
        Text(DamoimStrings.COHORT_INFO, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, lineHeight = 20.sp), color = colors.primaryDeep)
    }
}


@Preview
@Composable
private fun CohortManageScreenPreview() {
    DamoimTheme { CohortManageScreen() }
}
