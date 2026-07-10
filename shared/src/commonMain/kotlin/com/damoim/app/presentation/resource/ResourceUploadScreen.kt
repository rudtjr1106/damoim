package com.damoim.app.presentation.resource

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.ResourceDraft
import com.damoim.app.domain.model.ResourceFolder
import com.damoim.app.domain.model.ResourceVisibility
import com.damoim.app.platform.PickedDocument
import com.damoim.app.platform.PlatformBackHandler
import com.damoim.app.platform.rememberDocumentPickerLauncher
import com.damoim.app.presentation.component.ChevronDownIcon
import com.damoim.app.presentation.component.CloseIcon
import com.damoim.app.presentation.component.DamoimTextField
import com.damoim.app.presentation.component.PrimaryButton
import com.damoim.app.presentation.component.UploadIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 69 화면 위에 뜨는 오버레이(전부 이 화면 트리 안에서 렌더). */
private sealed interface UploadOverlay {
    data object FolderPicker : UploadOverlay
    data object CohortPicker : UploadOverlay
}

/**
 * 화면 69 자료 올리기 — Route. 실제 문서 피커로 파일을 고르고, 올리면 스토어에 반영되어
 * 67 목록 최상단과 저장공간 바에 즉시 나타난다.
 */
@Composable
fun ResourceUploadRoute(
    viewModel: ResourceUploadViewModel = viewModel(key = "resource_upload") {
        ResourceUploadViewModel(AppGraph.uploadResourceUseCase, AppGraph.observeMyContextUseCase, AppGraph.getCohortsUseCase)
    },
    onCancel: () -> Unit = {},
    onUploaded: () -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ResourceUploadSideEffect.Toast -> onToast(effect.message)
                ResourceUploadSideEffect.Uploaded -> onUploaded()
            }
        }
    }
    ResourceUploadScreen(state = state, onCancel = onCancel, onSubmit = viewModel::onSubmit)
}

@Composable
fun ResourceUploadScreen(
    state: ResourceUploadUiState = ResourceUploadUiState(),
    onCancel: () -> Unit = {},
    onSubmit: (ResourceDraft) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    var picked by remember { mutableStateOf<PickedDocument?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var folder by remember { mutableStateOf(ResourceFolder.DOCS) }
    var visibility by remember { mutableStateOf(ResourceVisibility.ALL_MEMBERS) }
    var cohortIds by remember { mutableStateOf(emptySet<Long>()) }
    var overlay by remember { mutableStateOf<UploadOverlay?>(null) }

    // 일반 회원은 활동사진 폴더에만 올릴 수 있다. 역할이 확정된 뒤에 기본 폴더를 정한다.
    LaunchedEffect(state.role) {
        if (state.role == ClubRole.MEMBER) folder = ResourceFolder.PHOTOS
    }

    // 프리뷰(Layoutlib)에는 Activity가 없어 런처를 만들지 않는다
    val inPreview = LocalInspectionMode.current
    val documentPicker = if (inPreview) null else rememberDocumentPickerLauncher { doc ->
        // doc == null 은 사용자 취소(또는 iOS 스텁) — 게시글 첨부와 동일하게 조용히 무시
        if (doc != null) {
            picked = doc
            if (title.isBlank()) title = doc.name.substringBeforeLast('.', doc.name)
        }
    }

    val cohortChosen = visibility == ResourceVisibility.ALL_MEMBERS || cohortIds.isNotEmpty()
    val canSubmit = picked != null && title.isNotBlank() && cohortChosen && !state.isSubmitting
    fun submit() {
        val file = picked ?: return
        if (!canSubmit) return
        onSubmit(
            ResourceDraft(
                fileName = file.name,
                sizeLabel = file.sizeLabel,
                title = title.trim(),
                description = description.trim(),
                // 일반 회원이 어떤 경로로든 다른 폴더를 담지 못하게 마지막에 한 번 더 강제
                folder = if (state.isLeader) folder else ResourceFolder.PHOTOS,
                visibility = visibility,
                cohortIds = if (visibility == ResourceVisibility.COHORT_ONLY) cohortIds.toList() else emptyList(),
            ),
        )
    }

    // 업로드가 진행 중이면 화면을 못 벗어나게 막는다 — 도중에 나가면 완료 이벤트가 갈 곳을 잃는다
    PlatformBackHandler(enabled = overlay != null || state.isSubmitting) {
        if (overlay != null) overlay = null
    }

    Box(Modifier.fillMaxSize()) {
        // CTA가 키보드를 따라 올라오지 않도록 ime는 제외(B1 동아리 생성과 동일)
        Column(Modifier.fillMaxSize().background(colors.surface).windowInsetsPadding(WindowInsets.safeDrawing.exclude(WindowInsets.ime))) {
            UploadHeader(canCancel = !state.isSubmitting, onCancel = onCancel)
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                picked?.let { file ->
                    AttachedFileCard(file, onRemove = { picked = null })
                }
                DropZone(onClick = { documentPicker?.launch() })
                FieldGroup(DamoimStrings.RESOURCE_FIELD_FOLDER) {
                    FolderField(folder) { overlay = UploadOverlay.FolderPicker }
                }
                FieldGroup(DamoimStrings.RESOURCE_FIELD_TITLE) {
                    DamoimTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = DamoimStrings.RESOURCE_TITLE_PLACEHOLDER,
                        textStyle = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                        strokeColor = colors.divider,
                        cornerRadius = 14.dp,
                        borderWidth = 1.dp,
                    )
                }
                FieldGroup(DamoimStrings.RESOURCE_FIELD_DESC, optional = true) {
                    DamoimTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = DamoimStrings.RESOURCE_DESC_PLACEHOLDER,
                        singleLine = false,
                        strokeColor = colors.divider,
                        cornerRadius = 14.dp,
                        borderWidth = 1.dp,
                        modifier = Modifier.heightIn(min = 64.dp),
                    )
                }
                FieldGroup(DamoimStrings.RESOURCE_FIELD_VISIBILITY) {
                    VisibilityRow(visibility) { choice ->
                        if (choice == ResourceVisibility.ALL_MEMBERS) {
                            visibility = ResourceVisibility.ALL_MEMBERS
                            cohortIds = emptySet()
                        } else {
                            // 기수를 고르기 전에는 확정하지 않는다 — 시트에서 '선택 완료'해야 적용
                            overlay = UploadOverlay.CohortPicker
                        }
                    }
                    if (visibility == ResourceVisibility.COHORT_ONLY && cohortIds.isNotEmpty()) {
                        val labels = state.cohorts.filter { it.id in cohortIds }.joinToString(" · ") { it.short }
                        Text(
                            DamoimStrings.resourceCohortSummary(labels),
                            style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal),
                            color = colors.primaryDark,
                            modifier = Modifier.padding(horizontal = 2.dp),
                        )
                    }
                }
            }
            Column(Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
                PrimaryButton(
                    text = DamoimStrings.RESOURCE_UPLOAD_SUBMIT,
                    onClick = { submit() },
                    enabled = canSubmit,
                    loading = state.isSubmitting,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
                )
            }
        }

        when (overlay) {
            UploadOverlay.FolderPicker -> FolderPickerSheet(
                selected = folder,
                isLeader = state.isLeader,
                onDismiss = { overlay = null },
                onSelect = {
                    folder = it
                    overlay = null
                },
            )

            UploadOverlay.CohortPicker -> CohortPickerSheet(
                cohorts = state.cohorts,
                selected = cohortIds,
                onDismiss = { overlay = null },
                onConfirm = { picks ->
                    cohortIds = picks
                    visibility = ResourceVisibility.COHORT_ONLY
                    overlay = null
                },
            )

            null -> Unit
        }
    }
}

@Composable
private fun UploadHeader(canCancel: Boolean, onCancel: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(24.dp).noRippleClick { if (canCancel) onCancel() }, contentAlignment = Alignment.Center) {
                CloseIcon(tint = if (canCancel) colors.textMuted else colors.outlineStrong, modifier = Modifier.size(24.dp))
            }
            Text(
                DamoimStrings.RESOURCE_UPLOAD_TITLE,
                style = DamoimTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            // 제출은 하단 CTA 하나로 (디자인의 우상단 '올리기'는 중복이라 제거)
            Spacer(Modifier.size(24.dp))
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
    }
}

/** 선택한 파일 카드. X를 누르면 선택 해제되고 CTA가 다시 비활성이 된다. */
@Composable
private fun AttachedFileCard(file: PickedDocument, onRemove: () -> Unit) {
    val colors = DamoimTheme.colors
    val ext = file.name.substringAfterLast('.', "").uppercase().take(4).ifBlank { "DOC" }
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceInput)
            .border(1.dp, colors.dividerLight, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FileTypeBadge(ext, boxSize = 40.dp, cornerRadius = 11.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                file.name,
                style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(1.dp))
            Text(
                DamoimStrings.resourceUploadReady(file.sizeLabel),
                style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Normal, fontSize = 11.5.sp),
                color = colors.textMuted,
            )
        }
        Spacer(Modifier.width(12.dp))
        Box(Modifier.size(18.dp).noRippleClick(onRemove), contentAlignment = Alignment.Center) {
            CloseIcon(tint = colors.textDisabled, modifier = Modifier.size(18.dp))
        }
    }
}

/** 파일 추가 드롭존(점선 테두리). 탭하면 시스템 문서 피커가 열린다. */
@Composable
private fun DropZone(onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth()
            .dashedBorder(colors.outline, width = 1.5.dp, cornerRadius = 16.dp)
            .noRippleClick(onClick)
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { UploadIcon(tint = colors.primary, modifier = Modifier.size(20.dp)) }
        Text(DamoimStrings.RESOURCE_UPLOAD_ADD_FILE, style = DamoimTheme.typography.bodyStrong, color = colors.textPrimary)
        Text(
            DamoimStrings.RESOURCE_UPLOAD_HINT,
            style = DamoimTheme.typography.label.copy(fontSize = 11.5.sp, lineHeight = 17.sp),
            color = colors.textMuted,
            textAlign = TextAlign.Center,
        )
    }
}

/** 점선 테두리(69 드롭존). Compose에 dashed border modifier가 없어 직접 그린다. */
private fun Modifier.dashedBorder(color: Color, width: Dp, cornerRadius: Dp): Modifier = drawBehind {
    val stroke = width.toPx()
    drawRoundRect(
        color = color,
        style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(stroke * 4, stroke * 3))),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
    )
}

/** 라벨 + 입력 한 묶음. 디자인은 묶음 안 간격 8px, 묶음 사이 20px. */
@Composable
private fun FieldGroup(label: String, optional: Boolean = false, content: @Composable () -> Unit) {
    val colors = DamoimTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.padding(horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp),
                color = colors.textMuted,
            )
            if (optional) {
                Spacer(Modifier.width(4.dp))
                Text(DamoimStrings.RESOURCE_FIELD_OPTIONAL, style = DamoimTheme.typography.caption, color = colors.outlineStrong)
            }
        }
        content()
    }
}

@Composable
private fun FolderField(folder: ResourceFolder, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, colors.divider, RoundedCornerShape(14.dp))
            .noRippleClick(onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            resourceFolderLabel(folder),
            style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        ChevronDownIcon(tint = colors.outlineStrong, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun VisibilityRow(selected: ResourceVisibility, onSelect: (ResourceVisibility) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        VisibilityOption(DamoimStrings.RESOURCE_VISIBILITY_ALL, selected == ResourceVisibility.ALL_MEMBERS, Modifier.weight(1f)) {
            onSelect(ResourceVisibility.ALL_MEMBERS)
        }
        VisibilityOption(DamoimStrings.RESOURCE_VISIBILITY_COHORT, selected == ResourceVisibility.COHORT_ONLY, Modifier.weight(1f)) {
            onSelect(ResourceVisibility.COHORT_ONLY)
        }
    }
}

@Composable
private fun VisibilityOption(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .then(if (active) Modifier.background(colors.primaryContainer) else Modifier)
            .border(if (active) 1.5.dp else 1.dp, if (active) colors.primary else colors.divider, shape)
            .noRippleClick(onClick)
            .padding(vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = DamoimTheme.typography.bodySmall.copy(fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold, fontSize = 13.5.sp),
            color = if (active) colors.primaryDeep else colors.textTertiary,
        )
    }
}

@Preview
@Composable
private fun ResourceUploadScreenPreview() {
    DamoimTheme { ResourceUploadScreen() }
}
