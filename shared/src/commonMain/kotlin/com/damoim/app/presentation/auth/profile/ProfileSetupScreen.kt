package com.damoim.app.presentation.auth.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.usecase.UpdateProfileUseCase
import com.damoim.app.presentation.component.CameraIcon
import com.damoim.app.presentation.component.DamoimTextField
import com.damoim.app.presentation.component.InitialAvatar
import com.damoim.app.presentation.component.PrimaryButton
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 31 프로필 설정 — Route(상태·이벤트·네비게이션).
 */
@Composable
fun ProfileSetupRoute(
    viewModel: ProfileSetupViewModel = viewModel { ProfileSetupViewModel(AppGraph.updateProfileUseCase) },
    onNavigateStart: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                ProfileSetupSideEffect.NavigateToStart -> onNavigateStart()
            }
        }
    }

    ProfileSetupScreen(
        state = state,
        onNicknameChange = viewModel::onNicknameChange,
        onContactChange = viewModel::onContactChange,
        onSubmit = viewModel::onSubmit,
        // 실제 사진 선택은 플랫폼 이미지 피커(expect/actual) 연동 시 구현. 지금은 UI만.
        onPickPhoto = {},
    )
}

/**
 * 화면 31 프로필 설정 — Screen(무상태 UI). 프로필 사진 + 이름(카운터) + 연락처 + 완료.
 */
@Composable
fun ProfileSetupScreen(
    state: ProfileSetupUiState = ProfileSetupUiState(),
    onNicknameChange: (String) -> Unit = {},
    onContactChange: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onPickPhoto: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(40.dp))
        Text(DamoimStrings.PROFILE_TITLE, style = DamoimTheme.typography.headline, color = colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text(DamoimStrings.PROFILE_SUBTITLE, style = DamoimTheme.typography.body, color = colors.textMuted)

        // 프로필 사진 (아바타 + 카메라 편집 배지)
        Spacer(Modifier.height(28.dp))
        ProfilePhoto(
            initial = state.nickname.ifBlank { DamoimStrings.PROFILE_AVATAR_FALLBACK },
            onPickPhoto = onPickPhoto,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        // 이름 (실명 권장) * + 글자수 카운터
        Spacer(Modifier.height(28.dp))
        FieldLabel(
            label = DamoimStrings.PROFILE_NICKNAME_LABEL,
            required = true,
            counter = DamoimStrings.charCounter(state.nickname.length, UpdateProfileUseCase.MAX_NICKNAME_LENGTH),
        )
        Spacer(Modifier.height(8.dp))
        DamoimTextField(
            value = state.nickname,
            onValueChange = onNicknameChange,
            placeholder = DamoimStrings.PROFILE_NICKNAME_PLACEHOLDER,
            isError = state.errorMessage != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        // 연락처 *
        Spacer(Modifier.height(20.dp))
        FieldLabel(label = DamoimStrings.PROFILE_CONTACT_LABEL, required = true)
        Spacer(Modifier.height(8.dp))
        DamoimTextField(
            value = state.contact,
            onValueChange = onContactChange,
            placeholder = DamoimStrings.PROFILE_CONTACT_PLACEHOLDER,
            isError = state.errorMessage != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        )

        state.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = DamoimTheme.typography.caption, color = colors.error)
        }

        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = DamoimStrings.COMMON_DONE,
            onClick = onSubmit,
            enabled = state.canSubmit,
            loading = state.isSaving,
        )
        Spacer(Modifier.height(24.dp))
    }
}

/** 입력 라벨 행: 라벨 + 필수(*) + 우측 글자수 카운터(선택). */
@Composable
private fun FieldLabel(
    label: String,
    required: Boolean = false,
    counter: String? = null,
) {
    val colors = DamoimTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = DamoimTheme.typography.bodySmall, color = colors.textSecondary)
        if (required) {
            Text(" ${DamoimStrings.REQUIRED_MARK}", style = DamoimTheme.typography.bodySmall, color = colors.primary)
        }
        Spacer(Modifier.weight(1f))
        if (counter != null) {
            Text(counter, style = DamoimTheme.typography.caption, color = colors.textDisabled)
        }
    }
}

/** 프로필 사진: 이니셜 아바타 위에 카메라 편집 배지를 겹친 형태. */
@Composable
private fun ProfilePhoto(
    initial: String,
    onPickPhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DamoimTheme.colors
    Box(modifier = modifier.size(96.dp)) {
        InitialAvatar(initial = initial, modifier = Modifier.size(96.dp))
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(30.dp)
                .clip(CircleShape)
                .background(colors.primary)
                .border(2.dp, colors.surface, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onPickPhoto,
                ),
            contentAlignment = Alignment.Center,
        ) {
            CameraIcon(tint = colors.onPrimary, modifier = Modifier.size(16.dp))
        }
    }
}

@Preview
@Composable
private fun ProfileSetupScreenPreview() {
    DamoimTheme { ProfileSetupScreen() }
}
