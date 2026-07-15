package com.damoim.app.presentation.member.list

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.Member
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.PersonPlusIcon
import com.damoim.app.presentation.component.SearchIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.member.MemberFilterChip
import com.damoim.app.presentation.member.MemberRow
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 화면 17 회원 목록 (+77 빈 상태) — Route. 행 탭 → 18 상세. */
@Composable
fun MemberListRoute(
    initialCohortId: Long? = null,
    viewModel: MemberListViewModel = viewModel(key = "member_list") {
        MemberListViewModel(AppGraph.getMembersUseCase, AppGraph.getCohortsUseCase, AppGraph.getClubInfoUseCase, initialCohortId)
    },
    onBack: () -> Unit = {},
    onOpenMember: (Long) -> Unit = {},
    onShareCode: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    MemberListScreen(state, onBack, viewModel::onQuery, viewModel::onFilter, onOpenMember, onShareCode)
}

@Composable
fun MemberListScreen(
    state: MemberListUiState = MemberListUiState(isLoading = false, all = previewMembers(), cohorts = previewCohorts(), totalCount = 38),
    onBack: () -> Unit = {},
    onQuery: (String) -> Unit = {},
    onFilter: (MemberFilter) -> Unit = {},
    onOpenMember: (Long) -> Unit = {},
    onShareCode: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        // 고정 헤더 (제목 + 회원 수는 명부가 있을 때만)
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 20.dp, top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(24.dp).noRippleClick(onBack), contentAlignment = Alignment.Center) { BackChevronIcon(colors.textPrimary, Modifier.size(24.dp)) }
            Spacer(Modifier.width(8.dp))
            Text(DamoimStrings.MEMBER_LIST_TITLE, style = DamoimTheme.typography.titleMedium, color = colors.textPrimary, modifier = Modifier.weight(1f))
            if (!state.isClubEmpty) Text(DamoimStrings.memberCountLabel(state.totalCount), style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = colors.textMuted)
        }
        when {
            state.isClubEmpty -> {
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
                MemberEmpty(onShareCode)
            }
            else -> {
                // 검색·필터는 명부가 있을 때만 노출
                SearchField(state.query, onQuery)
                FilterRow(state, onFilter)
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
                // displayed는 계산 getter(2중 필터) — 한 번만 계산해 재사용(매 리컴포지션 O(N²) 방지)
                val displayed = remember(state.all, state.query, state.filter, state.cohorts) { state.displayed }
                val lastIndex = displayed.lastIndex
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
                if (state.isSearchEmpty) {
                    Text(
                        DamoimStrings.memberSearchEmpty(state.query),
                        style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
                        color = colors.textDisabled,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        textAlign = TextAlign.Center,
                    )
                }
                displayed.forEachIndexed { i, m ->
                    MemberRow(m, state.cohortShort(m.cohortId), onClick = { onOpenMember(m.id) })
                    if (i != lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
                }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQuery: (String) -> Unit) {
    val colors = DamoimTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)
            .clip(RoundedCornerShape(14.dp)).background(colors.surfaceVariant).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SearchIcon(colors.textMuted, Modifier.size(18.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) Text(DamoimStrings.MEMBER_SEARCH_HINT, style = DamoimTheme.typography.body.copy(fontSize = 14.sp), color = colors.textDisabled)
            BasicTextField(
                value = query,
                onValueChange = onQuery,
                singleLine = true,
                textStyle = DamoimTheme.typography.body.copy(fontSize = 14.sp, color = colors.textPrimary),
                cursorBrush = SolidColor(colors.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FilterRow(state: MemberListUiState, onFilter: (MemberFilter) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MemberFilterChip(DamoimStrings.MEMBER_FILTER_ALL, active = state.filter == MemberFilter.All) { onFilter(MemberFilter.All) }
        MemberFilterChip(DamoimStrings.MEMBER_STAT_STAFF, active = state.filter == MemberFilter.Staff) { onFilter(MemberFilter.Staff) }
        state.cohorts.forEach { c ->
            MemberFilterChip(c.short, active = state.filter == MemberFilter.Cohort(c.id)) { onFilter(MemberFilter.Cohort(c.id)) }
        }
    }
}

/** 77 빈 상태 — 41/자료실 빈 상태 패턴. */
@Composable
private fun MemberEmpty(onShareCode: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(
        Modifier.fillMaxSize().padding(horizontal = 40.dp).padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(96.dp).clip(RoundedCornerShape(28.dp)).background(colors.surfaceVariant), contentAlignment = Alignment.Center) {
            PersonPlusIcon(colors.textDisabled, Modifier.size(40.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(DamoimStrings.MEMBER_EMPTY_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp), color = colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text(DamoimStrings.MEMBER_EMPTY_SUBTITLE, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, textAlign = TextAlign.Center)
        Spacer(Modifier.height(22.dp))
        Row(
            Modifier.clip(RoundedCornerShape(12.dp)).background(colors.primary).noRippleClick(onShareCode).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(DamoimStrings.MEMBER_EMPTY_CTA, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.onPrimary)
        }
    }
}

internal fun previewMembers(): List<Member> = listOf(
    Member(501, "이서연", "서연", 24, MemberRole.MEMBER, joinedLabel = "2024.09.15", isMe = true),
    Member(502, "김민준", "민준", 23, MemberRole.LEADER),
    Member(503, "최유진", "유진", 23, MemberRole.STAFF),
    Member(504, "박준혁", "준혁", 25, MemberRole.MEMBER, com.damoim.app.domain.model.MemberStatus.DORMANT),
    Member(505, "정하늘", "하늘", 25, MemberRole.MEMBER),
    Member(506, "강도윤", "도윤", 25, MemberRole.STAFF),
)

internal fun previewCohorts() = listOf(
    com.damoim.app.domain.model.Cohort(25, "2025학년 1기 (25기)", "25기", 12),
    com.damoim.app.domain.model.Cohort(24, "2024학년 1기 (24기)", "24기", 15),
    com.damoim.app.domain.model.Cohort(23, "2023학년 1기 (23기)", "23기", 11),
)

@Preview
@Composable
private fun MemberListScreenPreview() {
    DamoimTheme { MemberListScreen() }
}
