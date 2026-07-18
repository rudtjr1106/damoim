package com.damoim.app.presentation.settings.reports

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.MyReport
import com.damoim.app.domain.model.ReportTargetType
import com.damoim.app.presentation.component.NetworkAvatar
import com.damoim.app.presentation.settings.SettingsTopBar
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

@Composable
fun MyReportsRoute(
    viewModel: MyReportsViewModel = viewModel(key = "myReports") { MyReportsViewModel(AppGraph.myReportsUseCase) },
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    MyReportsScreen(state, onBack)
}

@Composable
fun MyReportsScreen(
    state: MyReportsUiState = MyReportsUiState(isLoading = false),
    onBack: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surface)) {
        SettingsTopBar(DamoimStrings.MY_REPORTS_TITLE, onBack)
        if (state.isEmpty) {
            Column(Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(DamoimStrings.MY_REPORTS_EMPTY_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                Spacer(Modifier.height(6.dp))
                Text(DamoimStrings.MY_REPORTS_EMPTY_SUB, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, textAlign = TextAlign.Center)
            }
        } else {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(DamoimStrings.MY_REPORTS_DESC, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp), color = colors.textMuted)
                Text(DamoimStrings.reportCount(state.reports.size), style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp), color = colors.textMuted)
                state.reports.forEach { r -> MyReportCard(r) }
            }
        }
    }
}

@Composable
private fun MyReportCard(r: MyReport) {
    val colors = DamoimTheme.colors
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, colors.divider, RoundedCornerShape(16.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        NetworkAvatar(url = r.reportedUserImageUrl, size = 42.dp) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(colors.surfaceDim), contentAlignment = Alignment.Center) {
                Text(reportInitials(r.reportedUserName), style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 12.sp), color = colors.textMuted)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(r.reportedUserName, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
                Text(r.createdLabel, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Normal, fontSize = 11.5.sp), color = colors.textDisabled)
            }
            Text(DamoimStrings.reportReasonLabel(r.reason), style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = colors.error)
            Text("${reportTargetLabel(r.targetType)} · ${r.targetPreview}", style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Normal, fontSize = 11.5.sp), color = colors.textMuted, maxLines = 1)
        }
    }
}

internal fun reportInitials(name: String): String = if (name.length >= 2) name.takeLast(2) else name

internal fun reportTargetLabel(type: ReportTargetType): String = when (type) {
    ReportTargetType.POST -> DamoimStrings.REPORT_TARGET_POST
    ReportTargetType.COMMENT -> DamoimStrings.REPORT_TARGET_COMMENT
}
