package com.damoim.app.presentation.board

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.Comment
import com.damoim.app.domain.model.PostDetail
import com.damoim.app.domain.usecase.GetPostDetailUseCase
import com.damoim.app.domain.usecase.ObserveMyContextUseCase
import com.damoim.app.domain.usecase.PostActionUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.launch

data class PostDetailUiState(
    val isLoading: Boolean = true,
    val detail: PostDetail? = null,
    val notFound: Boolean = false,        // 79 없는/삭제된 콘텐츠
    val myUserId: Long = 0,
    val isLeader: Boolean = false,
    val commentInput: String = "",
    val replyTarget: Comment? = null,     // 답글 대상(입력바 위 칩으로 표시)
    val isSendingComment: Boolean = false,
) : UiState {
    val isMyPost: Boolean get() = detail?.post?.authorId == myUserId
}

sealed interface PostDetailSideEffect : UiSideEffect {
    data class Toast(val message: String) : PostDetailSideEffect
    data object Deleted : PostDetailSideEffect
}

/**
 * 화면 14/36/84 게시글·모집 상세. 좋아요/투표/신청/댓글이 스토어에 실제 반영되고
 * Flow 구독으로 즉시 화면에 갱신된다.
 */
class PostDetailViewModel(
    getPostDetail: GetPostDetailUseCase,
    observeMyContext: ObserveMyContextUseCase,
    private val postAction: PostActionUseCase,
    private val postId: Long,
) : BaseViewModel<PostDetailUiState, PostDetailSideEffect>(PostDetailUiState()) {

    private var deleted = false

    init {
        viewModelScope.launch {
            getPostDetail(postId).collect { detail ->
                setState { copy(isLoading = false, detail = detail, notFound = detail == null && !deleted) }
            }
        }
        viewModelScope.launch {
            observeMyContext().collect { ctx ->
                setState { copy(myUserId = ctx.userId, isLeader = ctx.role == ClubRole.LEADER) }
            }
        }
    }

    // ── 좋아요 ──
    fun onToggleLike() = viewModelScope.launch { postAction.toggleLike(postId) }

    // ── 투표(36) ──
    fun onVote(optionIndex: Int) = viewModelScope.launch { postAction.vote(postId, optionIndex) }
    fun onRevote() = viewModelScope.launch { postAction.clearVote(postId) }

    // ── 모집 신청(84) ──
    fun onApplyRecruit() = viewModelScope.launch {
        handleResult(postAction.applyRecruit(postId), onSuccess = { applied ->
            sendEffect(PostDetailSideEffect.Toast(if (applied) DamoimStrings.TOAST_RECRUIT_APPLIED else DamoimStrings.TOAST_RECRUIT_FULL))
        })
    }

    // ── 댓글 ──
    fun onCommentInputChange(value: String) = setState { copy(commentInput = value) }
    fun onReplyTo(comment: Comment?) = setState { copy(replyTarget = comment) }

    fun onSendComment() {
        val content = currentState.commentInput.trim()
        if (content.isEmpty() || currentState.isSendingComment) return
        val parent = currentState.replyTarget
        setState { copy(isSendingComment = true) }
        viewModelScope.launch {
            val result = postAction.addComment(postId, content, parent?.let { if (it.isReply) it.parentId else it.id })
            setState { copy(isSendingComment = false) }
            handleResult(result, onSuccess = {
                setState { copy(commentInput = "", replyTarget = null) }
            })
        }
    }

    // ── 54 메뉴 액션 ──
    fun onTogglePin() = viewModelScope.launch {
        handleResult(postAction.togglePin(postId), onSuccess = { pinned ->
            sendEffect(PostDetailSideEffect.Toast(if (pinned) DamoimStrings.TOAST_POST_PINNED else DamoimStrings.TOAST_POST_UNPINNED))
        })
    }

    fun onDelete() = viewModelScope.launch {
        deleted = true
        handleResult(postAction.delete(postId), onSuccess = {
            sendEffect(PostDetailSideEffect.Deleted)
        })
    }
}
