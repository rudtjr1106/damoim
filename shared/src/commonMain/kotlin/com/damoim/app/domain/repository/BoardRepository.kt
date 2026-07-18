package com.damoim.app.domain.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.BoardPost
import com.damoim.app.domain.model.PostDetail
import com.damoim.app.domain.model.PostDraft
import kotlinx.coroutines.flow.Flow

/**
 * 게시판(C 그룹) 데이터 접근. 조회는 Flow(상태 변경 실시간 반영), 변경은 suspend.
 * 구현체는 [com.damoim.app.data.remote.board.RemoteBoardRepository](서버 위임).
 */
interface BoardRepository {

    /** 게시판 홈(10): 상단 고정(필독) + 최근 글. */
    fun observeBoardHome(): Flow<BoardHomeData>

    /** 카테고리별 목록(11/12/13). [category]=null이면 전체. */
    fun observePosts(category: BoardCategory?): Flow<List<BoardPost>>

    /** 게시글 상세(14/36/84) — 글 + 댓글. 삭제/부재 시 null. */
    fun observePostDetail(postId: Long): Flow<PostDetail?>

    /** 게시글 작성(15/34/35/39/70). 생성된 글 id 반환. */
    suspend fun createPost(draft: PostDraft): DataResult<Long>

    /** 게시글 수정(54 → 15). */
    suspend fun updatePost(postId: Long, draft: PostDraft): DataResult<Unit>

    /** 게시글 삭제(56). */
    suspend fun deletePost(postId: Long): DataResult<Unit>

    /** 상단 고정 토글(54). 새 고정 상태 반환. */
    suspend fun togglePin(postId: Long): DataResult<Boolean>

    /** 좋아요 토글(14/36/84). */
    suspend fun toggleLike(postId: Long): DataResult<Unit>

    /** 투표(36). 단일 선택은 교체, 복수 선택은 토글. */
    suspend fun votePoll(postId: Long, optionIndex: Int): DataResult<Unit>

    /** 다시 투표 — 내 표 회수(36). */
    suspend fun clearPollVote(postId: Long): DataResult<Unit>

    /** 모집 신청(84). 성공 여부 반환(정원 초과/중복이면 false). */
    suspend fun applyRecruit(postId: Long): DataResult<Boolean>

    /** 모집 신청 취소(84). */
    suspend fun cancelRecruit(postId: Long): DataResult<Unit>

    /** 댓글/답글 추가(14/36/84). */
    suspend fun addComment(postId: Long, content: String, parentId: Long?): DataResult<Unit>

    /** 검색 실행(40). 최근 검색어에도 기록된다. */
    suspend fun search(query: String): DataResult<SearchResults>

    /** 검색 시작(85): 최근 + 추천 검색어. */
    fun observeSearchSuggestions(): Flow<SearchSuggestions>

    /** 최근 검색어 개별 삭제(85). */
    suspend fun removeRecentSearch(query: String): DataResult<Unit>

    /** 최근 검색어 전체 삭제(85). */
    suspend fun clearRecentSearches(): DataResult<Unit>

    /** 작성 임시저장(15) — 저장/불러오기/삭제. 등록 성공 시 자동 삭제된다. */
    suspend fun saveDraft(draft: PostDraft): DataResult<Unit>
    fun loadDraft(): PostDraft?
    fun clearDraft()
}

/** 게시판 홈 데이터: 고정 공지(필독) + 최근 글 리스트. */
data class BoardHomeData(
    val pinned: List<BoardPost>,
    val recent: List<BoardPost>,
) {
    val isEmpty: Boolean get() = pinned.isEmpty() && recent.isEmpty()
}

/** 통합 검색 결과(40). 게시글은 board 도메인, 일정/파일은 요약 표시용 경량 모델. */
data class SearchResults(
    val query: String,
    val posts: List<BoardPost>,
    val schedules: List<SearchScheduleHit>,
    val files: List<SearchFileHit>,
) {
    val total: Int get() = posts.size + schedules.size + files.size
}

data class SearchScheduleHit(val month: String, val day: String, val title: String, val subtitle: String, val id: Long = 0)
data class SearchFileHit(val name: String, val meta: String)

/** 검색 시작 화면(85) 데이터. */
data class SearchSuggestions(
    val recent: List<String>,
)
