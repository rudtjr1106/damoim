package com.damoim.app.presentation.clubsettings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.damoim.app.presentation.component.CameraIcon
import com.damoim.app.presentation.component.CopyIcon
import com.damoim.app.presentation.component.TitleTopBar
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 08 동아리 정보 설정 · 가입 코드 발급 — Route. 토스트는 [onToast].
 */
@Composable
fun ClubSettingsRoute(
    viewModel: ClubSettingsViewModel = viewModel { ClubSettingsViewModel(AppGraph.getClubInfoUseCase, AppGraph.regenerateJoinCodeUseCase) },
    onBack: () -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { if (it is ClubSettingsSideEffect.Toast) onToast(it.message) }
    }
    ClubSettingsScreen(
        state = state,
        onBack = onBack,
        onSave = viewModel::onSave,
        onEditLogo = viewModel::onSave,
        onOpenShare = viewModel::onOpenShare,
        onCloseShare = viewModel::onCloseShare,
        onRegenerate = viewModel::onRegenerate,
        onDisable = viewModel::onDisable,
        onKakaoShare = viewModel::onKakaoShare,
        onCopyLink = viewModel::onCopyLink,
        onCopyCode = viewModel::onCopyCode,
    )
}

@Composable
fun ClubSettingsScreen(
    state: ClubSettingsUiState = ClubSettingsUiState(isLoading = false, clubName = "코딩하는 사람들", intro = "함께 성장하는 개발 동아리.", clubInitial = "코", joinCode = "DM29AX"),
    onBack: () -> Unit = {},
    onSave: () -> Unit = {},
    onEditLogo: () -> Unit = {},
    onOpenShare: () -> Unit = {},
    onCloseShare: () -> Unit = {},
    onRegenerate: () -> Unit = {},
    onDisable: () -> Unit = {},
    onKakaoShare: () -> Unit = {},
    onCopyLink: () -> Unit = {},
    onCopyCode: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
            TitleTopBar(DamoimStrings.SETTINGS_TITLE, onBack, actionText = DamoimStrings.COMMON_SAVE, onAction = onSave)

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(16.dp))
                // 로고 (이니셜 + 편집 배지)
                Box(Modifier.size(92.dp).align(Alignment.CenterHorizontally)) {
                    Box(Modifier.size(92.dp).clip(RoundedCornerShape(30.dp)).background(colors.primary), contentAlignment = Alignment.Center) {
                        Text(state.clubInitial, style = DamoimTheme.typography.display.copy(fontSize = 34.sp), color = colors.onPrimary)
                    }
                    Box(
                        modifier = Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp).size(32.dp).clip(CircleShape).background(colors.surface)
                            .border(1.dp, colors.divider, CircleShape)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onEditLogo),
                        contentAlignment = Alignment.Center,
                    ) { CameraIcon(tint = colors.textSecondary, modifier = Modifier.size(15.dp)) }
                }

                Spacer(Modifier.height(22.dp))
                Label(DamoimStrings.SETTINGS_NAME_LABEL)
                Spacer(Modifier.height(8.dp))
                DisplayBox(state.clubName, minHeight = 0, bold = true)

                Spacer(Modifier.height(22.dp))
                Label(DamoimStrings.SETTINGS_INTRO_LABEL)
                Spacer(Modifier.height(8.dp))
                DisplayBox(state.intro, minHeight = 72, bold = false)

                Spacer(Modifier.height(22.dp))
                Label(DamoimStrings.SETTINGS_CODE_LABEL)
                Spacer(Modifier.height(8.dp))
                JoinCodeCard(state.joinCode, onOpenShare, onRegenerate, onDisable)
                Spacer(Modifier.height(8.dp))
                Text(DamoimStrings.SETTINGS_CODE_HINT, style = DamoimTheme.typography.caption, color = colors.textMuted, modifier = Modifier.padding(horizontal = 4.dp))
                Spacer(Modifier.height(24.dp))
            }
        }

        // 59 코드 공유 시트
        if (state.showShareSheet) {
            CodeShareSheet(
                code = state.joinCode,
                onKakaoShare = onKakaoShare,
                onCopyLink = onCopyLink,
                onCopyCode = onCopyCode,
                onDismiss = onCloseShare,
            )
        }
    }
}

@Composable
private fun Label(text: String) =
    Text(text, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = DamoimTheme.colors.textSecondary)

@Composable
private fun DisplayBox(text: String, minHeight: Int, bold: Boolean) {
    val colors = DamoimTheme.colors
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).border(1.dp, colors.divider, RoundedCornerShape(14.dp))
            .defaultMinSize(minHeight = minHeight.dp).padding(horizontal = 16.dp, vertical = 15.dp),
    ) {
        Text(text, style = DamoimTheme.typography.body.copy(fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal), color = colors.textPrimary)
    }
}

@Composable
private fun JoinCodeCard(code: String, onShare: () -> Unit, onRegenerate: () -> Unit, onDisable: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(colors.onDarkNavy).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(DamoimStrings.SETTINGS_CODE_CURRENT, style = DamoimTheme.typography.label, color = colors.onPrimary.copy(alpha = 0.55f))
                Spacer(Modifier.height(4.dp))
                Text(code, style = DamoimTheme.typography.headline.copy(fontSize = 26.sp, letterSpacing = 3.sp), color = colors.onPrimary)
            }
            Box(
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(colors.onPrimary.copy(alpha = 0.12f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onShare),
                contentAlignment = Alignment.Center,
            ) { CopyIcon(tint = colors.onPrimary, modifier = Modifier.size(17.dp)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CodeActionButton(DamoimStrings.SETTINGS_CODE_REGEN, colors.onPrimary, Modifier.weight(1f), onRegenerate)
            CodeActionButton(DamoimStrings.SETTINGS_CODE_DISABLE, colors.errorSoft, Modifier.weight(1f), onDisable)
        }
    }
}

@Composable
private fun CodeActionButton(text: String, textColor: Color, modifier: Modifier, onClick: () -> Unit) {
    Text(
        text,
        style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
        color = textColor,
        textAlign = TextAlign.Center,
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(DamoimTheme.colors.onPrimary.copy(alpha = 0.14f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 12.dp),
    )
}

@Preview
@Composable
private fun ClubSettingsScreenPreview() {
    DamoimTheme { ClubSettingsScreen() }
}
