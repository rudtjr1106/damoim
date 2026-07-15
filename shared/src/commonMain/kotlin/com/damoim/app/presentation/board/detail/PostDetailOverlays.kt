package com.damoim.app.presentation.board.detail

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.domain.model.BoardPost
import com.damoim.app.domain.model.Comment
import com.damoim.app.domain.model.ReportReason
import com.damoim.app.presentation.board.InitialAvatar
import com.damoim.app.presentation.board.boardCategoryLabel
import com.damoim.app.presentation.component.CloseIcon
import com.damoim.app.presentation.component.NetworkAvatar
import com.damoim.app.presentation.component.CopyIcon
import com.damoim.app.presentation.component.DamoimBottomSheet
import com.damoim.app.presentation.component.DamoimDialog
import com.damoim.app.presentation.component.DialogButton
import com.damoim.app.presentation.component.DownloadIcon
import com.damoim.app.presentation.component.EditIcon
import com.damoim.app.presentation.component.MegaphoneIcon
import com.damoim.app.presentation.component.ReplyIcon
import com.damoim.app.presentation.component.ShareIcon
import com.damoim.app.presentation.component.SheetActionRow
import com.damoim.app.presentation.component.SheetCloseButton
import com.damoim.app.presentation.component.TrashIcon
import com.damoim.app.presentation.component.WarningIcon
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme
import kotlinx.coroutines.launch

// ── 54 게시글 ⋯ 메뉴 (내 글: 수정/공유/고정/삭제 · 남의 글: 공유/신고) ──
@Composable
internal fun PostMenuSheet(
    post: BoardPost?,
    isMyPost: Boolean,
    isLeader: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit,
) {
    val colors = DamoimTheme.colors
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (post != null) {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.surfaceInput).padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(boardCategoryLabel(post.category), style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold), color = colors.primaryDark, modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(colors.primaryContainer).padding(horizontal = 8.dp, vertical = 3.dp))
                    Text(post.title, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary, maxLines = 1)
                }
            }
            Column {
                if (isMyPost) SheetActionRow(DamoimStrings.MENU_EDIT, onEdit, icon = { EditIcon(colors.textSecondary) })
                SheetActionRow(DamoimStrings.MENU_SHARE, onShare, icon = { ShareIcon(colors.textSecondary) })
                if (isLeader) {
                    SheetActionRow(DamoimStrings.MENU_PIN, onPin, icon = { MegaphoneIcon(colors.textSecondary, Modifier.size(19.dp)) }, trailing = {
                        Text(DamoimStrings.ADMIN_BADGE, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold), color = colors.textDisabled, modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(colors.surfaceVariant).padding(horizontal = 8.dp, vertical = 4.dp))
                    })
                }
                if (isMyPost || isLeader) {
                    SheetActionRow(DamoimStrings.MENU_DELETE, onDelete, textColor = colors.error, icon = { TrashIcon(colors.error) }, showDivider = false)
                } else {
                    SheetActionRow(DamoimStrings.CMENU_REPORT, onReport, textColor = colors.error, icon = { WarningIcon(colors.error, Modifier.size(19.dp)) }, showDivider = false)
                }
            }
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceInput).padding(horizontal = 14.dp, vertical = 11.dp)) {
                Text(DamoimStrings.POST_MENU_NOTE, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
            }
            SheetCloseButton(onDismiss)
        }
    }
}

// ── 55 댓글 길게 누르기 메뉴 ──
@Composable
internal fun CommentMenuSheet(comment: Comment, onDismiss: () -> Unit, onReply: () -> Unit, onCopy: () -> Unit, onReport: () -> Unit) {
    val colors = DamoimTheme.colors
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.primaryContainer).padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NetworkAvatar(url = comment.authorImageUrl, size = 30.dp) { InitialAvatar(comment.authorInitials, size = 30.dp, fontSize = 9.sp) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(comment.authorName, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                    Text(comment.content, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textSecondary, maxLines = 2)
                }
            }
            Column {
                SheetActionRow(DamoimStrings.CMENU_REPLY, onReply, icon = { ReplyIcon(colors.textSecondary) })
                SheetActionRow(DamoimStrings.CMENU_COPY, onCopy, icon = { CopyIcon(colors.textSecondary, Modifier.size(19.dp)) })
                SheetActionRow(DamoimStrings.CMENU_REPORT, onReport, textColor = colors.error, icon = { WarningIcon(colors.error, Modifier.size(19.dp)) }, showDivider = false)
            }
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceInput).padding(horizontal = 14.dp, vertical = 11.dp)) {
                Text(DamoimStrings.COMMENT_MENU_NOTE, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
            }
            SheetCloseButton(onDismiss)
        }
    }
}

// ── 56 게시글 삭제 확인 다이얼로그 ──
@Composable
internal fun DeleteConfirmDialog(commentCount: Int, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val colors = DamoimTheme.colors
    DamoimDialog(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(64.dp).clip(CircleShape).background(colors.errorContainer), contentAlignment = Alignment.Center) { TrashIcon(colors.error, Modifier.size(26.dp)) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(DamoimStrings.DELETE_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp), color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text(DamoimStrings.deleteMessage(commentCount), style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, lineHeight = 21.sp), color = colors.textMuted, textAlign = TextAlign.Center)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogButton(DamoimStrings.COMMON_CANCEL, bg = colors.surfaceVariant, fg = colors.textTertiary, modifier = Modifier.weight(1f), onClick = onDismiss)
                DialogButton(DamoimStrings.DELETE_CONFIRM, bg = colors.error, fg = colors.onPrimary, modifier = Modifier.weight(1f), onClick = onConfirm)
            }
        }
    }
}

// ── 82 신고 사유 선택 시트 ──
@Composable
internal fun ReportSheet(onDismiss: () -> Unit, onSubmit: () -> Unit) {
    val colors = DamoimTheme.colors
    var selected by remember { mutableStateOf<ReportReason?>(ReportReason.ABUSE) }
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Column(Modifier.padding(start = 6.dp, end = 6.dp, bottom = 4.dp)) {
                Text(DamoimStrings.REPORT_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 17.sp), color = colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(DamoimStrings.REPORT_SUBTITLE, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
            }
            ReportReason.entries.forEachIndexed { i, reason ->
                ReportReasonRow(reason, selected == reason, isLast = i == ReportReason.entries.lastIndex) { selected = reason }
            }
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.primary).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onSubmit).padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                Text(DamoimStrings.REPORT_SUBMIT, style = DamoimTheme.typography.button, color = colors.onPrimary)
            }
        }
    }
}

@Composable
private fun ReportReasonRow(reason: ReportReason, selected: Boolean, isLast: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth()
                .then(if (selected) Modifier.clip(RoundedCornerShape(12.dp)).background(colors.primaryContainer.copy(alpha = 0.5f)) else Modifier)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
                .padding(horizontal = 6.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(20.dp).clip(CircleShape).border(if (selected) 6.dp else 1.5.dp, if (selected) colors.primary else colors.outline, CircleShape))
            Text(DamoimStrings.reportReasonLabel(reason), style = DamoimTheme.typography.body.copy(fontSize = 14.5.sp), color = colors.textPrimary)
        }
        if (!selected && !isLast) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
    }
}

// ── 57 이미지 전체화면 뷰어 — HorizontalPager로 좌우 슬라이드 ──
@Composable
internal fun ImageViewerOverlay(images: List<String>, startIndex: Int, caption: String, onClose: () -> Unit, onDownload: () -> Unit) {
    val colors = DamoimTheme.colors
    val white = Color.White
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = startIndex.coerceIn(0, (images.size - 1).coerceAtLeast(0)),
        pageCount = { images.size.coerceAtLeast(1) },
    )
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(colors.imageViewerBg).safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose), contentAlignment = Alignment.Center) { CloseIcon(white, Modifier.size(24.dp)) }
            Spacer(Modifier.weight(1f))
            Text(DamoimStrings.imageIndex(pagerState.currentPage + 1, images.size.coerceAtLeast(1)), style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = white)
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(32.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDownload), contentAlignment = Alignment.Center) { DownloadIcon(white, Modifier.size(22.dp)) }
        }
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { page ->
            Box(Modifier.fillMaxSize().padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                com.damoim.app.presentation.component.NetworkImage(
                    url = images.getOrNull(page).orEmpty(),
                    modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f),
                    cornerRadius = 4.dp,
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                )
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(caption, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = white.copy(alpha = 0.55f))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                images.forEachIndexed { i, label ->
                    Box(
                        Modifier.size(52.dp).clip(RoundedCornerShape(8.dp))
                            .then(if (i == pagerState.currentPage) Modifier.border(1.5.dp, white, RoundedCornerShape(8.dp)) else Modifier)
                            .alpha(if (i == pagerState.currentPage) 1f else 0.45f)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                scope.launch { pagerState.animateScrollToPage(i) }
                            },
                    ) {
                        com.damoim.app.presentation.component.NetworkImage(label, Modifier.size(52.dp), cornerRadius = 8.dp)
                    }
                }
            }
        }
    }
}

// ── 84 신청자 명단 시트 ──
@Composable
internal fun RosterSheet(recruit: com.damoim.app.domain.model.RecruitInfo?, onDismiss: () -> Unit) {
    val colors = DamoimTheme.colors
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(DamoimStrings.ROSTER_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 17.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
                if (recruit != null) {
                    Text(DamoimStrings.recruitProgress(recruit.current, recruit.capacity), style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.primaryDark)
                }
            }
            val applicants = recruit?.applicants.orEmpty()
            applicants.forEachIndexed { i, applicant ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NetworkAvatar(url = applicant.imageUrl, size = 38.dp) { InitialAvatar(applicant.initials, size = 38.dp) }
                    Text(applicant.name.ifBlank { applicant.initials }, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold), color = colors.textPrimary, modifier = Modifier.weight(1f))
                    Text("${i + 1}번째 신청", style = DamoimTheme.typography.caption, color = colors.textMuted)
                }
                if (i != applicants.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
            }
            val extra = (recruit?.current ?: 0) - applicants.size
            if (extra > 0) {
                Text("외 ${extra}명이 신청했어요", style = DamoimTheme.typography.caption, color = colors.textMuted, modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp))
            }
            Spacer(Modifier.height(8.dp))
            SheetCloseButton(onDismiss)
        }
    }
}

// ── 73 게시글 상세 로딩 스켈레톤 ──
@Composable
internal fun PostDetailSkeleton(onBack: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surface)) {
        Row(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(start = 8.dp, top = 16.dp, bottom = 12.dp)) {
            Box(Modifier.size(40.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onBack), contentAlignment = Alignment.Center) {
                com.damoim.app.presentation.component.BackChevronIcon(colors.textPrimary, Modifier.size(24.dp))
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Skeleton(64.dp, 22.dp, radius = 999.dp)
            Skeleton(fraction = 0.85f, height = 18.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Skeleton(40.dp, 40.dp, radius = 40.dp)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { Skeleton(120.dp, 12.dp); Skeleton(80.dp, 9.dp) }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Skeleton(fraction = 1f, height = 12.dp); Skeleton(fraction = 0.96f, height = 12.dp); Skeleton(fraction = 1f, height = 12.dp); Skeleton(fraction = 0.6f, height = 12.dp)
            }
            Skeleton(fraction = 1f, height = 180.dp, radius = 16.dp)
        }
    }
}

/** 셔머 펄스 스켈레톤 블록. fraction 지정 시 가로 비율, 아니면 고정폭. */
@Composable
private fun Skeleton(width: Dp = 0.dp, height: Dp = 12.dp, fraction: Float = 0f, radius: Dp = 8.dp) {
    val colors = DamoimTheme.colors
    val transition = rememberInfiniteTransition(label = "sk")
    val a by transition.animateFloat(1f, 0.4f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "a")
    val base = Modifier.height(height).clip(RoundedCornerShape(radius)).alpha(a).background(colors.skeletonBase)
    if (fraction > 0f) Box(Modifier.fillMaxWidth(fraction).then(base)) else Box(Modifier.width(width).then(base))
}
