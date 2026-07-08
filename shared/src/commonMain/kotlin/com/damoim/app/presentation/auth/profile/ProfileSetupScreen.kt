package com.damoim.app.presentation.auth.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.presentation.component.InitialAvatar
import com.damoim.app.presentation.component.PrimaryButton
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 31 프로필 설정(가입 직후). 아바타 + 닉네임 입력 + 완료 CTA.
 */
@Composable
fun ProfileSetupScreen(
    onNavigateStart: () -> Unit,
    viewModel: ProfileSetupViewModel = viewModel { ProfileSetupViewModel(AppGraph.updateProfileUseCase) },
) {
    val state by viewModel.uiState.collectAsState()
    val colors = DamoimTheme.colors

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                ProfileSetupSideEffect.NavigateToStart -> onNavigateStart()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(40.dp))
        Text("거의 다 왔어요!\n프로필을 설정해주세요", style = DamoimTheme.typography.headline, color = colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text("동아리에서 사용할 이름이에요", style = DamoimTheme.typography.body, color = colors.textMuted)

        Spacer(Modifier.height(32.dp))
        InitialAvatar(
            initial = state.nickname.ifBlank { "다" },
            modifier = Modifier.size(96.dp).align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.height(32.dp))
        // 닉네임 입력 필드
        Text("이름", style = DamoimTheme.typography.bodySmall, color = colors.textSecondary)
        Spacer(Modifier.height(8.dp))
        NicknameField(
            value = state.nickname,
            onValueChange = viewModel::onNicknameChange,
            onSubmit = viewModel::onSubmit,
        )
        state.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = DamoimTheme.typography.caption, color = colors.error)
        }

        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = "완료",
            onClick = viewModel::onSubmit,
            enabled = state.canSubmit,
            loading = state.isSaving,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun NicknameField(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val colors = DamoimTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceInput)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = DamoimTheme.typography.titleMedium.copy(color = colors.textPrimary),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.primary),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { onSubmit() }),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text("이름을 입력해주세요", style = DamoimTheme.typography.titleMedium, color = colors.textDisabled)
                }
                inner()
            },
        )
    }
}
