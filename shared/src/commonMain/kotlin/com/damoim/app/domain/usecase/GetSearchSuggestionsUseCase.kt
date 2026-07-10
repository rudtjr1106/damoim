package com.damoim.app.domain.usecase

import com.damoim.app.domain.repository.BoardRepository
import com.damoim.app.domain.repository.SearchSuggestions
import kotlinx.coroutines.flow.Flow

/** 검색 시작(85) — 최근·추천 검색어 관찰. */
class GetSearchSuggestionsUseCase(private val boardRepository: BoardRepository) {
    operator fun invoke(): Flow<SearchSuggestions> = boardRepository.observeSearchSuggestions()
}
