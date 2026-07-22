package com.damoim.app.presentation.resource.archive

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.domain.model.ResourceFolder
import com.damoim.app.domain.repository.StorageUsage
import com.damoim.app.data.remote.core.DataTopic
import com.damoim.app.data.remote.core.RemoteBus
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.PullRefreshColumn
import com.damoim.app.presentation.component.BottomNavBar
import com.damoim.app.presentation.component.FolderIcon
import com.damoim.app.presentation.component.MainTab
import com.damoim.app.presentation.component.SearchIcon
import com.damoim.app.presentation.component.UploadIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.resource.ArchiveFilterChip
import com.damoim.app.presentation.resource.ResourceRow
import com.damoim.app.presentation.resource.StorageBar
import com.damoim.app.presentation.resource.resourceFolderLabel
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 67 자료실 홈 — Route. 홈 퀵액션 '자료실'에서 진입하며 하단 탭바는 '홈' 활성 상태로 유지된다.
 * 행 탭 → 68 상세, FAB → 69 올리기.
 */
@Composable
fun ArchiveRoute(
    viewModel: ArchiveViewModel = viewModel(key = "archive") {
        ArchiveViewModel(AppGraph.getResourcesUseCase, AppGraph.getStorageUsageUseCase, AppGraph.getCohortsUseCase)
    },
    onBack: () -> Unit = {},
    onSearch: () -> Unit = {},
    onOpenResource: (Long) -> Unit = {},
    onUpload: () -> Unit = {},
    onTabSelect: (MainTab) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    ArchiveScreen(
        state = state,
        onBack = onBack,
        onSearch = onSearch,
        onSelectFolder = viewModel::onSelectFolder,
        onSelectCohort = viewModel::onSelectCohort,
        onOpenResource = onOpenResource,
        onUpload = onUpload,
        onTabSelect = onTabSelect,
    )
}

@Composable
fun ArchiveScreen(
    state: ArchiveUiState = ArchiveUiState(isLoading = false, resources = previewResources(), storage = previewStorage()),
    onBack: () -> Unit = {},
    onSearch: () -> Unit = {},
    onSelectFolder: (ResourceFolder?) -> Unit = {},
    onSelectCohort: (Long?) -> Unit = {},
    onOpenResource: (Long) -> Unit = {},
    onUpload: () -> Unit = {},
    onTabSelect: (MainTab) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.surfaceInput)) {
        // 헤더(뒤로가기·제목·검색 아이콘·저장공간·폴더칩·기수칩)는 고정, 아래 목록만 스크롤한다
        ArchiveHeader(state, onBack, onSearch, onSelectFolder, onSelectCohort)
        Box(Modifier.weight(1f)) {
            PullRefreshColumn(onRefresh = { RemoteBus.invalidate(DataTopic.RESOURCE) }, modifier = Modifier.fillMaxSize()) {
                when {
                    state.isEmpty -> ArchiveEmpty(onUpload)
                    else -> ArchiveList(state, onOpenResource)
                }
            }
            if (!state.isEmpty) {
                UploadFab(onUpload, Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 20.dp))
            }
        }
        BottomNavBar(selected = MainTab.ARCHIVE, onSelect = onTabSelect)
    }
}

@Composable
private fun ArchiveHeader(
    state: ArchiveUiState,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onSelectFolder: (ResourceFolder?) -> Unit,
    onSelectCohort: (Long?) -> Unit,
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().background(colors.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(24.dp).noRippleClick(onBack), contentAlignment = Alignment.Center) {
                BackChevronIcon(tint = colors.textPrimary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(DamoimStrings.ARCHIVE_TITLE, style = DamoimTheme.typography.titleMedium, color = colors.textPrimary, modifier = Modifier.weight(1f))
            // 67 헤더 우측 검색 아이콘 → 전용 검색 화면(게시판 검색과 대칭)
            Box(Modifier.size(24.dp).noRippleClick(onSearch), contentAlignment = Alignment.Center) {
                SearchIcon(tint = colors.textSecondary, modifier = Modifier.size(22.dp))
            }
        }
        StorageBar(state.storage, Modifier.padding(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 2.dp, bottom = if (state.cohorts.isEmpty()) 14.dp else 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ArchiveFilterChip(DamoimStrings.ARCHIVE_FILTER_ALL, active = state.folder == null) { onSelectFolder(null) }
            ResourceFolder.entries.forEach { folder ->
                ArchiveFilterChip(resourceFolderLabel(folder), active = state.folder == folder) { onSelectFolder(folder) }
            }
        }
        // 기수 필터 — 동아리에 기수가 있을 때만 노출(폴더 필터와 독립적으로 겹쳐 적용된다).
        if (state.cohorts.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ArchiveFilterChip(DamoimStrings.ARCHIVE_COHORT_ALL, active = state.selectedCohortId == null) { onSelectCohort(null) }
                state.cohorts.forEach { cohort ->
                    ArchiveFilterChip(cohort.short, active = state.selectedCohortId == cohort.id) { onSelectCohort(cohort.id) }
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
    }
}

@Composable
private fun ArchiveList(state: ArchiveUiState, onOpenResource: (Long) -> Unit) {
    val colors = DamoimTheme.colors
    val list = state.visibleResources
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val folder = state.folder
        val cohort = state.cohorts.firstOrNull { it.id == state.selectedCohortId }
        Text(
            when {
                folder != null -> DamoimStrings.archiveFolderCount(resourceFolderLabel(folder), list.size)
                cohort != null -> DamoimStrings.archiveFolderCount(cohort.short, list.size)
                else -> DamoimStrings.archiveCount(state.storage?.count ?: list.size)
            },
            style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp),
            color = colors.textMuted,
        )
        if (state.isFolderEmpty) {
            Text(
                DamoimStrings.ARCHIVE_FOLDER_EMPTY,
                style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                color = colors.textDisabled,
                modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                textAlign = TextAlign.Center,
            )
        }
        list.forEach { resource ->
            ResourceRow(resource, onClick = { onOpenResource(resource.id) })
        }
        Spacer(Modifier.height(70.dp))   // FAB 아래 여백
    }
}

@Composable
private fun UploadFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DamoimTheme.colors
    Box(
        modifier = modifier.size(56.dp).shadow(14.dp, CircleShape).clip(CircleShape)
            .background(colors.primary).noRippleClick(onClick),
        contentAlignment = Alignment.Center,
    ) { UploadIcon(tint = colors.onPrimary, modifier = Modifier.size(24.dp)) }
}

/** 자료실 빈 상태 — 디자인 아카이브에 없어 41 게시판 빈 상태 패턴을 그대로 따른다(신규 동아리). */
@Composable
private fun ArchiveEmpty(onUpload: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(96.dp).clip(RoundedCornerShape(28.dp)).background(colors.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) { FolderIcon(tint = colors.textDisabled, modifier = Modifier.size(40.dp)) }
        Spacer(Modifier.height(20.dp))
        Text(
            DamoimStrings.ARCHIVE_EMPTY_TITLE,
            style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp),
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            DamoimStrings.ARCHIVE_EMPTY_SUBTITLE,
            style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
            color = colors.textMuted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(22.dp))
        Row(
            modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(colors.primary)
                .noRippleClick(onUpload).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UploadIcon(tint = colors.onPrimary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(DamoimStrings.ARCHIVE_EMPTY_CTA, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.onPrimary)
        }
    }
}

/** 프리뷰/기본값용 샘플 (presentation이 data.mock에 의존하지 않도록 여기 둔다). */
internal fun previewResources(): List<ResourceFile> = listOf(
    ResourceFile(9001, "동아리 회칙 v3.2", "동아리 회칙 v3.2.pdf", "PDF", folder = ResourceFolder.DOCS, sizeLabel = "1.4MB", sizeBytes = 1_468_006, uploaderId = 2001, uploaderName = "김민준", uploaderIsLeader = true, uploadedLabel = "3일 전", downloadCount = 28, pageCount = 12, createdAt = 120),
    ResourceFile(9002, "2025 상반기 회계내역", "2025 상반기 회계내역.xlsx", "XLSX", folder = ResourceFolder.ACCOUNTING, sizeLabel = "88KB", sizeBytes = 90_112, uploaderId = 2007, uploaderName = "강도윤", uploadedLabel = "1주 전", downloadCount = 15, createdAt = 110),
    ResourceFile(9003, "신입 OT 발표자료", "신입 OT 발표자료.pptx", "PPTX", folder = ResourceFolder.PRESENTATION, sizeLabel = "6.2MB", sizeBytes = 6_501_171, uploaderId = 2004, uploaderName = "최유진", uploadedLabel = "2주 전", downloadCount = 41, createdAt = 100),
    ResourceFile(9004, "동아리 소개서 최종본", "동아리 소개서 최종본.hwp", "HWP", folder = ResourceFolder.DOCS, sizeLabel = "240KB", sizeBytes = 245_760, uploaderId = 2001, uploaderName = "김민준", uploaderIsLeader = true, uploadedLabel = "3주 전", downloadCount = 33, createdAt = 90),
    ResourceFile(9005, "2024 MT 사진 모음", "2024 MT 사진 모음.zip", "ZIP", folder = ResourceFolder.PHOTOS, sizeLabel = "84MB", sizeBytes = 88_080_384, uploaderId = 2004, uploaderName = "최유진", uploadedLabel = "1개월 전", downloadCount = 57, createdAt = 80),
)

internal fun previewStorage() = StorageUsage(
    usedBytes = 1_269_247_998, totalBytes = 5L * 1024 * 1024 * 1024,
    usedLabel = "1.2GB", totalLabel = "5GB", count = 12,
)

@Preview
@Composable
private fun ArchiveScreenPreview() {
    DamoimTheme { ArchiveScreen() }
}

@Preview
@Composable
private fun ArchiveEmptyPreview() {
    DamoimTheme {
        ArchiveScreen(state = ArchiveUiState(isLoading = false, resources = emptyList(), storage = StorageUsage(totalBytes = 5L * 1024 * 1024 * 1024)))
    }
}
