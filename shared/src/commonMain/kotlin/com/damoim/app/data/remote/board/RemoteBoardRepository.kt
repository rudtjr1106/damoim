package com.damoim.app.data.remote.board

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.core.result.getOrNull
import com.damoim.app.core.result.map
import com.damoim.app.data.remote.core.ApiClient
import com.damoim.app.data.remote.core.ApiRoutes
import com.damoim.app.data.remote.core.AttachmentTypes
import com.damoim.app.data.remote.core.DataTopic
import com.damoim.app.data.remote.core.ErrorCodes
import com.damoim.app.data.remote.core.RawHttp
import com.damoim.app.data.remote.core.RemoteBus
import com.damoim.app.data.remote.core.RemoteEnv
import com.damoim.app.data.remote.core.SharedFlows
import com.damoim.app.data.remote.core.reactiveFlow
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.BoardPost
import com.damoim.app.domain.model.PostDetail
import com.damoim.app.domain.model.PostDraft
import com.damoim.app.domain.repository.BoardHomeData
import com.damoim.app.domain.repository.BoardRepository
import com.damoim.app.domain.repository.SearchResults
import com.damoim.app.domain.repository.SearchSuggestions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * [BoardRepository]의 서버 구현 (C 게시판).
 *
 * 변경은 [DataTopic.BOARD]만 무효화 → 게시판 화면만 재조회(일정/회원/알림 등은 건드리지 않음).
 */
class RemoteBoardRepository(private val api: ApiClient) : BoardRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val shared = SharedFlows(scope)

    /** loadDraft/clearDraft가 동기 계약이라 서버 초안을 인메모리로 캐시. */
    private var cachedDraft: PostDraft? = null

    init {
        scope.launch { cachedDraft = fetchDraft() } // 콜드스타트 프라임
    }

    override fun observeBoardHome(): Flow<BoardHomeData> = shared.get("board-home") {
        reactiveFlow(DataTopic.BOARD, fallback = BoardHomeData(emptyList(), emptyList())) {
            api.getData<BoardHomeResponseDto>(ApiRoutes.Board.HOME).getOrNull()?.toDomainHome()
                ?: BoardHomeData(emptyList(), emptyList())
        }
    }

    override fun observePosts(category: BoardCategory?): Flow<List<BoardPost>> =
        shared.get("posts:${category?.name}") {
            reactiveFlow(DataTopic.BOARD, fallback = emptyList()) {
                api.getData<List<PostSummaryResponseDto>>(
                    ApiRoutes.Board.POSTS,
                    mapOf("category" to category?.name),
                ).getOrNull()?.toDomainList() ?: emptyList()
            }
        }

    override fun observePostDetail(postId: Long): Flow<PostDetail?> = shared.get("post:$postId") {
        reactiveFlow<PostDetail?>(DataTopic.BOARD, fallback = null) {
            api.getData<PostDetailResponseDto>(ApiRoutes.Board.post(postId))
                .getOrNull()?.toDomain(RemoteEnv.currentUserId)
        }
    }

    override suspend fun createPost(draft: PostDraft): DataResult<Long> {
        // 이미지/문서 첨부를 먼저 S3에 업로드해 storageKey를 확보(실패 시 글 생성 중단).
        val attachments = uploadMedia(draft)
            ?: return DataResult.Failure(DataError(ErrorCodes.UPLOAD_FAILED, "첨부 업로드에 실패했어요"))
        return api.postData<PostDetailResponseDto>(ApiRoutes.Board.POSTS, draft.toCreateRequest(attachments))
            .map { it.id }
            .also { RemoteBus.invalidate(DataTopic.BOARD) }
    }

    /** draft의 이미지/문서를 presigned PUT으로 S3에 올리고 AttachmentInputDto(storageKey) 목록을 만든다. 실패 시 null. */
    private suspend fun uploadMedia(draft: PostDraft): List<AttachmentInputDto>? {
        val out = mutableListOf<AttachmentInputDto>()
        draft.images.forEachIndexed { i, img ->
            val bytes = img.bytes ?: return@forEachIndexed // 기존 이미지(수정 프리필)는 재업로드 안 함
            val key = uploadOne("image_$i", img.contentType, bytes, AttachmentTypes.IMAGE) ?: return null
            out += AttachmentInputDto(type = AttachmentTypes.IMAGE, storageKey = key)
        }
        draft.docs.forEach { doc ->
            val bytes = doc.bytes ?: return@forEach
            val key = uploadOne(doc.name, doc.contentType, bytes, AttachmentTypes.FILE_DOC) ?: return null
            out += AttachmentInputDto(
                type = AttachmentTypes.FILE_DOC, storageKey = key,
                fileName = doc.name, fileSizeBytes = bytes.size.toLong(),
            )
        }
        return out
    }

    /** 1) 업로드 URL 발급(권한·상한 검증) → 2) S3에 직접 PUT. storageKey 반환(실패 null). */
    private suspend fun uploadOne(fileName: String, contentType: String?, bytes: ByteArray, kind: String): String? {
        val presign = api.postData<BoardUploadUrlResponseDto>(
            ApiRoutes.Board.UPLOAD_URL,
            BoardUploadUrlRequestDto(fileName = fileName, contentType = contentType, sizeBytes = bytes.size.toLong(), kind = kind),
        ).getOrNull() ?: return null
        return if (RawHttp.put(presign.uploadUrl, bytes, contentType)) presign.storageKey else null
    }

    override suspend fun updatePost(postId: Long, draft: PostDraft): DataResult<Unit> =
        api.patchUnit(ApiRoutes.Board.post(postId), draft.toUpdateRequest())
            .also { RemoteBus.invalidate(DataTopic.BOARD) }

    override suspend fun deletePost(postId: Long): DataResult<Unit> =
        api.deleteUnit(ApiRoutes.Board.post(postId)).also { RemoteBus.invalidate(DataTopic.BOARD) }

    override suspend fun togglePin(postId: Long): DataResult<Boolean> =
        api.postData<PinResponseDto>(ApiRoutes.Board.pin(postId))
            .map { it.isPinned }
            .also { RemoteBus.invalidate(DataTopic.BOARD) }

    override suspend fun toggleLike(postId: Long): DataResult<Unit> =
        api.postUnit(ApiRoutes.Board.like(postId)).also { RemoteBus.invalidate(DataTopic.BOARD) }

    override suspend fun votePoll(postId: Long, optionIndex: Int): DataResult<Unit> =
        api.postUnit(ApiRoutes.Board.pollVote(postId), VotePollRequestDto(optionIndex))
            .also { RemoteBus.invalidate(DataTopic.BOARD) }

    override suspend fun clearPollVote(postId: Long): DataResult<Unit> =
        api.deleteUnit(ApiRoutes.Board.pollVote(postId)).also { RemoteBus.invalidate(DataTopic.BOARD) }

    override suspend fun applyRecruit(postId: Long): DataResult<Boolean> {
        val result = api.postData<RecruitResponseDto>(ApiRoutes.Board.recruitApply(postId))
        RemoteBus.invalidate(DataTopic.BOARD)
        return when (result) {
            is DataResult.Success -> DataResult.Success(true)
            is DataResult.Failure ->
                if (result.error.code == ErrorCodes.RECRUIT_CLOSED || result.error.code == ErrorCodes.ALREADY_APPLIED) {
                    DataResult.Success(false)
                } else {
                    result
                }
        }
    }

    override suspend fun addComment(postId: Long, content: String, parentId: Long?): DataResult<Unit> =
        api.postUnit(ApiRoutes.Board.comments(postId), AddCommentRequestDto(content, parentId))
            .also { RemoteBus.invalidate(DataTopic.BOARD) }

    override suspend fun search(query: String): DataResult<SearchResults> =
        api.getData<SearchResultResponseDto>(ApiRoutes.Board.SEARCH, mapOf("q" to query))
            .map {
                SearchResults(
                    query = it.query,
                    posts = it.posts.toDomainList(),
                    schedules = emptyList(), // 게시판 검색은 글만 반환(일정/파일 탭은 미연결 갭)
                    files = emptyList(),
                )
            }
            .also { RemoteBus.invalidate(DataTopic.BOARD) } // 최근 검색어 기록 → 추천 화면 갱신

    override fun observeSearchSuggestions(): Flow<SearchSuggestions> = shared.get("search-suggestions") {
        reactiveFlow(DataTopic.BOARD, fallback = SearchSuggestions(emptyList(), emptyList())) {
            api.getData<SearchSuggestionsResponseDto>(ApiRoutes.Board.SEARCH_SUGGESTIONS).getOrNull()
                ?.let { SearchSuggestions(recent = it.recent, recommended = it.recommended) }
                ?: SearchSuggestions(emptyList(), emptyList())
        }
    }

    override suspend fun removeRecentSearch(query: String): DataResult<Unit> =
        api.deleteUnit(ApiRoutes.Board.SEARCH_RECENT, mapOf("q" to query))
            .also { RemoteBus.invalidate(DataTopic.BOARD) }

    override suspend fun clearRecentSearches(): DataResult<Unit> =
        api.deleteUnit(ApiRoutes.Board.SEARCH_RECENT).also { RemoteBus.invalidate(DataTopic.BOARD) }

    override suspend fun saveDraft(draft: PostDraft): DataResult<Unit> {
        cachedDraft = draft
        return api.putUnit(ApiRoutes.Board.DRAFT, draft.toDraftRequest())
    }

    override fun loadDraft(): PostDraft? = cachedDraft

    override fun clearDraft() {
        cachedDraft = null
        scope.launch { runCatching { api.deleteUnit(ApiRoutes.Board.DRAFT) } }
    }

    private suspend fun fetchDraft(): PostDraft? =
        api.getData<DraftResponseDto>(ApiRoutes.Board.DRAFT).getOrNull()?.toDomain()
}
