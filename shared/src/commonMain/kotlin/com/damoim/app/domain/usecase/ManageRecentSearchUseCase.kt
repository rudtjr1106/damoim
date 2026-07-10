package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.repository.BoardRepository

/** 최근 검색어 관리(85) — 개별/전체 삭제. */
class ManageRecentSearchUseCase(private val boardRepository: BoardRepository) {
    suspend fun remove(query: String): DataResult<Unit> = boardRepository.removeRecentSearch(query)
    suspend fun clear(): DataResult<Unit> = boardRepository.clearRecentSearches()
}
