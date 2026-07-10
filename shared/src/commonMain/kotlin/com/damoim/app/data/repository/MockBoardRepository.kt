package com.damoim.app.data.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.data.mock.MockStore
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.BoardPost
import com.damoim.app.domain.model.PostDetail
import com.damoim.app.domain.model.PostDraft
import com.damoim.app.domain.repository.BoardHomeData
import com.damoim.app.domain.repository.BoardRepository
import com.damoim.app.domain.repository.SearchResults
import com.damoim.app.domain.repository.SearchSuggestions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/**
 * [BoardRepository]의 Mock 구현 — [MockStore]에 위임. 서버 도입 시 Ktor 구현으로 교체.
 */
class MockBoardRepository : BoardRepository {

    override fun observeBoardHome(): Flow<BoardHomeData> = MockStore.boardHomeFlow()

    override fun observePosts(category: BoardCategory?): Flow<List<BoardPost>> = MockStore.postsFlow(category)

    override fun observePostDetail(postId: Long): Flow<PostDetail?> = MockStore.postDetailFlow(postId)

    override suspend fun createPost(draft: PostDraft): DataResult<Long> {
        delay(WRITE_DELAY_MS)
        return DataResult.Success(MockStore.createPost(draft))
    }

    override suspend fun updatePost(postId: Long, draft: PostDraft): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.updatePost(postId, draft)
        return DataResult.Success(Unit)
    }

    override suspend fun deletePost(postId: Long): DataResult<Unit> {
        delay(WRITE_DELAY_MS)
        MockStore.deletePost(postId)
        return DataResult.Success(Unit)
    }

    override suspend fun togglePin(postId: Long): DataResult<Boolean> =
        DataResult.Success(MockStore.togglePin(postId))

    override suspend fun toggleLike(postId: Long): DataResult<Unit> {
        MockStore.toggleLike(postId)
        return DataResult.Success(Unit)
    }

    override suspend fun votePoll(postId: Long, optionIndex: Int): DataResult<Unit> {
        MockStore.votePoll(postId, optionIndex)
        return DataResult.Success(Unit)
    }

    override suspend fun clearPollVote(postId: Long): DataResult<Unit> {
        MockStore.clearPollVote(postId)
        return DataResult.Success(Unit)
    }

    override suspend fun applyRecruit(postId: Long): DataResult<Boolean> {
        delay(WRITE_DELAY_MS)
        return DataResult.Success(MockStore.applyRecruit(postId))
    }

    override suspend fun addComment(postId: Long, content: String, parentId: Long?): DataResult<Unit> {
        delay(200L)
        MockStore.addComment(postId, content, parentId)
        return DataResult.Success(Unit)
    }

    override suspend fun search(query: String): DataResult<SearchResults> {
        delay(300L)
        return DataResult.Success(MockStore.search(query))
    }

    override fun observeSearchSuggestions(): Flow<SearchSuggestions> = MockStore.searchSuggestionsFlow()

    override suspend fun removeRecentSearch(query: String): DataResult<Unit> {
        MockStore.removeRecentSearch(query)
        return DataResult.Success(Unit)
    }

    override suspend fun clearRecentSearches(): DataResult<Unit> {
        MockStore.clearRecentSearches()
        return DataResult.Success(Unit)
    }

    private companion object {
        const val WRITE_DELAY_MS = 350L
    }
}
