package com.damoim.app.presentation.clubcreate

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.presentation.component.CameraIcon
import com.damoim.app.presentation.component.DamoimTextField
import com.damoim.app.presentation.component.InfoIcon
import com.damoim.app.presentation.component.PrimaryButton
import com.damoim.app.presentation.component.TitleTopBar
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap

/**
 * 화면 07 동아리 생성 — Route. 생성 성공 시 [onCreated](→홈 동아리장).
 */
@Composable
fun ClubCreateRoute(
    viewModel: ClubCreateViewModel = viewModel {
        ClubCreateViewModel(AppGraph.createClubUseCase, AppGraph.uploadClubImageUseCase, AppGraph.updateClubUseCase)
    },
    onBack: () -> Unit = {},
    onCreated: () -> Unit = {},
    onError: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var logo by remember { mutableStateOf<ImageBitmap?>(null) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val picker = rememberImagePickerLauncher(SelectionMode.Single, scope) { arr ->
        arr.firstOrNull()?.let { bytes ->
            logo = bytes.toImageBitmap()          // 즉시 미리보기
            viewModel.onPhotoPicked(bytes, null)  // 생성 후 S3 업로드용 바이트 보관
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                ClubCreateSideEffect.NavigateToHome -> onCreated()
                is ClubCreateSideEffect.ShowError -> onError(effect.message)
            }
        }
    }

    ClubCreateScreen(
        state = state,
        logo = logo,
        onBack = onBack,
        onPickLogo = { picker.launch() },
        onNameChange = viewModel::onNameChange,
        onIntroChange = viewModel::onIntroChange,
        onCategorySelect = viewModel::onCategorySelect,
        onSubmit = viewModel::onSubmit,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClubCreateScreen(
    state: ClubCreateUiState = ClubCreateUiState(),
    logo: ImageBitmap? = null,
    onBack: () -> Unit = {},
    onPickLogo: () -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onIntroChange: (String) -> Unit = {},
    onCategorySelect: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(
        modifier = Modifier.fillMaxSize().background(colors.surface)
            // 키보드가 올라와도 하단 CTA가 따라 올라오지 않도록 IME 인셋은 제외한다
            .windowInsetsPadding(WindowInsets.safeDrawing.exclude(WindowInsets.ime)),
    ) {
        TitleTopBar(DamoimStrings.CREATE_TITLE, onBack)

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(16.dp))
            // 로고
            LogoUpload(logo, onPickLogo, Modifier.align(Alignment.CenterHorizontally))

            Spacer(Modifier.height(22.dp))
            FieldLabel(DamoimStrings.CREATE_NAME_LABEL, required = true)
            Spacer(Modifier.height(8.dp))
            DamoimTextField(
                value = state.name,
                onValueChange = onNameChange,
                placeholder = DamoimStrings.CREATE_NAME_PLACEHOLDER,
                textStyle = DamoimTheme.typography.body.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                cornerRadius = 14.dp,
                trailing = {
                    Text("${state.name.length}/20", style = DamoimTheme.typography.label, color = colors.textDisabled)
                },
            )

            Spacer(Modifier.height(22.dp))
            FieldLabel(DamoimStrings.CREATE_INTRO_LABEL)
            Spacer(Modifier.height(8.dp))
            DamoimTextField(
                value = state.intro,
                onValueChange = onIntroChange,
                placeholder = DamoimStrings.CREATE_INTRO_PLACEHOLDER,
                cornerRadius = 14.dp,
            )

            Spacer(Modifier.height(22.dp))
            FieldLabel(DamoimStrings.CREATE_CATEGORY_LABEL)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DamoimStrings.CREATE_CATEGORIES.forEach { cat ->
                    CategoryChip(cat, selected = cat == state.category) { onCategorySelect(cat) }
                }
            }

            Spacer(Modifier.height(22.dp))
            InfoBannerFree()
            state.errorMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, style = DamoimTheme.typography.caption, color = colors.error)
            }
            Spacer(Modifier.height(16.dp))
        }

        Column(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 48.dp)) {
            PrimaryButton(DamoimStrings.CREATE_SUBMIT, onSubmit, enabled = state.canSubmit, loading = state.isSaving)
        }
    }
}

@Composable
private fun LogoUpload(logo: ImageBitmap?, onPick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DamoimTheme.colors
    Box(
        modifier = modifier.size(92.dp).clip(RoundedCornerShape(30.dp))
            .background(colors.primaryContainer)
            .drawBehind {
                if (logo == null) drawRoundRect(
                    color = colors.accentSkySoft,
                    cornerRadius = CornerRadius(30.dp.toPx(), 30.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 7f))),
                )
            }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onPick),
        contentAlignment = Alignment.Center,
    ) {
        if (logo != null) {
            Image(bitmap = logo, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(92.dp).clip(RoundedCornerShape(30.dp)))
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CameraIcon(tint = colors.primary, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(4.dp))
                Text(DamoimStrings.CREATE_LOGO, style = DamoimTheme.typography.labelSmall, color = colors.primary)
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Text(
        label,
        style = DamoimTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
        color = if (selected) colors.onPrimary else colors.textTertiary,
        modifier = Modifier.clip(RoundedCornerShape(999.dp))
            .background(if (selected) colors.primary else colors.surfaceVariant)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
    )
}

@Composable
private fun InfoBannerFree() {
    val colors = DamoimTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.primaryContainer).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        InfoIcon(tint = colors.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.size(10.dp))
        Text(DamoimStrings.CREATE_FREE_INFO, style = DamoimTheme.typography.bodySmall, color = colors.primaryDeep)
    }
}

@Composable
private fun FieldLabel(label: String, required: Boolean = false) {
    val style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
    Row {
        Text(label, style = style, color = DamoimTheme.colors.textSecondary)
        if (required) Text(" ${DamoimStrings.REQUIRED_MARK}", style = style, color = DamoimTheme.colors.primary)
    }
}

@Preview
@Composable
private fun ClubCreateScreenPreview() {
    DamoimTheme { ClubCreateScreen(state = ClubCreateUiState(name = "코딩하는 사람들")) }
}
