package com.damoim.app.presentation.settings.blocked

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
import androidx.compose.runtime.LaunchedEffect
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
import com.damoim.app.domain.model.BlockedUser
import com.damoim.app.presentation.component.NetworkAvatar
import com.damoim.app.presentation.component.UserSingleIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.settings.SettingsTopBar
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

@Composable
fun BlockedRoute(
    viewModel: BlockedViewModel = viewModel(key = "blocked") { BlockedViewModel(AppGraph.blockedUserUseCase) },
    onBack: () -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.sideEffect.collect { if (it is BlockedSideEffect.Toast) onToast(it.message) } }
    BlockedScreen(state, onBack, viewModel::unblock)
}

@Composable
fun BlockedScreen(
    state: BlockedUiState = BlockedUiState(isLoading = false),
    onBack: () -> Unit = {},
    onUnblock: (Long) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surface)) {
        SettingsTopBar(DamoimStrings.BLOCKED_TITLE, onBack)
        if (state.isEmpty) {
            Column(Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(DamoimStrings.BLOCKED_EMPTY_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                Spacer(Modifier.height(6.dp))
                Text(DamoimStrings.BLOCKED_EMPTY_SUB, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, textAlign = TextAlign.Center)
            }
        } else {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(DamoimStrings.BLOCKED_DESC, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp), color = colors.textMuted)
                Text(DamoimStrings.blockedCount(state.users.size), style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp), color = colors.textMuted)
                state.users.forEach { u -> BlockedCard(u) { onUnblock(u.id) } }
            }
        }
    }
}

@Composable
private fun BlockedCard(u: BlockedUser, onUnblock: () -> Unit) {
    val colors = DamoimTheme.colors
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, colors.divider, RoundedCornerShape(16.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        NetworkAvatar(url = u.imageUrl, size = 42.dp) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(colors.surfaceDim), contentAlignment = Alignment.Center) {
                if (u.isWithdrawn) UserSingleIcon(colors.textMuted, Modifier.size(20.dp))
                else Text(u.initials, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 12.sp), color = colors.textMuted)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(u.name, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = colors.textPrimary)
            Text(u.blockedLabel, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Normal, fontSize = 11.5.sp), color = colors.textDisabled)
        }
        Box(Modifier.clip(RoundedCornerShape(999.dp)).background(colors.surfaceVariant).noRippleClick(onUnblock).padding(horizontal = 14.dp, vertical = 8.dp)) {
            Text(DamoimStrings.BLOCKED_UNBLOCK, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = colors.textTertiary)
        }
    }
}
