package com.damoim.app.presentation.board.search

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.repository.SearchResults
import com.damoim.app.domain.repository.SearchSuggestions
import com.damoim.app.domain.usecase.GetSearchSuggestionsUseCase
import com.damoim.app.domain.usecase.ManageRecentSearchUseCase
import com.damoim.app.domain.usecase.SearchBoardUseCase
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val suggestions: SearchSuggestions? = null,
    val results: SearchResults? = null,
    val searched: Boolean = false,      // 검색 실행 여부(85 vs 40/76 분기)
) : UiState {
    val showSuggestions: Boolean get() = !searched     // 85 검색 시작
    val noResult: Boolean get() = searched && (results?.total ?: 0) == 0  // 76 무결과
}

sealed interface SearchSideEffect : UiSideEffect

/**
 * 화면 85 검색 시작 / 40 검색 결과 / 76 무결과 통합.
 * 실제 게시글을 질의어로 필터링하고, 최근 검색어가 기록/삭제된다.
 */
class SearchViewModel(
    getSuggestions: GetSearchSuggestionsUseCase,
    private val searchBoard: SearchBoardUseCase,
    private val manageRecent: ManageRecentSearchUseCase,
) : BaseViewModel<SearchUiState, SearchSideEffect>(SearchUiState()) {

    init {
        viewModelScope.launch {
            getSuggestions().collect { s -> setState { copy(suggestions = s) } }
        }
    }

    fun onQueryChange(q: String) = setState {
        // 입력을 비우면 다시 검색 시작(85) 상태로
        if (q.isEmpty()) copy(query = q, searched = false, results = null) else copy(query = q)
    }

    fun onClear() = setState { copy(query = "", searched = false, results = null) }

    fun submit(q: String = currentState.query) {
        val query = q.trim()
        if (query.isEmpty()) return
        setState { copy(query = query) }
        viewModelScope.launch {
            handleResult(searchBoard(query), onSuccess = { r -> setState { copy(results = r, searched = true) } })
        }
    }

    fun onRemoveRecent(term: String) = viewModelScope.launch { manageRecent.remove(term) }
    fun onClearRecents() = viewModelScope.launch { manageRecent.clear() }
}
