package com.damoim.app.presentation.resource

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.domain.model.Cohort
import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.domain.model.ResourceFolder
import com.damoim.app.presentation.component.CheckIcon
import com.damoim.app.presentation.component.DamoimBottomSheet
import com.damoim.app.presentation.component.DamoimDialog
import com.damoim.app.presentation.component.DialogButton
import com.damoim.app.presentation.component.LinkIcon
import com.damoim.app.presentation.component.LockIcon
import com.damoim.app.presentation.component.SheetActionRow
import com.damoim.app.presentation.component.SheetCloseButton
import com.damoim.app.presentation.component.ShareIcon
import com.damoim.app.presentation.component.TrashIcon
import com.damoim.app.presentation.component.WarningIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 68 자료 ⋯ 메뉴 시트. 디자인 아카이브에 없어 54(게시글 ⋯ 메뉴) 패턴을 그대로 따른다.
 * 내 자료이거나 동아리장이면 삭제, 아니면 신고가 노출된다.
 */
@Composable
internal fun ResourceMenuSheet(
    resource: ResourceFile?,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit,
) {
    val colors = DamoimTheme.colors
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (resource != null) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.surfaceInput)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    FileTypeBadge(resource.ext, boxSize = 34.dp, cornerRadius = 10.dp)
                    Text(
                        resource.title,
                        style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Column {
                SheetActionRow(DamoimStrings.MENU_SHARE, onShare, icon = { ShareIcon(colors.textSecondary) })
                SheetActionRow(DamoimStrings.MENU_COPY_LINK, onCopyLink, icon = { LinkIcon(colors.textSecondary, Modifier.size(19.dp)) })
                if (canDelete) {
                    SheetActionRow(DamoimStrings.MENU_DELETE, onDelete, textColor = colors.error, icon = { TrashIcon(colors.error) }, showDivider = false)
                } else {
                    SheetActionRow(DamoimStrings.CMENU_REPORT, onReport, textColor = colors.error, icon = { WarningIcon(colors.error, Modifier.size(19.dp)) }, showDivider = false)
                }
            }
            SheetCloseButton(onDismiss)
        }
    }
}

/** 68 자료 삭제 확인 다이얼로그(56 패턴). */
@Composable
internal fun ResourceDeleteDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val colors = DamoimTheme.colors
    DamoimDialog(onDismiss = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(64.dp).clip(CircleShape).background(colors.errorContainer), contentAlignment = Alignment.Center) {
                    TrashIcon(colors.error, Modifier.size(26.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        DamoimStrings.RESOURCE_DELETE_TITLE,
                        style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp),
                        color = colors.textPrimary,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        DamoimStrings.RESOURCE_DELETE_MESSAGE,
                        style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, lineHeight = 21.sp),
                        color = colors.textMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogButton(DamoimStrings.COMMON_CANCEL, bg = colors.surfaceVariant, fg = colors.textTertiary, modifier = Modifier.weight(1f), onClick = onDismiss)
                DialogButton(DamoimStrings.DELETE_CONFIRM, bg = colors.error, fg = colors.onPrimary, modifier = Modifier.weight(1f), onClick = onConfirm)
            }
        }
    }
}

/**
 * 69 폴더 선택 시트. 디자인 아카이브에 없어 52/53(카테고리 선택 시트) 패턴을 따른다.
 * 일반 회원은 활동사진 폴더에만 올릴 수 있어 나머지는 잠금(52의 '공지 잠김'과 동일).
 */
@Composable
internal fun FolderPickerSheet(
    selected: ResourceFolder,
    isLeader: Boolean,
    onDismiss: () -> Unit,
    onSelect: (ResourceFolder) -> Unit,
) {
    val colors = DamoimTheme.colors
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                DamoimStrings.RESOURCE_FOLDER_SHEET_TITLE,
                style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = colors.textPrimary,
                modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            )
            Column {
                ResourceFolder.entries.forEachIndexed { index, folder ->
                    val locked = !isLeader && folder != ResourceFolder.PHOTOS
                    SheetActionRow(
                        resourceFolderLabel(folder),
                        onClick = { if (!locked) onSelect(folder) },
                        textColor = when {
                            locked -> colors.textDisabled
                            folder == selected -> colors.primary
                            else -> colors.textPrimary
                        },
                        trailing = {
                            when {
                                locked -> LockIcon(colors.textDisabled, Modifier.size(17.dp))
                                folder == selected -> CheckIcon(colors.primary)
                            }
                        },
                        showDivider = index != ResourceFolder.entries.lastIndex,
                    )
                }
            }
            if (!isLeader) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceInput).padding(horizontal = 14.dp, vertical = 11.dp)) {
                    Text(
                        DamoimStrings.RESOURCE_FOLDER_MEMBER_NOTE,
                        style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal),
                        color = colors.textMuted,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            SheetCloseButton(onDismiss)
        }
    }
}

/**
 * 69 공개 기수 선택 시트 (복수 선택). 디자인 아카이브에 없어 42(기수 변경 시트) 카드 행 패턴을 따르되,
 * 여러 기수를 고를 수 있도록 라디오 대신 체크로 바꿨다.
 */
@Composable
internal fun CohortPickerSheet(
    cohorts: List<Cohort>,
    selected: Set<Long>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Long>) -> Unit,
) {
    val colors = DamoimTheme.colors
    var picked by remember { mutableStateOf(selected) }
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Column(Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                Text(
                    DamoimStrings.RESOURCE_COHORT_SHEET_TITLE,
                    style = DamoimTheme.typography.titleLarge.copy(fontSize = 19.sp),
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    DamoimStrings.RESOURCE_COHORT_SHEET_DESC,
                    style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                    color = colors.textMuted,
                )
            }
            if (cohorts.isEmpty()) {
                Text(
                    DamoimStrings.RESOURCE_COHORT_EMPTY,
                    style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                    color = colors.textDisabled,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    textAlign = TextAlign.Center,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cohorts.forEach { cohort ->
                        CohortRow(cohort, cohort.id in picked) {
                            picked = if (cohort.id in picked) picked - cohort.id else picked + cohort.id
                        }
                    }
                }
            }
            DialogButton(
                DamoimStrings.RESOURCE_COHORT_CONFIRM,
                bg = colors.primary,
                fg = colors.onPrimary,
                modifier = Modifier.fillMaxWidth().alpha(if (picked.isEmpty()) 0.35f else 1f),
                onClick = { if (picked.isNotEmpty()) onConfirm(picked) },
            )
        }
    }
}

/** 42 기수 변경 시트의 카드 행 — 선택 시 primary 테두리 + primaryContainer 배경. */
@Composable
private fun CohortRow(cohort: Cohort, selected: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(shape)
            .then(if (selected) Modifier.background(colors.primaryContainer) else Modifier)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) colors.primary else colors.divider, shape)
            .noRippleClick(onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(20.dp).clip(CircleShape)
                .then(if (selected) Modifier.background(colors.primary) else Modifier.border(1.5.dp, colors.outline, CircleShape)),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) CheckIcon(colors.onPrimary, Modifier.size(13.dp))
        }
        Text(
            cohort.label,
            style = DamoimTheme.typography.body.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold, fontSize = 15.sp),
            color = if (selected) colors.textPrimary else colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            DamoimStrings.resourceCohortMembers(cohort.memberCount),
            style = DamoimTheme.typography.caption,
            color = if (selected) colors.textTertiary else colors.textDisabled,
        )
    }
}
