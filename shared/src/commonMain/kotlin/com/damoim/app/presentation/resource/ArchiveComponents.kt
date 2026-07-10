package com.damoim.app.presentation.resource

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.domain.model.ResourceFolder
import com.damoim.app.domain.repository.StorageUsage
import com.damoim.app.presentation.component.ChevronRightIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 폴더 → 한국어 라벨. (게시판의 boardCategoryLabel과 같은 역할) */
fun resourceFolderLabel(folder: ResourceFolder): String = when (folder) {
    ResourceFolder.DOCS -> DamoimStrings.ARCHIVE_FOLDER_DOCS
    ResourceFolder.ACCOUNTING -> DamoimStrings.ARCHIVE_FOLDER_ACCOUNTING
    ResourceFolder.PRESENTATION -> DamoimStrings.ARCHIVE_FOLDER_PRESENTATION
    ResourceFolder.PHOTOS -> DamoimStrings.ARCHIVE_FOLDER_PHOTOS
}

/** 확장자별 배지 색 (디자인 67: PDF 빨강 · XLSX 초록 · PPTX 주황 · HWP 파랑 · ZIP 회색). */
@Composable
fun resourceBadgeColor(ext: String): Color {
    val colors = DamoimTheme.colors
    return when (ext.uppercase()) {
        "PDF" -> colors.error
        "XLSX", "XLS", "CSV" -> colors.fileSheet
        "PPTX", "PPT" -> colors.filePresentation
        "HWP", "HWPX" -> colors.primary
        "DOC", "DOCX" -> colors.primaryDark
        else -> colors.fileArchive
    }
}

/**
 * 파일 형식 배지(정사각). 확장자 4글자(XLSX/PPTX)는 9sp, 3글자는 10sp — 디자인 그대로.
 */
@Composable
fun FileTypeBadge(
    ext: String,
    modifier: Modifier = Modifier,
    boxSize: Dp = 42.dp,
    cornerRadius: Dp = 12.dp,
) {
    Box(
        modifier = modifier.size(boxSize).clip(RoundedCornerShape(cornerRadius)).background(resourceBadgeColor(ext)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            ext,
            style = DamoimTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (ext.length >= 4) 9.sp else 10.sp,
            ),
            color = DamoimTheme.colors.onPrimary,
        )
    }
}

/** 67 저장공간 바 — 라벨 행 + 6dp 트랙. 사용량은 실제 파일 크기 합계에서 온다. */
@Composable
fun StorageBar(usage: StorageUsage?, modifier: Modifier = Modifier) {
    val colors = DamoimTheme.colors
    Column(modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(
                DamoimStrings.ARCHIVE_STORAGE_LABEL,
                style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            Text(
                usage?.usedLabel.orEmpty(),
                style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold),
                color = colors.textPrimary,
            )
            Text(
                DamoimStrings.archiveStorageTotal(usage?.totalLabel.orEmpty()),
                style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Normal),
                color = colors.textMuted,
            )
        }
        Spacer(Modifier.height(7.dp))
        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(99.dp)).background(colors.surfaceTrack)) {
            val fraction = usage?.fraction ?: 0f
            if (fraction > 0f) {
                Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().clip(RoundedCornerShape(99.dp)).background(colors.primary))
            }
        }
    }
}

/** 67 폴더 필터 칩. 활성 = primary 배경(게시판 칩은 textPrimary 배경이라 별도 변형). */
@Composable
fun ArchiveFilterChip(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Text(
        label,
        style = DamoimTheme.typography.caption.copy(fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold),
        color = if (active) colors.onPrimary else colors.textTertiary,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) colors.primary else colors.surfaceVariant)
            .noRippleClick(onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    )
}

/** 67 자료 목록 행 — 형식 배지 + 제목 + '올린이 · 시간 · 크기' + 우측 화살표. */
@Composable
fun ResourceRow(resource: ResourceFile, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DamoimTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier.fillMaxWidth()
            .shadow(2.dp, shape)
            .clip(shape)
            .background(colors.surface)
            .noRippleClick(onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FileTypeBadge(resource.ext)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                resource.title,
                style = DamoimTheme.typography.bodyStrong,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                DamoimStrings.resourceMeta(resource.uploaderName, resource.uploadedLabel, resource.sizeLabel),
                style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Normal, fontSize = 11.5.sp),
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(12.dp))
        ChevronRightIcon(tint = colors.outlineStrong, modifier = Modifier.size(19.dp))
    }
}
