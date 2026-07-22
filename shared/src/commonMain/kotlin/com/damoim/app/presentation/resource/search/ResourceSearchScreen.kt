package com.damoim.app.presentation.resource.search

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.ClockIcon
import com.damoim.app.presentation.component.CloseIcon
import com.damoim.app.presentation.component.SearchIcon
import com.damoim.app.presentation.resource.ResourceRow
import com.damoim.app.presentation.resource.archive.previewResources
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 자료실 검색 — 게시판 검색(85/40/76)과 대칭. 서버 검색 엔드포인트가 없어 로드된 자료 목록을
 * 클라이언트에서 제목/파일명/올린이로 거른다. 결과 행은 자료실 [ResourceRow]를 그대로 재사용.
 */
@Composable
fun ResourceSearchRoute(
    viewModel: ResourceSearchViewModel = viewModel { ResourceSearchViewModel(AppGraph.getResourcesUseCase) },
    onBack: () -> Unit = {},
    onOpenResource: (Long) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    // 앱-전역 ViewModelStore에 남아 재진입 시 이전 검색이 뜨므로 진입할 때마다 입력/결과 초기화.
    LaunchedEffect(Unit) { viewModel.onClear() }
    ResourceSearchScreen(
        state = state,
        onBack = onBack,
        onQueryChange = viewModel::onQueryChange,
        onSubmit = { viewModel.submit() },
        onClear = viewModel::onClear,
        onKeyword = { viewModel.submit(it) },
        onRemoveRecent = viewModel::onRemoveRecent,
        onClearRecents = viewModel::onClearRecents,
        onOpenResource = onOpenResource,
    )
}

@Composable
fun ResourceSearchScreen(
    state: ResourceSearchUiState = ResourceSearchUiState(recent = listOf("회칙", "MT", "회계")),
    onBack: () -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
    onClear: () -> Unit = {},
    onKeyword: (String) -> Unit = {},
    onRemoveRecent: (String) -> Unit = {},
    onClearRecents: () -> Unit = {},
    onOpenResource: (Long) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        SearchBar(state.query, state.showSuggestions, onBack, onQueryChange, onSubmit, onClear)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            when {
                state.showSuggestions -> SuggestionsBody(state.recent, onKeyword, onRemoveRecent, onClearRecents)
                state.noResult -> NoResultBody(state.query)
                else -> ResultsBody(state.results, onOpenResource)
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
                if (query.isEmpty()) Text(DamoimStrings.ARCHIVE_SEARCH_PLACEHOLDER, style = DamoimTheme.typography.body, color = colors.textDisabled)
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

// ── 85 검색 시작(최근 검색어) ──
@Composable
private fun SuggestionsBody(recent: List<String>, onKeyword: (String) -> Unit, onRemoveRecent: (String) -> Unit, onClearRecents: () -> Unit) {
    val colors = DamoimTheme.colors
    if (recent.isEmpty()) {
        // 최근 검색어가 없을 때 안내(게시판은 빈 화면 — 자료실은 힌트를 보여준다).
        Column(Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = 40.dp, vertical = 90.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.size(72.dp).clip(CircleShape).background(colors.surfaceVariant), contentAlignment = Alignment.Center) { SearchIcon(colors.textDisabled, Modifier.size(30.dp)) }
            Text(DamoimStrings.ARCHIVE_SEARCH_HINT, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, textAlign = TextAlign.Center)
        }
        return
    }
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(DamoimStrings.SEARCH_RECENT, style = DamoimTheme.typography.bodyStrong.copy(fontWeight = FontWeight.ExtraBold), color = colors.textPrimary, modifier = Modifier.weight(1f))
            Text(
                DamoimStrings.SEARCH_CLEAR_ALL,
                style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold), color = colors.textDisabled,
                modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClearRecents),
            )
        }
        Column {
            recent.forEachIndexed { i, term ->
                Row(Modifier.fillMaxWidth().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onKeyword(term) }.padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ClockIcon(colors.textDisabled, Modifier.size(17.dp))
                    Text(term, style = DamoimTheme.typography.body.copy(fontSize = 14.5.sp), color = colors.textSecondary, modifier = Modifier.weight(1f))
                    Box(Modifier.size(24.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onRemoveRecent(term) }, contentAlignment = Alignment.Center) {
                        CloseIcon(colors.outlineStrong, Modifier.size(16.dp))
                    }
                }
                if (i != recent.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
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
@Composable
private fun ResultsBody(results: List<ResourceFile>, onOpenResource: (Long) -> Unit) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "${DamoimStrings.SEARCH_SECTION_RESOURCE} ${results.size}",
            style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
            color = colors.textMuted,
            modifier = Modifier.padding(bottom = 2.dp),
        )
        results.forEach { resource ->
            ResourceRow(resource, onClick = { onOpenResource(resource.id) })
        }
    }
}

@Preview
@Composable
private fun ResourceSearchStartPreview() {
    DamoimTheme { ResourceSearchScreen(state = ResourceSearchUiState(recent = listOf("회칙", "MT 사진", "회계"))) }
}

@Preview
@Composable
private fun ResourceSearchResultsPreview() {
    DamoimTheme {
        ResourceSearchScreen(state = ResourceSearchUiState(query = "회", searched = true, results = previewResources().filter { it.title.contains("회") }))
    }
}

@Preview
@Composable
private fun ResourceSearchNoResultPreview() {
    DamoimTheme { ResourceSearchScreen(state = ResourceSearchUiState(query = "xyz", searched = true, results = emptyList())) }
}
