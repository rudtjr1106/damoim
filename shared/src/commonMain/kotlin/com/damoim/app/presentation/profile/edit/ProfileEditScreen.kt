package com.damoim.app.presentation.profile.edit

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.usecase.UpdateProfileUseCase
import com.damoim.app.presentation.board.InitialAvatar
import com.damoim.app.presentation.component.CameraIcon
import com.damoim.app.presentation.component.DamoimTextField
import com.damoim.app.presentation.component.KakaoBubbleIcon
import com.damoim.app.presentation.component.PhoneNumberVisualTransformation
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap

/** 화면 45 프로필 수정 — Route. 저장하면 홈 인사말·회원 명부·댓글 작성자에 즉시 반영된다. */
@Composable
fun ProfileEditRoute(
    viewModel: ProfileEditViewModel = viewModel(key = "profile_edit") {
        ProfileEditViewModel(AppGraph.getAuthUserUseCase, AppGraph.updateProfileUseCase)
    },
    onCancel: () -> Unit = {},
    onSaved: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var pickedPhoto by remember { mutableStateOf<ImageBitmap?>(null) }
    val scope = rememberCoroutineScope()
    val picker = rememberImagePickerLauncher(selectionMode = SelectionMode.Single, scope = scope, onResult = { arr ->
        arr.firstOrNull()?.let { pickedPhoto = it.toImageBitmap() }
    })
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { if (it is ProfileEditSideEffect.Saved) onSaved() }
    }
    ProfileEditScreen(
        state, pickedPhoto,
        onCancel = { viewModel.onCancel(); onCancel() },
        onNameChange = viewModel::onNameChange,
        onContactChange = viewModel::onContactChange,
        onBioChange = viewModel::onBioChange,
        onSave = viewModel::onSave,
        onPickPhoto = { picker.launch() },
    )
}

@Composable
fun ProfileEditScreen(
    state: ProfileEditUiState = ProfileEditUiState("이서연", "01012345678", "seoyeon@kakao.com"),
    pickedPhoto: ImageBitmap? = null,
    onCancel: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onContactChange: (String) -> Unit = {},
    onBioChange: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onPickPhoto: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surface).safeDrawingPadding().verticalScroll(rememberScrollState())) {
        // 헤더: 취소 / 제목 / 저장
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(DamoimStrings.COMMON_CANCEL, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp), color = colors.textMuted, modifier = Modifier.noRippleClick(onCancel))
            Text(DamoimStrings.PROFILE_EDIT_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontSize = 16.sp), color = colors.textPrimary, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Text(DamoimStrings.PROFILE_EDIT_SAVE, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp), color = if (state.canSave) colors.primary else colors.outlineStrong, modifier = Modifier.noRippleClick { if (state.canSave) onSave() })
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))

        // 아바타 + 카메라 배지 (28sp, 배지 원 밖 2dp)
        Box(Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 20.dp), contentAlignment = Alignment.Center) {
            Box {
                if (pickedPhoto != null) {
                    Image(pickedPhoto, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(96.dp).clip(CircleShape))
                } else {
                    InitialAvatar(state.name.takeLast(2).ifBlank { DamoimStrings.PROFILE_AVATAR_FALLBACK }, size = 96.dp, fontSize = 28.sp)
                }
                Box(
                    Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp).size(32.dp).clip(CircleShape).background(colors.primary).border(2.5.dp, colors.surface, CircleShape).noRippleClick(onPickPhoto),
                    contentAlignment = Alignment.Center,
                ) { CameraIcon(colors.onPrimary, Modifier.size(16.dp)) }
            }
        }

        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 0.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            // 이름 — 카운터는 입력창 안 trailing
            Field(DamoimStrings.PROFILE_NICKNAME_LABEL) {
                DamoimTextField(
                    state.name, onNameChange, placeholder = DamoimStrings.PROFILE_NICKNAME_PLACEHOLDER,
                    cornerRadius = 14.dp,
                    trailing = { Text(DamoimStrings.charCounter(state.name.length, UpdateProfileUseCase.MAX_NICKNAME_LENGTH), style = DamoimTheme.typography.label.copy(fontSize = 11.sp), color = colors.textDisabled) },
                )
            }
            // 연락처 + 공개 안내
            Field(DamoimStrings.PROFILE_CONTACT_LABEL, helper = DamoimStrings.PROFILE_CONTACT_HELPER) {
                DamoimTextField(
                    state.contact, onContactChange, placeholder = DamoimStrings.PROFILE_CONTACT_PLACEHOLDER,
                    cornerRadius = 14.dp,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    visualTransformation = PhoneNumberVisualTransformation,
                )
            }
            // 한 줄 소개
            Field(DamoimStrings.PROFILE_BIO_LABEL) {
                DamoimTextField(state.bio, onBioChange, placeholder = DamoimStrings.PROFILE_BIO_PLACEHOLDER, cornerRadius = 14.dp)
            }
            // 이메일 — 카카오 연동값(수정 불가) + 칩
            Field(DamoimStrings.PROFILE_INFO_EMAIL, helper = DamoimStrings.PROFILE_EMAIL_LOCKED) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.surfaceInput).padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(state.email, style = DamoimTheme.typography.body, color = colors.textMuted, modifier = Modifier.weight(1f))
                    Box(Modifier.size(20.dp).clip(RoundedCornerShape(6.dp)).background(colors.kakao), contentAlignment = Alignment.Center) {
                        KakaoBubbleIcon(colors.onKakao, Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun Field(label: String, helper: String? = null, content: @Composable () -> Unit) {
    val colors = DamoimTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.textSecondary)
        content()
        if (helper != null) Text(helper, style = DamoimTheme.typography.caption, color = colors.textMuted, modifier = Modifier.padding(horizontal = 4.dp))
    }
}

@Preview
@Composable
private fun ProfileEditScreenPreview() {
    DamoimTheme { ProfileEditScreen() }
}
