package com.damoim.app.presentation.board.home

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.BoardPost
import com.damoim.app.domain.model.RecruitStatus
import com.damoim.app.domain.repository.BoardHomeData
import com.damoim.app.presentation.board.CategoryBadge
import com.damoim.app.presentation.board.SolidBadge
import com.damoim.app.presentation.component.BottomNavBar
import com.damoim.app.presentation.component.CommentIcon
import com.damoim.app.presentation.component.HeartIcon
import com.damoim.app.presentation.component.MainTab
import com.damoim.app.presentation.component.MegaphoneIcon
import com.damoim.app.presentation.component.DamoimFab
import com.damoim.app.presentation.component.PlusIcon
import com.damoim.app.presentation.component.SearchIcon
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 10 게시판 홈 — Route. 게시판 탭의 루트. 카테고리 탭 → 목록(11/12/13),
 * 글/배너 탭 → 상세(14), FAB → 작성(15), 검색 → 검색(40). 미구현 탭은 [onComingSoon].
 */
@Composable
fun BoardHomeRoute(
    viewModel: BoardHomeViewModel = viewModel { BoardHomeViewModel(AppGraph.getBoardHomeUseCase) },
    onOpenPost: (Long) -> Unit = {},
    onOpenCategory: (BoardCategory) -> Unit = {},
    onSearch: () -> Unit = {},
    onWrite: () -> Unit = {},
    onTabSelect: (MainTab) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    BoardHomeScreen(
        state = state,
        onOpenPost = onOpenPost,
        onOpenCategory = onOpenCategory,
        onSearch = onSearch,
        onWrite = onWrite,
        onTabSelect = onTabSelect,
    )
}

@Composable
fun BoardHomeScreen(
    state: BoardHomeUiState = BoardHomeUiState(isLoading = false, home = previewBoardHome()),
    onOpenPost: (Long) -> Unit = {},
    onOpenCategory: (BoardCategory) -> Unit = {},
    onSearch: () -> Unit = {},
    onWrite: () -> Unit = {},
    onTabSelect: (MainTab) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(modifier = Modifier.fillMaxSize().background(colors.surface)) {
        Box(Modifier.weight(1f)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                BoardHomeHeader(onSearch)
                CategoryFilterRow(selected = null, onSelect = onOpenCategory)
                val home = state.home
                if (home != null) {
                    if (state.isEmpty) {
                        BoardEmpty(onWrite)
                    } else {
                        PinnedBanners(home.pinned, onOpenPost)
                        FeedList(home.recent, onOpenPost)
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
            if (state.home != null && !state.isEmpty) {
                WriteFab(onWrite, Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 20.dp))
            }
        }
        BottomNavBar(selected = MainTab.BOARD, onSelect = onTabSelect)
    }
}

@Composable
private fun BoardHomeHeader(onSearch: () -> Unit) {
    val colors = DamoimTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(DamoimStrings.BOARD_TITLE, style = DamoimTheme.typography.headline, color = colors.textPrimary, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onSearch,
            ),
            contentAlignment = Alignment.Center,
        ) { SearchIcon(tint = colors.textSecondary, modifier = Modifier.size(22.dp)) }
    }
}

/** 카테고리 필터 칩 행. selected=null이면 '전체' 활성. */
@Composable
private fun CategoryFilterRow(selected: BoardCategory?, onSelect: (BoardCategory) -> Unit) {
    val colors = DamoimTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().background(colors.surface).padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(DamoimStrings.BOARD_FILTER_ALL, active = selected == null) { /* 전체 = 현재 화면 */ }
        FilterChip(DamoimStrings.BOARD_NOTICE, active = selected == BoardCategory.NOTICE) { onSelect(BoardCategory.NOTICE) }
        FilterChip(DamoimStrings.BOARD_FREE, active = selected == BoardCategory.FREE) { onSelect(BoardCategory.FREE) }
        FilterChip(DamoimStrings.BOARD_RECRUIT, active = selected == BoardCategory.RECRUIT) { onSelect(BoardCategory.RECRUIT) }
    }
}

@Composable
private fun FilterChip(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Text(
        label,
        style = DamoimTheme.typography.bodySmall.copy(fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold),
        color = if (active) colors.surface else colors.textTertiary,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) colors.textPrimary else colors.surfaceVariant)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun PinnedBanners(pinned: List<BoardPost>, onOpenPost: (Long) -> Unit) {
    if (pinned.isEmpty()) return
    val colors = DamoimTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        pinned.forEach { post ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)).background(colors.primaryContainer)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onOpenPost(post.id) }
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MegaphoneIcon(tint = colors.primaryDark, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    post.title,
                    style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.primaryDeep,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(10.dp))
                Text(DamoimStrings.BOARD_PINNED, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold), color = colors.primaryDark)
            }
        }
    }
}

@Composable
private fun FeedList(posts: List<BoardPost>, onOpenPost: (Long) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        posts.forEachIndexed { index, post ->
            FeedRow(post, isLast = index == posts.lastIndex, onClick = { onOpenPost(post.id) })
        }
    }
}

@Composable
private fun FeedRow(post: BoardPost, isLast: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryBadge(post.category)
            val recruit = post.recruit
            if (recruit != null && recruit.status == RecruitStatus.OPEN) {
                SolidBadge(DamoimStrings.RECRUIT_OPEN, bg = colors.primary, horizontalPadding = 9.dp, verticalPadding = 3.dp)
            }
            Text(post.timeLabelShort(), style = DamoimTheme.typography.label, color = colors.textDisabled)
        }
        Text(post.title, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = colors.textPrimary)
        if (post.preview.isNotEmpty()) {
            Text(post.preview, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(post.authorName, style = DamoimTheme.typography.caption, color = colors.textTertiary)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CommentIcon(tint = colors.textDisabled, modifier = Modifier.size(13.dp))
                Text(post.commentCount.toString(), style = DamoimTheme.typography.caption, color = colors.textDisabled)
            }
            if (post.likeCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    HeartIcon(tint = colors.textDisabled, modifier = Modifier.size(13.dp))
                    Text(post.likeCount.toString(), style = DamoimTheme.typography.caption, color = colors.textDisabled)
                }
            }
        }
    }
    if (!isLast) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
}

@Composable
private fun WriteFab(onClick: () -> Unit, modifier: Modifier = Modifier) =
    DamoimFab(onClick, modifier)

/** 41 게시판 빈 상태 (홈 피드 영역 대체). */
@Composable
private fun BoardEmpty(onWrite: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(96.dp).clip(RoundedCornerShape(28.dp)).background(colors.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) { MegaphoneIcon(tint = colors.textDisabled, modifier = Modifier.size(40.dp)) }
        Spacer(Modifier.height(20.dp))
        Text(DamoimStrings.BOARD_EMPTY_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp), color = colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text(DamoimStrings.BOARD_EMPTY_SUBTITLE, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(22.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp)).background(colors.primary)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onWrite)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlusIcon(tint = colors.onPrimary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(DamoimStrings.BOARD_EMPTY_CTA, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.onPrimary)
        }
    }
}

/** 목록/피드용 시간 라벨 — '관리자 · 6.01' 같은 접두어를 떼고 상대시간만 노출하지 않고 그대로 보여준다. */
private fun BoardPost.timeLabelShort(): String = timeLabel.substringAfter(" · ", timeLabel)

internal fun previewBoardHome(): BoardHomeData = BoardHomeData(
    pinned = listOf(
        BoardPost(101, BoardCategory.NOTICE, "신입 회원 환영 OT 일정 안내", authorName = "김민준", authorInitials = "민준", timeLabel = "관리자 · 6.01", isPinned = true),
        BoardPost(102, BoardCategory.NOTICE, "2025년 상반기 회비 납부 안내", authorName = "김민준", authorInitials = "민준", timeLabel = "관리자 · 5.28", isPinned = true),
    ),
    recent = listOf(
        BoardPost(201, BoardCategory.FREE, "동아리 MT 후기 공유해요", preview = "이번 MT 진짜 재밌었는데 다들 사진 올려주세요! 특히 둘째 날 게임할 때...", authorName = "이서연", authorInitials = "서연", timeLabel = "10분 전", likeCount = 8, commentCount = 12),
        BoardPost(301, BoardCategory.RECRUIT, "2025 하반기 신입 부원 모집", preview = "함께 성장할 신입 부원을 모집합니다. 백엔드/프론트 관심자 환영!", authorName = "김민준", authorInitials = "민준", timeLabel = "2시간 전", commentCount = 3, recruit = com.damoim.app.domain.model.RecruitInfo(RecruitStatus.OPEN, "D-7", 12, 20)),
        BoardPost(202, BoardCategory.FREE, "이번 주 모임 시간 변경 가능한가요?", authorName = "박준혁", authorInitials = "준혁", timeLabel = "어제", commentCount = 5),
    ),
)

@Preview
@Composable
private fun BoardHomeScreenPreview() {
    DamoimTheme { BoardHomeScreen(state = BoardHomeUiState(isLoading = false, home = previewBoardHome())) }
}

@Preview
@Composable
private fun BoardEmptyPreview() {
    DamoimTheme { BoardHomeScreen(state = BoardHomeUiState(isLoading = false, home = BoardHomeData(emptyList(), emptyList()))) }
}
