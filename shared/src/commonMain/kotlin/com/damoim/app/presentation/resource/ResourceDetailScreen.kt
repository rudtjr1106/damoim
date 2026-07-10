package com.damoim.app.presentation.resource

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.domain.model.ResourceVisibility
import com.damoim.app.platform.PlatformBackHandler
import com.damoim.app.platform.rememberShareText
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.DownloadIcon
import com.damoim.app.presentation.component.MoreIcon
import com.damoim.app.presentation.component.ShareIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 상세 화면 위에 뜨는 오버레이(전부 이 화면 트리 안에서 렌더). */
private sealed interface ResourceOverlay {
    data object Menu : ResourceOverlay           // ⋯ 메뉴
    data object DeleteConfirm : ResourceOverlay  // 삭제 확인
}

/**
 * 화면 68 자료 상세 — Route. 다운로드는 카운트가 실제로 오르고, 삭제하면 목록으로 돌아간다.
 */
@Composable
fun ResourceDetailRoute(
    resourceId: Long,
    viewModel: ResourceDetailViewModel = viewModel(key = "resource_detail_$resourceId") {
        ResourceDetailViewModel(
            AppGraph.getResourceDetailUseCase,
            AppGraph.observeMyContextUseCase,
            AppGraph.getCohortsUseCase,
            AppGraph.resourceActionUseCase,
            resourceId,
        )
    },
    onBack: () -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ResourceDetailSideEffect.Toast -> onToast(effect.message)
                ResourceDetailSideEffect.Deleted -> {
                    onBack()
                    onToast(DamoimStrings.TOAST_RESOURCE_DELETED)
                }
            }
        }
    }
    ResourceDetailScreen(
        state = state,
        onBack = onBack,
        onDownload = viewModel::onDownload,
        onDelete = viewModel::onDelete,
        onToast = onToast,
    )
}

@Composable
fun ResourceDetailScreen(
    state: ResourceDetailUiState = ResourceDetailUiState(isLoading = false, resource = previewResources().first(), isLeader = true),
    onBack: () -> Unit = {},
    onDownload: () -> Unit = {},
    onDelete: () -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    val resource = state.resource
    val clipboard = LocalClipboardManager.current
    val share = rememberShareText()
    var overlay by remember { mutableStateOf<ResourceOverlay?>(null) }

    PlatformBackHandler(enabled = overlay != null) { overlay = null }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
            DetailHeader(onBack = onBack, onMore = { if (resource != null) overlay = ResourceOverlay.Menu })
            if (resource != null) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    DocumentPreview(resource) { onToast(DamoimStrings.TOAST_COMING_SOON) }
                    TitleBlock(resource)
                    InfoBox(resource, state.cohortSummary)
                }
                DetailActionBar(
                    onShare = { share(DamoimStrings.resourceShareText(resource.title)) },
                    onDownload = onDownload,
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
        }

        when (overlay) {
            ResourceOverlay.Menu -> ResourceMenuSheet(
                resource = resource,
                canDelete = state.canDelete,
                onDismiss = { overlay = null },
                onShare = {
                    overlay = null
                    resource?.let { share(DamoimStrings.resourceShareText(it.title)) }
                },
                onCopyLink = {
                    overlay = null
                    clipboard.setText(AnnotatedString("https://damoim.app/archive/${resource?.id}"))
                    onToast(DamoimStrings.TOAST_POST_LINK_COPIED)
                },
                onDelete = { overlay = ResourceOverlay.DeleteConfirm },
                onReport = {
                    overlay = null
                    onToast(DamoimStrings.TOAST_REPORTED)
                },
            )

            ResourceOverlay.DeleteConfirm -> ResourceDeleteDialog(
                onDismiss = { overlay = null },
                onConfirm = {
                    overlay = null
                    onDelete()
                },
            )

            null -> Unit
        }
    }
}

@Composable
private fun DetailHeader(onBack: () -> Unit, onMore: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(40.dp).noRippleClick(onBack), contentAlignment = Alignment.Center) {
                BackChevronIcon(tint = colors.textPrimary, modifier = Modifier.size(24.dp))
            }
            Text(
                DamoimStrings.RESOURCE_DETAIL_TITLE,
                style = DamoimTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Box(Modifier.size(40.dp).noRippleClick(onMore), contentAlignment = Alignment.Center) {
                MoreIcon(tint = colors.outlineStrong, modifier = Modifier.size(20.dp))
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
    }
}

/** 문서 미리보기 자리(사선 스트라이프). 실제 뷰어는 없고, 탭하면 준비 중 안내. */
@Composable
private fun DocumentPreview(resource: ResourceFile, onTap: () -> Unit) {
    val colors = DamoimTheme.colors
    val stripe = Brush.linearGradient(
        0f to colors.surfaceTrack,
        0.5f to colors.surfaceTrack,
        0.5f to colors.divider,
        1f to colors.divider,
        start = Offset(0f, 0f),
        end = Offset(22f, 22f),
        tileMode = TileMode.Repeated,
    )
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp))
            .background(stripe).noRippleClick(onTap),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            resource.ext,
            style = DamoimTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = colors.onPrimary,
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                .clip(RoundedCornerShape(8.dp)).background(resourceBadgeColor(resource.ext))
                .padding(horizontal = 9.dp, vertical = 4.dp),
        )
        Text(
            resource.pageCount?.let { DamoimStrings.resourcePreviewPages(it) } ?: DamoimStrings.RESOURCE_PREVIEW,
            style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Normal),
            color = colors.previewPillText,
            modifier = Modifier.clip(RoundedCornerShape(999.dp))
                .background(colors.surface.copy(alpha = 0.85f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun TitleBlock(resource: ResourceFile) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Text(
            resource.title,
            style = DamoimTheme.typography.titleLarge.copy(fontSize = 19.sp),
            color = colors.textPrimary,
        )
        if (resource.description.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                resource.description,
                style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, lineHeight = 21.sp),
                color = colors.textMuted,
            )
        }
    }
}

/**
 * 정보 박스. 디자인 68은 4행이지만, 특정 기수에만 공개된 자료는 그 사실이 보여야 하므로
 * [cohortSummary]가 있을 때만 '공개 범위' 행이 추가된다(전체 공개 자료는 디자인 그대로 4행).
 */
@Composable
private fun InfoBox(resource: ResourceFile, cohortSummary: String) {
    val colors = DamoimTheme.colors
    val restricted = resource.visibility == ResourceVisibility.COHORT_ONLY && cohortSummary.isNotBlank()
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(colors.surfaceInput)
            .padding(horizontal = 18.dp, vertical = 4.dp),
    ) {
        InfoRow(DamoimStrings.RESOURCE_INFO_UPLOADER, DamoimStrings.resourceUploaderName(resource.uploaderName, resource.uploaderIsLeader))
        InfoRow(DamoimStrings.RESOURCE_INFO_FOLDER, resourceFolderLabel(resource.folder))
        InfoRow(DamoimStrings.RESOURCE_INFO_FORMAT, DamoimStrings.resourceFormatValue(resource.ext, resource.sizeLabel))
        if (restricted) {
            InfoRow(DamoimStrings.RESOURCE_INFO_VISIBILITY, cohortSummary)
        }
        InfoRow(DamoimStrings.RESOURCE_INFO_DOWNLOADS, DamoimStrings.resourceDownloadCount(resource.downloadCount), showDivider = false)
    }
}

@Composable
private fun InfoRow(label: String, value: String, showDivider: Boolean = true) {
    val colors = DamoimTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, modifier = Modifier.weight(1f))
            Text(value, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
        }
        if (showDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
    }
}

/**
 * 하단 액션 바. 디자인은 좌우 버튼이 동일한 다운로드 글리프를 쓰는데(중복),
 * 좌측 정사각은 공유로 재해석했다(사용자 확인).
 */
@Composable
private fun DetailActionBar(onShare: () -> Unit, onDownload: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(colors.surfaceVariant).noRippleClick(onShare),
                contentAlignment = Alignment.Center,
            ) { ShareIcon(tint = colors.textTertiary, modifier = Modifier.size(21.dp)) }
            Row(
                modifier = Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(14.dp)).background(colors.primary).noRippleClick(onDownload),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DownloadIcon(tint = colors.onPrimary, modifier = Modifier.size(19.dp))
                Spacer(Modifier.width(8.dp))
                Text(DamoimStrings.RESOURCE_DOWNLOAD, style = DamoimTheme.typography.button, color = colors.onPrimary)
            }
        }
    }
}

@Preview
@Composable
private fun ResourceDetailScreenPreview() {
    DamoimTheme { ResourceDetailScreen() }
}
