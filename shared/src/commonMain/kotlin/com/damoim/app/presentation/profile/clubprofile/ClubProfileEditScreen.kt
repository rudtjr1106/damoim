package com.damoim.app.presentation.profile.clubprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.presentation.component.DamoimTextField
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.settings.SettingsTopBar
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

@Composable
fun ClubProfileEditRoute(
    viewModel: ClubProfileEditViewModel = viewModel(key = "club_profile_edit") {
        ClubProfileEditViewModel(AppGraph.getMyMemberUseCase, AppGraph.updateClubProfileUseCase)
    },
    onBack: () -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                ClubProfileEditSideEffect.Saved -> { onToast(DamoimStrings.TOAST_CLUB_PROFILE_SAVED); onBack() }
                is ClubProfileEditSideEffect.Failed -> onToast(effect.message)
            }
        }
    }
    ClubProfileEditScreen(state, onBack, viewModel::onNameChange, viewModel::onSave)
}

@Composable
fun ClubProfileEditScreen(
    state: ClubProfileEditUiState = ClubProfileEditUiState(displayName = "조경석", loaded = true),
    onBack: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onSave: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surface)) {
        SettingsTopBar(DamoimStrings.CLUB_PROFILE_EDIT_TITLE, onBack)
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(DamoimStrings.CLUB_PROFILE_EDIT_DESC, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, lineHeight = 19.sp), color = colors.textMuted)
            Spacer(Modifier.height(4.dp))
            Text(DamoimStrings.CLUB_PROFILE_NAME_LABEL, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = colors.textSecondary)
            DamoimTextField(
                value = state.displayName,
                onValueChange = onNameChange,
                placeholder = DamoimStrings.CLUB_PROFILE_NAME_PLACEHOLDER,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(Modifier.weight(1f))
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 24.dp)) {
            val enabled = state.canSave
            androidx.compose.foundation.layout.Box(
                Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp))
                    .background(if (enabled) colors.primary else colors.outline)
                    .then(if (enabled) Modifier.noRippleClick(onSave) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                Text(DamoimStrings.COMMON_SAVE, style = DamoimTheme.typography.button, color = colors.onPrimary)
            }
        }
    }
}
