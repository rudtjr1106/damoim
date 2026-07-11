package com.damoim.app.presentation.board.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.BoardPost
import com.damoim.app.domain.model.RecruitStatus
import com.damoim.app.presentation.board.CategoryBadge
import com.damoim.app.presentation.board.SkeletonBlock
import com.damoim.app.presentation.board.SolidBadge
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.ChevronDownIcon
import com.damoim.app.presentation.component.SearchIcon
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 11 자유 / 12 공지 / 13 모집 게시판 목록 — Route.
 * 카테고리에 따라 본문 레이아웃이 달라진다(자유=리스트, 공지/모집=카드).
 */
@Composable
fun BoardListRoute(
    category: BoardCategory,
    viewModel: BoardListViewModel = viewModel(key = "boardlist_${category.name}") {
        BoardListViewModel(AppGraph.getBoardPostsUseCase, category)
    },
    onBack: () -> Unit = {},
    onOpenPost: (Long) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    BoardListScreen(
        state = state,
        onBack = onBack,
        onOpenPost = onOpenPost,
        onSort = viewModel::onSort,
        onToggleOpenOnly = viewModel::onToggleOpenOnly,
    )
}

@Composable
fun BoardListScreen(
    state: BoardListUiState = BoardListUiState(BoardCategory.FREE, isLoading = false, posts = previewFreePosts()),
    onBack: () -> Unit = {},
    onOpenPost: (Long) -> Unit = {},
    onSort: (BoardSort) -> Unit = {},
    onToggleOpenOnly: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    val cardLayout = state.category != BoardCategory.FREE
    // 루트(=상태바 뒤 영역)는 헤더와 같은 흰색으로 두고, 목록 영역만 회색 배경을 준다
    Column(
        modifier = Modifier.fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding(),
    ) {
        ListHeader(state.category, state.recruitOpenOnly, onBack, onToggleOpenOnly)
        Column(
            Modifier.weight(1f)
                .fillMaxWidth()
                .background(if (cardLayout) colors.surfaceInput else colors.surface)
                .verticalScroll(rememberScrollState()),
        ) {
            if (state.isLoading) {
                BoardListSkeleton()
            } else {
                when (state.category) {
                    BoardCategory.FREE -> FreeBody(state.displayed, state.sort, onOpenPost, onSort)
                    BoardCategory.NOTICE -> NoticeBody(state.displayed, onOpenPost)
                    BoardCategory.RECRUIT -> RecruitBody(state.displayed, onOpenPost)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ListHeader(category: BoardCategory, recruitOpenOnly: Boolean, onBack: () -> Unit, onToggleOpenOnly: () -> Unit) {
    val colors = DamoimTheme.colors
    val title = when (category) {
        BoardCategory.FREE -> DamoimStrings.BOARD_LIST_FREE
        BoardCategory.NOTICE -> DamoimStrings.BOARD_LIST_NOTICE
        BoardCategory.RECRUIT -> DamoimStrings.BOARD_LIST_RECRUIT
    }
    // 공지/모집은 흰 헤더 + 하단 구분선, 자유는 경계선 없음
    val bordered = category != BoardCategory.FREE
    Column(Modifier.fillMaxWidth().background(colors.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onBack,
                ),
                contentAlignment = Alignment.Center,
            ) { BackChevronIcon(tint = colors.textPrimary, modifier = Modifier.size(24.dp)) }
            Text(title, style = DamoimTheme.typography.titleMedium, color = colors.textPrimary, modifier = Modifier.weight(1f))
            if (category == BoardCategory.RECRUIT) {
                // 모집중만 필터 토글(실동작)
                Text(
                    DamoimStrings.BOARD_RECRUIT_ONLY,
                    style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = if (recruitOpenOnly) colors.primary else colors.textDisabled,
                    modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onToggleOpenOnly),
                )
            }
        }
        if (bordered) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
    }
}

// ── 자유(11) ── (검색은 게시판 홈 상단에서만 — 목록 내 검색바 제거)
@Composable
private fun FreeBody(posts: List<BoardPost>, sort: BoardSort, onOpenPost: (Long) -> Unit, onSort: (BoardSort) -> Unit) {
    Spacer(Modifier.height(8.dp))
    // 정렬 칩(실동작)
    SortChips(sort, onSort)
    // 목록
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        posts.forEachIndexed { index, post ->
            FreeRow(post, isLast = index == posts.lastIndex) { onOpenPost(post.id) }
        }
    }
}

@Composable
private fun SortChips(sort: BoardSort, onSort: (BoardSort) -> Unit) {
    val colors = DamoimTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SortChip(DamoimStrings.BOARD_SORT_RECENT, active = sort == BoardSort.RECENT) { onSort(BoardSort.RECENT) }
            SortChip(DamoimStrings.BOARD_SORT_POPULAR, active = sort == BoardSort.POPULAR) { onSort(BoardSort.POPULAR) }
            SortChip(DamoimStrings.BOARD_SORT_COMMENTS, active = sort == BoardSort.COMMENTS) { onSort(BoardSort.COMMENTS) }
            // 기간 드롭다운 칩 (기간 필터는 일정 도메인 붙일 때 함께)
            Row(
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(DamoimStrings.BOARD_SORT_PERIOD, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold), color = colors.textTertiary)
                Spacer(Modifier.width(4.dp))
                ChevronDownIcon(tint = colors.textTertiary, modifier = Modifier.size(11.dp))
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
    }
}

@Composable
private fun SortChip(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Text(
        label,
        style = DamoimTheme.typography.caption.copy(fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold),
        color = if (active) colors.surface else colors.textTertiary,
        modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(if (active) colors.textPrimary else colors.surfaceVariant)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    )
}

@Composable
private fun FreeRow(post: BoardPost, isLast: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(post.title, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = colors.textPrimary)
            if (post.preview.isNotEmpty()) {
                Text(post.preview, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                val gisu = post.authorGisu
                val who = if (gisu != null) DamoimStrings.boardAuthorGisu(post.authorName, gisu) else post.authorName
                Text(who, style = DamoimTheme.typography.caption, color = colors.textTertiary)
                Text(post.timeLabel, style = DamoimTheme.typography.caption, color = colors.textDisabled)
                Text(DamoimStrings.commentCountLabel(post.commentCount), style = DamoimTheme.typography.caption, color = colors.textDisabled)
            }
        }
        val thumbLabel = post.attachments.filterIsInstance<com.damoim.app.domain.model.PostAttachment.Image>().firstOrNull()?.label
        if (thumbLabel != null) {
            com.damoim.app.presentation.component.AttachedImage(thumbLabel, Modifier.size(64.dp), cornerRadius = 12.dp)
        }
    }
    if (!isLast) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
}

// ── 공지(12) ──
@Composable
private fun NoticeBody(posts: List<BoardPost>, onOpenPost: (Long) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        posts.forEach { post -> NoticeCard(post) { onOpenPost(post.id) } }
    }
}

@Composable
private fun NoticeCard(post: BoardPost, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    val pinned = post.isPinned
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .then(if (pinned) Modifier.border(1.5.dp, colors.primary, RoundedCornerShape(18.dp)) else Modifier)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (pinned) SolidBadge(DamoimStrings.BOARD_PINNED, bg = colors.primary, leadingMegaphone = true)
            else CategoryBadge(post.category, horizontalPadding = 10.dp, verticalPadding = 4.dp)
            Text(post.timeLabel, style = DamoimTheme.typography.label, color = colors.textDisabled)
        }
        Text(post.title, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp), color = colors.textPrimary)
        if (post.preview.isNotEmpty()) {
            Text(post.preview, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(DamoimStrings.viewCountLabel(post.viewCount), style = DamoimTheme.typography.caption, color = colors.textDisabled)
            Text(DamoimStrings.commentCountLabel(post.commentCount), style = DamoimTheme.typography.caption, color = colors.textDisabled)
            val readRate = post.readRate
            if (pinned && readRate != null) {
                Text(
                    DamoimStrings.readRateLabel(readRate),
                    style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                    color = colors.primaryDark,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                )
            }
        }
    }
}

// ── 모집(13) ──
@Composable
private fun RecruitBody(posts: List<BoardPost>, onOpenPost: (Long) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        posts.forEach { post -> RecruitCard(post) { onOpenPost(post.id) } }
    }
}

@Composable
private fun RecruitCard(post: BoardPost, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    val recruit = post.recruit ?: return
    val closed = recruit.status == RecruitStatus.CLOSED
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)).background(colors.surface)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .alpha(if (closed) 0.6f else 1f)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (closed) {
                SolidBadge(DamoimStrings.RECRUIT_CLOSED, bg = colors.surfaceDim, textColor = colors.textMuted)
            } else {
                SolidBadge(DamoimStrings.RECRUIT_OPEN, bg = colors.primary)
                if (recruit.dday != null) {
                    Text(recruit.dday, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold), color = colors.primaryDark)
                }
            }
            Text(
                "${post.authorName} · ${post.timeLabel}",
                style = DamoimTheme.typography.label,
                color = colors.textDisabled,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
            )
        }
        Text(post.title, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp), color = colors.textPrimary)
        if (post.preview.isNotEmpty() && !closed) {
            Text(post.preview, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        RecruitProgress(recruit.current, recruit.capacity, closed)
    }
}

@Composable
private fun RecruitProgress(current: Int, capacity: Int, closed: Boolean) {
    val colors = DamoimTheme.colors
    val fraction = if (capacity == 0) 0f else (current.toFloat() / capacity).coerceIn(0f, 1f)
    val barColor = if (closed) colors.textDisabled else colors.primary
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(99.dp)).background(colors.surfaceDim)) {
            Box(Modifier.fillMaxWidth(fraction).height(6.dp).clip(RoundedCornerShape(99.dp)).background(barColor))
        }
        Text(
            DamoimStrings.recruitProgress(current, capacity),
            style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
            color = if (closed) colors.textMuted else colors.textTertiary,
        )
    }
}

// ── 72 로딩 스켈레톤 ──
@Composable
private fun BoardListSkeleton() {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBlock(Modifier.width(54.dp), height = 30.dp, radius = 999.dp)
            SkeletonBlock(Modifier.width(66.dp), height = 30.dp, radius = 999.dp)
            SkeletonBlock(Modifier.width(60.dp), height = 30.dp, radius = 999.dp)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            repeat(4) { i ->
                Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonBlock(Modifier.fillMaxWidth(0.8f), height = 15.dp)
                    SkeletonBlock(Modifier.fillMaxWidth(), height = 11.dp)
                    SkeletonBlock(Modifier.fillMaxWidth(0.55f), height = 11.dp)
                }
                if (i != 3) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
            }
        }
    }
}

// ── 프리뷰 ──
internal fun previewFreePosts() = listOf(
    BoardPost(201, BoardCategory.FREE, "동아리 MT 후기 공유해요", preview = "이번 MT 진짜 재밌었는데 다들 사진 올려주세요! 특히 둘째 날 게임할 때 찍은 사진들이요", authorName = "이서연", authorInitials = "서연", authorGisu = "24기", timeLabel = "10분 전", commentCount = 12, hasThumbnail = true),
    BoardPost(202, BoardCategory.FREE, "이번 주 모임 시간 변경 가능한가요?", preview = "목요일에 시험이 있어서 금요일로 옮길 수 있을지 궁금합니다", authorName = "박준혁", authorInitials = "준혁", authorGisu = "26기", timeLabel = "어제", commentCount = 5),
    BoardPost(203, BoardCategory.FREE, "신입 환영 파티 사진 올립니다", authorName = "최유진", authorInitials = "유진", authorGisu = "23기", timeLabel = "2일 전", commentCount = 18),
)

@Preview
@Composable
private fun BoardListFreePreview() {
    DamoimTheme { BoardListScreen(state = BoardListUiState(BoardCategory.FREE, isLoading = false, posts = previewFreePosts())) }
}

@Preview
@Composable
private fun BoardListRecruitPreview() {
    DamoimTheme {
        BoardListScreen(state = BoardListUiState(BoardCategory.RECRUIT, isLoading = false, posts = listOf(
            BoardPost(301, BoardCategory.RECRUIT, "2025 하반기 신입 부원 모집", preview = "함께 성장할 신입 부원을 모집합니다. 백엔드/프론트 관심자 환영!", authorName = "김민준", authorInitials = "민준", timeLabel = "2시간 전", recruit = com.damoim.app.domain.model.RecruitInfo(RecruitStatus.OPEN, "D-7", 12, 20)),
            BoardPost(303, BoardCategory.RECRUIT, "사이드 프로젝트 디자이너 모집", authorName = "정하늘", authorInitials = "하늘", timeLabel = "5.22", recruit = com.damoim.app.domain.model.RecruitInfo(RecruitStatus.CLOSED, null, 2, 2)),
        )))
    }
}
