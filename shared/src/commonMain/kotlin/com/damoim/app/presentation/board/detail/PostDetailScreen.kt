package com.damoim.app.presentation.board.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.BoardPost
import com.damoim.app.domain.model.Comment
import com.damoim.app.domain.model.Poll
import com.damoim.app.domain.model.PostAttachment
import com.damoim.app.domain.model.PostDetail
import com.damoim.app.domain.model.RecruitInfo
import com.damoim.app.domain.model.RecruitStatus
import com.damoim.app.platform.PlatformBackHandler
import com.damoim.app.platform.rememberShareText
import com.damoim.app.presentation.board.CategoryBadge
import com.damoim.app.presentation.board.InitialAvatar
import com.damoim.app.presentation.board.SolidBadge
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.CalendarIcon
import com.damoim.app.presentation.component.ChartIcon
import com.damoim.app.presentation.component.CheckIcon
import com.damoim.app.presentation.component.ClockIcon
import com.damoim.app.presentation.component.CloseIcon
import com.damoim.app.presentation.component.CommentIcon
import com.damoim.app.presentation.component.CrownIcon
import com.damoim.app.presentation.component.DownloadIcon
import com.damoim.app.presentation.component.HeartIcon
import com.damoim.app.presentation.component.LinkIcon
import com.damoim.app.presentation.component.ListIcon
import com.damoim.app.presentation.component.MoreIcon
import com.damoim.app.presentation.component.NetworkImage
import com.damoim.app.presentation.component.PaperclipIcon
import com.damoim.app.presentation.component.SendIcon
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 상세 화면 위에 뜨는 오버레이(전부 이 화면 트리 안에서 렌더). */
private sealed interface DetailOverlay {
    data object PostMenu : DetailOverlay                       // 54 ⋯ 메뉴
    data class CommentMenu(val comment: Comment) : DetailOverlay // 55 댓글 메뉴
    data object DeleteConfirm : DetailOverlay                  // 56 삭제 확인
    data object Report : DetailOverlay                         // 82 신고 사유
    data class ImageViewer(val images: List<String>, val index: Int, val caption: String) : DetailOverlay // 57
    data object Roster : DetailOverlay                          // 84 신청자 명단
}

/**
 * 화면 14/36/84 게시글·모집 상세 — Route. 조회 실패 시 79 오류.
 * 좋아요/투표/모집 신청/댓글 작성이 실제로 동작하고, 삭제 시 [onDeleted]로 목록에 복귀한다.
 */
@Composable
fun PostDetailRoute(
    postId: Long,
    viewModel: PostDetailViewModel = viewModel(key = "postdetail_$postId") {
        PostDetailViewModel(AppGraph.getPostDetailUseCase, AppGraph.observeMyContextUseCase, AppGraph.postActionUseCase, postId)
    },
    onBack: () -> Unit = {},
    onEdit: (BoardPost) -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is PostDetailSideEffect.Toast -> onToast(effect.message)
                PostDetailSideEffect.Deleted -> {
                    onBack()
                    onToast(DamoimStrings.TOAST_POST_DELETED)
                }
            }
        }
    }
    when {
        state.notFound -> PostErrorScreen(onBack = onBack)
        else -> PostDetailScreen(
            state = state,
            onBack = onBack,
            onEdit = onEdit,
            onToast = onToast,
            onToggleLike = viewModel::onToggleLike,
            onVote = viewModel::onVote,
            onRevote = viewModel::onRevote,
            onApplyRecruit = viewModel::onApplyRecruit,
            onCommentChange = viewModel::onCommentInputChange,
            onReplyTo = viewModel::onReplyTo,
            onSendComment = viewModel::onSendComment,
            onTogglePin = viewModel::onTogglePin,
            onDelete = viewModel::onDelete,
        )
    }
}

@Composable
fun PostDetailScreen(
    state: PostDetailUiState = PostDetailUiState(isLoading = false, detail = previewNoticeDetail()),
    onBack: () -> Unit = {},
    onEdit: (BoardPost) -> Unit = {},
    onToast: (String) -> Unit = {},
    onToggleLike: () -> Unit = {},
    onVote: (Int) -> Unit = {},
    onRevote: () -> Unit = {},
    onApplyRecruit: () -> Unit = {},
    onCommentChange: (String) -> Unit = {},
    onReplyTo: (Comment?) -> Unit = {},
    onSendComment: () -> Unit = {},
    onTogglePin: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    val detail = state.detail
    val clipboard = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val share = rememberShareText()
    var overlay by remember { mutableStateOf<DetailOverlay?>(null) }

    // 링크=웹 이동, 문서=presigned URL 열기(브라우저 다운로드). URL이 없으면 안내 토스트.
    fun openAttachment(att: PostAttachment) {
        val url = when (att) {
            is PostAttachment.Link -> att.url
            is PostAttachment.FileDoc -> att.url
            else -> null
        }
        if (!url.isNullOrBlank()) {
            runCatching { uriHandler.openUri(url) }.onFailure { onToast(DamoimStrings.TOAST_FILE_DOWNLOADED) }
        } else {
            onToast(DamoimStrings.TOAST_FILE_DOWNLOADED)
        }
    }

    // 오버레이는 시스템 뒤로가기로 닫힌다
    PlatformBackHandler(enabled = overlay != null) { overlay = null }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
            if (detail == null) {
                PostDetailSkeleton(onBack)   // 73 로딩 스켈레톤
            } else {
                val isRecruit = detail.post.recruit != null
                DetailHeader(detail.post.category, showRoster = isRecruit, onBack = onBack, onRoster = { overlay = DetailOverlay.Roster }, onMore = { overlay = DetailOverlay.PostMenu })
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    if (isRecruit) {
                        RecruitBody(detail.post, onToggleLike)
                    } else {
                        PostBody(
                            detail.post,
                            onImageTap = { imgs, i -> overlay = DetailOverlay.ImageViewer(imgs, i, "${detail.post.authorName} · ${detail.post.title}") },
                            onOpenAttachment = ::openAttachment,
                            onVote = onVote,
                            onRevote = onRevote,
                            onToggleLike = onToggleLike,
                        )
                    }
                    Box(Modifier.fillMaxWidth().height(8.dp).background(colors.surfaceInput))
                    CommentsSection(
                        detail.comments, detail.post.commentCount,
                        onReply = { onReplyTo(it) },
                        onCommentLongPress = { overlay = DetailOverlay.CommentMenu(it) },
                    )
                    Spacer(Modifier.height(8.dp))
                }
                CommentInputBar(
                    input = state.commentInput,
                    replyTarget = state.replyTarget,
                    sending = state.isSendingComment,
                    applyState = detail.post.recruit,
                    onInputChange = onCommentChange,
                    onCancelReply = { onReplyTo(null) },
                    onSend = onSendComment,
                    onApply = onApplyRecruit,
                )
            }
        }

        // ── 오버레이 (호출 화면 위) ──
        when (val o = overlay) {
            DetailOverlay.PostMenu -> PostMenuSheet(
                post = detail?.post,
                isMyPost = state.isMyPost,
                isLeader = state.isLeader,
                onDismiss = { overlay = null },
                onEdit = { overlay = null; detail?.post?.let(onEdit) },
                onShare = {
                    overlay = null
                    detail?.post?.let { share("[다모임] ${it.title}\nhttps://damoim.app/post/${it.id}") }
                },
                onPin = { overlay = null; onTogglePin() },
                onDelete = { overlay = DetailOverlay.DeleteConfirm },
                onReport = { overlay = DetailOverlay.Report },
            )
            is DetailOverlay.CommentMenu -> CommentMenuSheet(
                comment = o.comment,
                onDismiss = { overlay = null },
                onReply = { overlay = null; onReplyTo(o.comment) },
                onCopy = {
                    overlay = null
                    clipboard.setText(AnnotatedString(o.comment.content))
                    onToast(DamoimStrings.TOAST_COMMENT_COPIED)
                },
                onReport = { overlay = DetailOverlay.Report },
            )
            DetailOverlay.DeleteConfirm -> DeleteConfirmDialog(
                commentCount = detail?.post?.commentCount ?: 0,
                onDismiss = { overlay = null },
                onConfirm = { overlay = null; onDelete() },
            )
            DetailOverlay.Report -> ReportSheet(
                onDismiss = { overlay = null },
                onSubmit = { overlay = null; onToast(DamoimStrings.TOAST_REPORTED) },
            )
            DetailOverlay.Roster -> RosterSheet(
                recruit = detail?.post?.recruit,
                onDismiss = { overlay = null },
            )
            is DetailOverlay.ImageViewer -> ImageViewerOverlay(
                images = o.images, startIndex = o.index, caption = o.caption,
                onClose = { overlay = null },
                onDownload = { onToast(DamoimStrings.TOAST_FILE_DOWNLOADED) },
            )
            null -> {}
        }
    }
}

@Composable
private fun DetailHeader(category: BoardCategory?, showRoster: Boolean, onBack: () -> Unit, onRoster: () -> Unit, onMore: () -> Unit) {
    val colors = DamoimTheme.colors
    val title = when (category) {
        BoardCategory.NOTICE -> DamoimStrings.BOARD_LIST_NOTICE
        BoardCategory.FREE -> DamoimStrings.BOARD_LIST_FREE
        BoardCategory.RECRUIT -> DamoimStrings.BOARD_LIST_RECRUIT
        null -> ""
    }
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderIconButton(onBack) { BackChevronIcon(tint = colors.textPrimary, modifier = Modifier.size(24.dp)) }
            // 목록 헤더와 동일한 titleMedium(17sp)으로 통일
            Text(title, style = DamoimTheme.typography.titleMedium, color = colors.textPrimary, modifier = Modifier.weight(1f).padding(start = 4.dp))
            if (showRoster) HeaderIconButton(onRoster) { ListIcon(tint = colors.textMuted, modifier = Modifier.size(20.dp)) }
            HeaderIconButton(onMore) { MoreIcon(tint = colors.textMuted, modifier = Modifier.size(20.dp)) }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
    }
}

@Composable
private fun HeaderIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(40.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { content() }
}

// ── 일반 게시글 본문(14/36) ──
@Composable
private fun PostBody(
    post: BoardPost,
    onImageTap: (List<String>, Int) -> Unit,
    onOpenAttachment: (PostAttachment) -> Unit,
    onVote: (Int) -> Unit,
    onRevote: () -> Unit,
    onToggleLike: () -> Unit,
) {
    val colors = DamoimTheme.colors
    val imageUrls = post.attachments.filterIsInstance<PostAttachment.Image>().mapNotNull { it.url }
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 뱃지 행
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (post.isPinned) SolidBadge(DamoimStrings.BOARD_PINNED, bg = colors.primary, horizontalPadding = 10.dp, verticalPadding = 4.dp)
            CategoryBadge(post.category, horizontalPadding = 10.dp, verticalPadding = 4.dp)
            if (post.poll != null) {
                Row(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.primary).padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    ChartIcon(tint = colors.onPrimary, modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(DamoimStrings.POLL_LABEL, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold), color = colors.onPrimary)
                }
            }
        }
        Text(post.title, style = DamoimTheme.typography.titleLarge, color = colors.textPrimary)
        AuthorRow(post)
        post.poll?.let { PollBlock(it, onVote, onRevote) }
        if (post.content.isNotEmpty()) {
            Text(post.content, style = DamoimTheme.typography.body.copy(fontSize = 14.5.sp, lineHeight = 24.sp), color = colors.textSecondary)
        }
        post.attachments.forEach { att ->
            when (att) {
                is PostAttachment.Image -> {
                    val idx = att.url?.let { imageUrls.indexOf(it) }?.coerceAtLeast(0) ?: 0
                    NetworkImage(
                        url = att.url,
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onImageTap(imageUrls, idx) },
                        cornerRadius = 14.dp,
                    )
                }
                else -> AttachmentCard(att) { onOpenAttachment(att) }
            }
        }
        ReactionRow(post, onToggleLike)
    }
}

// ── 모집 글 상세(84) 본문 ──
@Composable
private fun RecruitBody(post: BoardPost, onToggleLike: () -> Unit) {
    val colors = DamoimTheme.colors
    val recruit = post.recruit ?: return
    Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SolidBadge(if (recruit.status == RecruitStatus.OPEN) DamoimStrings.RECRUIT_OPEN else DamoimStrings.RECRUIT_CLOSED, bg = if (recruit.status == RecruitStatus.OPEN) colors.primary else colors.surfaceDim, textColor = if (recruit.status == RecruitStatus.OPEN) colors.onPrimary else colors.textMuted, horizontalPadding = 10.dp, verticalPadding = 4.dp)
            Text(DamoimStrings.BOARD_LIST_RECRUIT, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold), color = colors.primaryDark, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.primaryContainer).padding(horizontal = 10.dp, vertical = 4.dp))
        }
        Text(post.title, style = DamoimTheme.typography.titleLarge, color = colors.textPrimary)
        AuthorRow(post)
        RecruitHeroCard(recruit)
        RecruitInfoBox(recruit)
        if (post.content.isNotEmpty()) {
            Text(post.content, style = DamoimTheme.typography.body.copy(fontSize = 14.5.sp, lineHeight = 24.sp), color = colors.textSecondary)
        }
        if (recruit.current > 0) ApplicantStack(recruit)
        ReactionRow(post, onToggleLike)
    }
}

@Composable
private fun RecruitHeroCard(recruit: RecruitInfo) {
    val colors = DamoimTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(colors.primaryContainer.copy(alpha = 0.35f)).border(1.5.dp, colors.primary, RoundedCornerShape(18.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(DamoimStrings.RECRUIT_STATUS, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold), color = colors.primaryDeep, modifier = Modifier.weight(1f))
            if (recruit.status == RecruitStatus.OPEN && recruit.dday != null) {
                Row(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.error).padding(horizontal = 11.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                    ClockIcon(tint = colors.onPrimary, modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(DamoimStrings.recruitDeadlineBadge(recruit.dday), style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold), color = colors.onPrimary)
                }
            } else if (recruit.status == RecruitStatus.CLOSED) {
                SolidBadge(DamoimStrings.RECRUIT_CLOSED, bg = colors.surfaceDim, textColor = colors.textMuted)
            }
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text("${recruit.current}", style = DamoimTheme.typography.display.copy(fontSize = 38.sp, lineHeight = 38.sp), color = colors.primaryDark)
            Spacer(Modifier.width(8.dp))
            Text(DamoimStrings.recruitAppliedSuffix(recruit.capacity), style = DamoimTheme.typography.titleMedium.copy(fontSize = 16.sp), color = colors.textMuted, modifier = Modifier.padding(bottom = 5.dp))
            Spacer(Modifier.weight(1f))
            Text(DamoimStrings.recruitRemaining(recruit.remaining), style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold), color = colors.primaryDark, modifier = Modifier.padding(bottom = 5.dp))
        }
        Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(99.dp)).background(colors.divider)) {
            Box(Modifier.fillMaxWidth(recruit.percent / 100f).height(10.dp).clip(RoundedCornerShape(99.dp)).background(colors.primary))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(DamoimStrings.RECRUIT_AUTO_CLOSE, style = DamoimTheme.typography.label.copy(fontSize = 11.5.sp), color = colors.textMuted, modifier = Modifier.weight(1f))
            Text("${recruit.percent}%", style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp), color = colors.textTertiary)
        }
    }
}

/** 모집 정보(마감/방식). ※모집 대상 필드는 작성에서 설정할 수 없어 제거됨. */
@Composable
private fun RecruitInfoBox(recruit: RecruitInfo) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, colors.dividerLight, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp)) {
        RecruitInfoRow(DamoimStrings.RECRUIT_INFO_DEADLINE, recruit.deadlineLabel.orEmpty(), showDivider = true) { CalendarIcon(colors.textDisabled, Modifier.size(17.dp)) }
        RecruitInfoRow(DamoimStrings.RECRUIT_INFO_METHOD, recruit.method.orEmpty(), showDivider = false) { ChartIcon(colors.textDisabled, Modifier.size(17.dp)) }
    }
}

@Composable
private fun RecruitInfoRow(label: String, value: String, showDivider: Boolean, icon: @Composable () -> Unit) {
    val colors = DamoimTheme.colors
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            icon()
            Text(label, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.5.sp), color = colors.textMuted, modifier = Modifier.weight(1f))
            Text(value, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp), color = colors.textPrimary)
        }
        if (showDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
    }
}

@Composable
private fun ApplicantStack(recruit: RecruitInfo) {
    val colors = DamoimTheme.colors
    val shown = recruit.applicants.take(3)
    val extra = (recruit.current - shown.size).coerceAtLeast(0)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy((-9).dp)) {
            shown.forEach { a -> RingAvatar(a.initials, a.colorIndex) }
            if (extra > 0) {
                Box(Modifier.size(30.dp).clip(CircleShape).background(colors.surface).padding(2.dp).clip(CircleShape).background(Color(0xFFEEF1F7)), contentAlignment = Alignment.Center) {
                    Text("+$extra", style = DamoimTheme.typography.labelSmall, color = colors.textMuted)
                }
            }
        }
        val firstName = recruit.applicants.firstOrNull()?.initials ?: "부원"
        Text(
            DamoimStrings.recruitApplicantSummary(firstName, (recruit.current - 1).coerceAtLeast(0)),
            style = DamoimTheme.typography.caption,
            color = colors.textMuted,
        )
    }
}

@Composable
private fun RingAvatar(initials: String, colorIndex: Int) {
    val colors = DamoimTheme.colors
    val (bg, fg) = when (colorIndex % 3) {
        0 -> colors.primaryContainerHigh to colors.primaryDeep
        1 -> Color(0xFFBFE0F2) to Color(0xFF1E6C93)
        else -> Color(0xFFC6E7CB) to Color(0xFF2E7D4A)
    }
    Box(Modifier.size(30.dp).clip(CircleShape).background(colors.surface).padding(2.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
        Text(initials, style = DamoimTheme.typography.labelSmall, color = fg)
    }
}

@Composable
private fun AuthorRow(post: BoardPost) {
    val colors = DamoimTheme.colors
    val bordered = post.poll == null
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = if (bordered) 14.dp else 0.dp)) {
            InitialAvatar(post.authorInitials, size = 38.dp)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(post.authorName, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                    if (post.isAuthorLeader) CrownIcon(tint = colors.primary, modifier = Modifier.size(12.dp))
                }
                val meta = buildString {
                    append(post.dateLabel ?: post.timeLabel)
                    if (post.viewCount > 0) append(" · ${DamoimStrings.viewCountLabel(post.viewCount)}")
                }
                Text(meta, style = DamoimTheme.typography.label, color = colors.textDisabled)
            }
        }
        if (bordered) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
    }
}

// ── 투표(36) — 참여 전엔 선택 UI, 참여 후엔 결과 막대 ──
@Composable
private fun PollBlock(poll: Poll, onVote: (Int) -> Unit, onRevote: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).border(1.dp, colors.divider, RoundedCornerShape(18.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChartIcon(tint = colors.primaryDeep, modifier = Modifier.size(14.dp))
                Text(DamoimStrings.pollMeta(poll.anonymous, poll.multiSelect), style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold), color = colors.primaryDeep)
            }
            Text(poll.deadlineLabel, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold), color = colors.error)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            poll.options.forEachIndexed { index, option ->
                PollOptionBar(
                    label = option.label,
                    percent = poll.percentOf(index),
                    selected = index in poll.myVotes,
                    showResult = poll.hasVoted,
                    onClick = { onVote(index) },
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            val myVoteLabel = poll.myVotes.firstOrNull()?.let { poll.options.getOrNull(it)?.label }
            Text(
                if (poll.hasVoted) DamoimStrings.pollParticipation(poll.totalVotes, myVoteLabel) else DamoimStrings.POLL_TAP_TO_VOTE,
                style = DamoimTheme.typography.caption, color = colors.textMuted,
                modifier = Modifier.weight(1f), maxLines = 1,
            )
            if (poll.hasVoted) {
                Text(
                    DamoimStrings.POLL_REVOTE,
                    style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = colors.primary,
                    modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onRevote),
                )
            }
        }
    }
}

@Composable
private fun PollOptionBar(label: String, percent: Int, selected: Boolean, showResult: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    val fillColor = if (selected) colors.primaryContainer else colors.surfaceVariant
    val fraction = if (showResult) (percent / 100f).coerceIn(0f, 1f) else 0f
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .border(if (selected) 1.5.dp else 1.dp, if (selected) colors.primary else colors.divider, RoundedCornerShape(12.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().drawBehind { drawRect(color = fillColor, size = size.copy(width = size.width * fraction)) }.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (selected) CheckIcon(tint = colors.primary, modifier = Modifier.size(16.dp))
            Text(label, style = DamoimTheme.typography.body.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold), color = if (selected) colors.textPrimary else colors.textSecondary, modifier = Modifier.weight(1f))
            if (showResult) {
                Text("$percent%", style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold), color = if (selected) colors.primaryDark else colors.textMuted)
            }
        }
    }
}

@Composable
private fun AttachmentCard(att: PostAttachment, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    when (att) {
        is PostAttachment.FileDoc -> Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).border(1.dp, colors.divider, RoundedCornerShape(14.dp)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick).padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(colors.primaryContainer), contentAlignment = Alignment.Center) { PaperclipIcon(tint = colors.primary, modifier = Modifier.size(17.dp)) }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(att.name, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                Text(att.size, style = DamoimTheme.typography.label, color = colors.textDisabled)
            }
            DownloadIcon(tint = colors.textMuted, modifier = Modifier.size(18.dp))
        }
        is PostAttachment.Link -> Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceInput).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinkIcon(tint = colors.textMuted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(att.title, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                Text(att.domain, style = DamoimTheme.typography.caption, color = colors.textMuted)
            }
        }
        is PostAttachment.Image -> Unit  // 이미지는 PostBody에서 별도 처리
    }
}

@Composable
private fun ReactionRow(post: BoardPost, onToggleLike: () -> Unit) {
    val colors = DamoimTheme.colors
    val likeTint = if (post.likedByMe) colors.error else colors.textTertiary
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onToggleLike),
        ) {
            HeartIcon(tint = likeTint, modifier = Modifier.size(16.dp))
            Text(DamoimStrings.likeCountLabel(post.likeCount), style = DamoimTheme.typography.bodySmall.copy(fontWeight = if (post.likedByMe) FontWeight.Bold else FontWeight.SemiBold), color = likeTint)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            CommentIcon(tint = colors.textTertiary, modifier = Modifier.size(16.dp))
            Text(DamoimStrings.commentCountLabel(post.commentCount), style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = colors.textTertiary)
        }
    }
}

@Composable
private fun CommentsSection(comments: List<Comment>, commentCount: Int, onReply: (Comment) -> Unit, onCommentLongPress: (Comment) -> Unit) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(DamoimStrings.commentSectionHeader(commentCount), style = DamoimTheme.typography.bodyStrong.copy(fontWeight = FontWeight.ExtraBold), color = colors.textPrimary)
        comments.forEach { CommentItem(it, onReply = { onReply(it) }, onLongPress = { onCommentLongPress(it) }) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CommentItem(comment: Comment, onReply: () -> Unit, onLongPress: () -> Unit) {
    val colors = DamoimTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = if (comment.isReply) 42.dp else 0.dp)
            .combinedClickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {}, onLongClick = onLongPress),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        InitialAvatar(comment.authorInitials, size = 32.dp, fontSize = 10.sp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(comment.authorName, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                if (comment.isAuthor) Text(DamoimStrings.BOARD_AUTHOR_BADGE, style = DamoimTheme.typography.labelSmall, color = colors.primaryDark, modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(colors.primaryContainer).padding(horizontal = 6.dp, vertical = 2.dp))
                else Text(comment.timeLabel, style = DamoimTheme.typography.label, color = colors.textDisabled)
            }
            Text(comment.content, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textSecondary)
            if (!comment.isReply) {
                Text(
                    DamoimStrings.BOARD_REPLY,
                    style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.SemiBold), color = colors.textMuted,
                    modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onReply),
                )
            }
        }
    }
}

/** 하단 입력바 — 실제 댓글 입력 + 전송. 모집글이면 우측에 신청하기 버튼이 추가된다. */
@Composable
private fun CommentInputBar(
    input: String,
    replyTarget: Comment?,
    sending: Boolean,
    applyState: RecruitInfo?,
    onInputChange: (String) -> Unit,
    onCancelReply: () -> Unit,
    onSend: () -> Unit,
    onApply: () -> Unit,
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().background(colors.surface)) {
        // 답글 대상 표시 칩
        if (replyTarget != null) {
            Row(
                Modifier.fillMaxWidth().background(colors.surfaceInput).padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(DamoimStrings.replyingTo(replyTarget.authorName), style = DamoimTheme.typography.caption, color = colors.textTertiary, modifier = Modifier.weight(1f))
                Box(Modifier.size(20.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCancelReply), contentAlignment = Alignment.Center) {
                    CloseIcon(colors.textMuted, Modifier.size(12.dp))
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(Modifier.weight(1f).clip(RoundedCornerShape(999.dp)).background(colors.surfaceVariant).padding(horizontal = 18.dp, vertical = 12.dp)) {
                if (input.isEmpty()) Text(DamoimStrings.BOARD_COMMENT_HINT, style = DamoimTheme.typography.body, color = colors.textDisabled)
                BasicTextField(
                    value = input,
                    onValueChange = onInputChange,
                    textStyle = DamoimTheme.typography.body.copy(color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.primary),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )
            }
            if (applyState != null && input.isEmpty()) {
                // 모집(84): 입력 중이 아니면 신청하기 버튼
                val applied = applyState.appliedByMe
                val closed = applyState.status == RecruitStatus.CLOSED && !applied
                Box(
                    Modifier.clip(RoundedCornerShape(14.dp))
                        .background(if (applied || closed) colors.surfaceVariant else colors.primary)
                        .clickable(enabled = !applied && !closed, interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onApply)
                        .padding(horizontal = 22.dp, vertical = 13.dp),
                ) {
                    Text(
                        if (applied) DamoimStrings.RECRUIT_APPLIED_BUTTON else DamoimStrings.RECRUIT_APPLY,
                        style = DamoimTheme.typography.button.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (applied || closed) colors.textMuted else colors.onPrimary,
                    )
                }
            } else {
                val canSend = input.isNotBlank() && !sending
                Box(
                    Modifier.size(40.dp).clip(CircleShape)
                        .background(if (canSend) colors.primary else colors.primary.copy(alpha = 0.4f))
                        .clickable(enabled = canSend, interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onSend),
                    contentAlignment = Alignment.Center,
                ) { SendIcon(tint = colors.onPrimary, modifier = Modifier.size(17.dp)) }
            }
        }
    }
}

/** 79 없는/삭제된 게시글 오류. */
@Composable
fun PostErrorScreen(onBack: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 16.dp, bottom = 12.dp)) {
            HeaderIconButton(onBack) { BackChevronIcon(tint = colors.textPrimary, modifier = Modifier.size(24.dp)) }
        }
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(Modifier.size(84.dp).clip(RoundedCornerShape(26.dp)).background(colors.surfaceVariant))
            Spacer(Modifier.height(20.dp))
            Text(DamoimStrings.POST_ERROR_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 19.sp), color = colors.textPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(DamoimStrings.POST_ERROR_SUBTITLE, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Text(DamoimStrings.POST_ERROR_BACK, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.Bold), color = colors.textSecondary, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(colors.surfaceVariant).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onBack).padding(horizontal = 20.dp, vertical = 13.dp))
        }
    }
}

// ── 프리뷰 ──
internal fun previewNoticeDetail(): PostDetail = PostDetail(
    post = BoardPost(id = 101, category = BoardCategory.NOTICE, title = "신입 회원 환영 OT 일정 안내", content = "안녕하세요, 회장 김민준입니다.\n\n6월 14일(토) 신입 회원 환영 OT를 진행합니다.", authorId = 2001, authorName = "김민준", authorInitials = "민준", dateLabel = "2025.06.01", timeLabel = "6.01", viewCount = 128, likeCount = 14, commentCount = 3, isPinned = true, isAuthorLeader = true, readRate = 82, attachments = listOf(PostAttachment.FileDoc("OT_일정표.pdf", "1.2MB"))),
    comments = listOf(Comment(1, "이서연", "서연", "1시간 전", "참석합니다! 신입분들 환영해요 🎉"), Comment(3, "김민준", "민준", "35분 전", "네! 줌 링크 공유드릴게요.", isReply = true, isAuthor = true, parentId = 1)),
)

@Preview
@Composable
private fun PostDetailNoticePreview() {
    DamoimTheme { PostDetailScreen(state = PostDetailUiState(isLoading = false, detail = previewNoticeDetail())) }
}
