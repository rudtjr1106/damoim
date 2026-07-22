package com.damoim.app.presentation.resource.search

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.domain.usecase.GetResourcesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 자료실 최근 검색어 — 게시판은 서버가 저장하지만 자료실은 검색 서버 계약이 없어
 * 세션 동안 클라이언트(앱 전역)에 보관한다. (서버 검색 도입 시 게시판과 동일하게 이관)
 */
internal object ResourceSearchRecents {
    private const val MAX = 8
    private val _recent = MutableStateFlow<List<String>>(emptyList())
    val recent: StateFlow<List<String>> get() = _recent

    fun add(query: String) {
        val term = query.trim()
        if (term.isEmpty()) return
        _recent.value = (listOf(term) + _recent.value.filterNot { it.equals(term, ignoreCase = true) }).take(MAX)
    }

    fun remove(term: String) { _recent.value = _recent.value.filterNot { it == term } }
    fun clear() { _recent.value = emptyList() }
}

data class ResourceSearchUiState(
    val query: String = "",
    val recent: List<String> = emptyList(),
    val results: List<ResourceFile> = emptyList(),
    val searched: Boolean = false,      // 검색 실행 여부(85 vs 40/76 분기)
) : UiState {
    val showSuggestions: Boolean get() = !searched                       // 85 검색 시작
    val noResult: Boolean get() = searched && results.isEmpty()          // 76 무결과
}

sealed interface ResourceSearchSideEffect : UiSideEffect

/**
 * 자료실 검색(게시판 85/40/76 대칭). 서버 검색 엔드포인트가 없어 로드된 자료 목록 위에서
 * 제목/파일명/올린이로 필터한다(폴더 무관 전체 대상). 최근 검색어는 [ResourceSearchRecents]에 보관.
 */
class ResourceSearchViewModel(
    getResources: GetResourcesUseCase,
) : BaseViewModel<ResourceSearchUiState, ResourceSearchSideEffect>(ResourceSearchUiState()) {

    private var all: List<ResourceFile> = emptyList()

    init {
        // 폴더 무관 전체 목록을 관찰(서버 재조회는 topic 무효화로 자동). 목록이 바뀌면 결과도 갱신.
        viewModelScope.launch {
            getResources(null).collect { list ->
                all = list
                setState { if (searched) copy(results = filterBy(query)) else this }
            }
        }
        viewModelScope.launch {
            ResourceSearchRecents.recent.collect { r -> setState { copy(recent = r) } }
        }
    }

    fun onQueryChange(q: String) = setState {
        // 입력을 비우면 다시 검색 시작(85) 상태로
        if (q.isEmpty()) copy(query = q, searched = false, results = emptyList()) else copy(query = q)
    }

    fun onClear() = setState { copy(query = "", searched = false, results = emptyList()) }

    fun submit(q: String = currentState.query) {
        val query = q.trim()
        if (query.isEmpty()) return
        ResourceSearchRecents.add(query)
        setState { copy(query = query, searched = true, results = filterBy(query)) }
    }

    private fun filterBy(q: String): List<ResourceFile> {
        val query = q.trim()
        if (query.isEmpty()) return emptyList()
        return all.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.fileName.contains(query, ignoreCase = true) ||
                it.uploaderName.contains(query, ignoreCase = true)
        }
    }

    fun onRemoveRecent(term: String) = ResourceSearchRecents.remove(term)
    fun onClearRecents() = ResourceSearchRecents.clear()
}
