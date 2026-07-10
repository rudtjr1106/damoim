package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.repository.BoardRepository
import com.damoim.app.domain.repository.SearchResults

/** 통합 검색 (화면 40). */
class SearchBoardUseCase(private val boardRepository: BoardRepository) {
    suspend operator fun invoke(query: String): DataResult<SearchResults> =
        boardRepository.search(query)
}
