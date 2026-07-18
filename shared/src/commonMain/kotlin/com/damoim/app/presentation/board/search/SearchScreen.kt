package com.damoim.app.presentation.board.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.repository.SearchResults
import com.damoim.app.domain.repository.SearchSuggestions
import com.damoim.app.presentation.board.boardCategoryLabel
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.ClockIcon
import com.damoim.app.presentation.component.CloseIcon
import com.damoim.app.presentation.component.PaperclipIcon
import com.damoim.app.presentation.component.SearchIcon
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

@Composable
fun SearchRoute(
    viewModel: SearchViewModel = viewModel { SearchViewModel(AppGraph.getSearchSuggestionsUseCase, AppGraph.searchBoardUseCase, AppGraph.manageRecentSearchUseCase) },
    onBack: () -> Unit = {},
    onOpenPost: (Long) -> Unit = {},
    onOpenSchedule: (Long) -> Unit = {},
    onComingSoon: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    // 검색 화면은 세션 내내 살아있는 앱-전역 ViewModelStore에 남아 재진입 시 이전 검색어가
    // 그대로 뜬다 → 진입할 때마다 입력창/결과를 초기화(최근 검색어는 별도 flow라 유지).
    LaunchedEffect(Unit) { viewModel.onClear() }
    SearchScreen(
        state = state,
        onBack = onBack,
        onQueryChange = viewModel::onQueryChange,
        onSubmit = { viewModel.submit() },
        onClear = viewModel::onClear,
        onKeyword = { viewModel.submit(it) },
        onRemoveRecent = viewModel::onRemoveRecent,
        onClearRecents = viewModel::onClearRecents,
        onOpenPost = onOpenPost,
        onOpenSchedule = onOpenSchedule,
        onComingSoon = onComingSoon,
    )
}

@Composable
fun SearchScreen(
    state: SearchUiState = SearchUiState(suggestions = previewSuggestions()),
    onBack: () -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onClear: () -> Unit = {},
    onKeyword: (String) -> Unit = {},
    onRemoveRecent: (String) -> Unit = {},
    onClearRecents: () -> Unit = {},
    onOpenPost: (Long) -> Unit = {},
    onOpenSchedule: (Long) -> Unit = {},
    onComingSoon: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        SearchBar(state.query, state.showSuggestions, onBack, onQueryChange, onSubmit, onClear)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            when {
                state.showSuggestions -> state.suggestions?.let { SuggestionsBody(it, onKeyword, onRemoveRecent, onClearRecents) }
                state.noResult -> NoResultBody(state.query)
                else -> state.results?.let { ResultsBody(it, onOpenPost, onOpenSchedule, onComingSoon) }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, focusedLook: Boolean, onBack: () -> Unit, onQueryChange: (String) -> Unit, onSubmit: () -> Unit, onClear: () -> Unit) {
    val colors = DamoimTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val bordered = focused || focusedLook
    Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(28.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onBack), contentAlignment = Alignment.Center) { BackChevronIcon(colors.textPrimary, Modifier.size(24.dp)) }
        Row(
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(colors.surfaceVariant)
                .then(if (bordered) Modifier.border(1.5.dp, colors.primary, RoundedCornerShape(14.dp)) else Modifier)
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SearchIcon(colors.textMuted, Modifier.size(17.dp))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) Text(DamoimStrings.SEARCH_PLACEHOLDER, style = DamoimTheme.typography.body, color = colors.textDisabled)
                BasicTextField(
                    value = query, onValueChange = onQueryChange, singleLine = true,
                    textStyle = DamoimTheme.typography.body.copy(color = colors.textPrimary, fontWeight = FontWeight.Bold),
                    cursorBrush = SolidColor(colors.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                    interactionSource = interaction,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isNotEmpty()) {
                Box(Modifier.size(18.dp).clip(CircleShape).background(colors.outline).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClear), contentAlignment = Alignment.Center) {
                    CloseIcon(colors.surface, Modifier.size(10.dp))
                }
            }
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
}

// ── 85 검색 시작 ──
@Composable
private fun SuggestionsBody(s: SearchSuggestions, onKeyword: (String) -> Unit, onRemoveRecent: (String) -> Unit, onClearRecents: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (s.recent.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(DamoimStrings.SEARCH_RECENT, style = DamoimTheme.typography.bodyStrong.copy(fontWeight = FontWeight.ExtraBold), color = colors.textPrimary, modifier = Modifier.weight(1f))
                Text(
                    DamoimStrings.SEARCH_CLEAR_ALL,
                    style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold), color = colors.textDisabled,
                    modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClearRecents),
                )
            }
            Column {
                s.recent.forEachIndexed { i, term ->
                    Row(Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onKeyword(term) }.padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ClockIcon(colors.textDisabled, Modifier.size(17.dp))
                        Text(term, style = DamoimTheme.typography.body.copy(fontSize = 14.5.sp), color = colors.textSecondary, modifier = Modifier.weight(1f))
                        Box(Modifier.size(24.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onRemoveRecent(term) }, contentAlignment = Alignment.Center) {
                            CloseIcon(colors.outlineStrong, Modifier.size(16.dp))
                        }
                    }
                    if (i != s.recent.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
                }
            }
        }
    }
}

// ── 76 무결과 ──
@Composable
private fun NoResultBody(query: String) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = 40.dp, vertical = 90.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Box(Modifier.size(80.dp).clip(CircleShape).background(colors.surfaceVariant), contentAlignment = Alignment.Center) { SearchIcon(colors.textDisabled, Modifier.size(34.dp)) }
        Text(DamoimStrings.searchNoResultTitle(query), style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 17.sp), color = colors.textPrimary, textAlign = TextAlign.Center)
        Text(DamoimStrings.SEARCH_NO_RESULT_SUBTITLE, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, textAlign = TextAlign.Center)
    }
}

// ── 40 검색 결과 ──
// 게시판 검색은 게시글만 반환하므로(일정/파일 미연결) 전체=게시글로 동일해 의미 없던
// 상단 필터 탭(전체/게시글/일정/파일)을 제거했다. 결과는 바로 목록으로 보여준다.
@Composable
private fun ResultsBody(results: SearchResults, onOpenPost: (Long) -> Unit, onOpenSchedule: (Long) -> Unit, onComingSoon: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            if (results.posts.isNotEmpty()) {
                SectionLabel(DamoimStrings.SEARCH_SECTION_POST)
                results.posts.forEach { p ->
                    Column(Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onOpenPost(p.id) }.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(highlight(p.title, results.query), style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = colors.textPrimary)
                        Text("${boardCategoryLabel(p.category)} 게시판 · ${p.authorName} · ${p.timeLabel}", style = DamoimTheme.typography.caption, color = colors.textDisabled)
                    }
                }
            }
            results.schedules.forEach { sch ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionLabel(DamoimStrings.SEARCH_SECTION_SCHEDULE)
                    Row(Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onOpenSchedule(sch.id) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(colors.primaryContainer), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(sch.month, style = DamoimTheme.typography.labelSmall, color = colors.primaryDark)
                            Text(sch.day, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp), color = colors.textPrimary)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(highlight(sch.title, results.query), style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = colors.textPrimary)
                            Text(sch.subtitle, style = DamoimTheme.typography.caption, color = colors.textDisabled)
                        }
                    }
                }
            }
            results.files.forEach { file ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionLabel(DamoimStrings.SEARCH_SECTION_FILE)
                    Row(Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onComingSoon).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(colors.surfaceVariant), contentAlignment = Alignment.Center) { PaperclipIcon(colors.textTertiary, Modifier.size(16.dp)) }
                        Column(Modifier.weight(1f)) {
                            Text(highlight(file.name, results.query), style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = colors.textPrimary)
                            Text(file.meta, style = DamoimTheme.typography.caption, color = colors.textDisabled)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp), color = DamoimTheme.colors.textMuted)
}

/** 검색어 하이라이트(primaryContainer 배경 + primaryDeep 텍스트). */
@Composable
private fun highlight(text: String, query: String) = buildAnnotatedString {
    val colors = DamoimTheme.colors
    if (query.isBlank()) { append(text); return@buildAnnotatedString }
    var i = 0
    val lower = text.lowercase(); val q = query.lowercase()
    while (i < text.length) {
        val hit = lower.indexOf(q, i)
        if (hit < 0) { append(text.substring(i)); break }
        append(text.substring(i, hit))
        withStyle(SpanStyle(background = colors.primaryContainer, color = colors.primaryDeep)) { append(text.substring(hit, hit + query.length)) }
        i = hit + query.length
    }
}

internal fun previewSuggestions() = SearchSuggestions(
    recent = listOf("MT", "신입 부원 모집", "회칙", "OT 일정"),
)

@Preview
@Composable
private fun SearchStartPreview() {
    DamoimTheme { SearchScreen(state = SearchUiState(suggestions = previewSuggestions())) }
}

@Preview
@Composable
private fun SearchResultsPreview() {
    DamoimTheme {
        SearchScreen(state = SearchUiState(query = "MT", searched = true, results = SearchResults(
            query = "MT",
            posts = listOf(
                com.damoim.app.domain.model.BoardPost(201, com.damoim.app.domain.model.BoardCategory.FREE, "동아리 MT 후기 공유해요", authorName = "이서연", authorInitials = "서연", timeLabel = "10분 전"),
                com.damoim.app.domain.model.BoardPost(205, com.damoim.app.domain.model.BoardCategory.FREE, "MT 날짜 투표해주세요!", authorName = "김민준", authorInitials = "민준", timeLabel = "1시간 전"),
            ),
            schedules = listOf(com.damoim.app.domain.repository.SearchScheduleHit("6월", "14", "신입 환영 MT", "1박 2일 · 가평")),
            files = listOf(com.damoim.app.domain.repository.SearchFileHit("MT_준비물_체크리스트.xlsx", "게시글 첨부 · 24KB")),
        )))
    }
}
