package com.damoim.app.presentation.auth.profile

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap

/**
 * 화면 31 프로필 설정 — Route(상태·이벤트·네비게이션 + 이미지 피커).
 *
 * 사진 선택은 Peekaboo 이미지 피커(Android=Photo Picker, iOS=PHPicker)를 사용해 Android/iOS 모두 동작.
 * 선택 결과(ByteArray)는 toImageBitmap()으로 변환해 화면에 표시한다. (서버 업로드는 추후: 지금은 표시만)
 */
@Composable
fun ProfileSetupRoute(
    viewModel: ProfileSetupViewModel = viewModel { ProfileSetupViewModel(AppGraph.updateProfileUseCase, AppGraph.observeMyContextUseCase) },
    onNavigateStart: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var pickedPhoto by remember { mutableStateOf<ImageBitmap?>(null) }
    val scope = rememberCoroutineScope()

    val imagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let { pickedPhoto = it.toImageBitmap() }
        },
    )

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                ProfileSetupSideEffect.NavigateToStart -> onNavigateStart()
            }
        }
    }

    ProfileSetupScreen(
        state = state,
        pickedPhoto = pickedPhoto,
        onNicknameChange = viewModel::onNicknameChange,
        onContactChange = viewModel::onContactChange,
        onSubmit = viewModel::onSubmit,
        onPickPhoto = { imagePicker.launch() },
    )
}

/**
 * 화면 31 프로필 설정 — Screen(무상태 UI). 프로필 사진 + 이름(카운터) + 연락처 + 완료.
 */
@Composable
fun ProfileSetupScreen(
    state: ProfileSetupUiState = ProfileSetupUiState(),
    pickedPhoto: ImageBitmap? = null,
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

        // 프로필 사진 (선택된 사진 or 이니셜 아바타 + 카메라 편집 배지)
        Spacer(Modifier.height(28.dp))
        ProfilePhoto(
            initial = state.nickname.ifBlank { DamoimStrings.PROFILE_AVATAR_FALLBACK },
            photo = pickedPhoto,
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
            visualTransformation = com.damoim.app.presentation.component.PhoneNumberVisualTransformation,
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

/** 프로필 사진: 선택된 사진(원형 크롭) 또는 이니셜 아바타 위에 카메라 편집 배지. */
@Composable
private fun ProfilePhoto(
    initial: String,
    photo: ImageBitmap?,
    onPickPhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DamoimTheme.colors
    // 카메라 배지뿐 아니라 원형 아바타 전체를 눌러도 사진 선택이 열린다
    Box(
        modifier = modifier.size(96.dp).clip(CircleShape).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onPickPhoto,
        ),
    ) {
        if (photo != null) {
            Image(
                bitmap = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(96.dp).clip(CircleShape),
            )
        } else {
            InitialAvatar(initial = initial, modifier = Modifier.size(96.dp))
        }
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
