package com.damoim.app.presentation.board

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.BoardPost
import com.damoim.app.domain.model.PostDraft
import com.damoim.app.domain.usecase.GetPostDetailUseCase
import com.damoim.app.domain.usecase.SubmitPostUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PostWriteUiState(
    val isSaving: Boolean = false,
    /** 수정 모드일 때 원본 글(프리필용). 로드 전 null. */
    val editing: BoardPost? = null,
    val editLoaded: Boolean = false,
) : UiState

sealed interface PostWriteSideEffect : UiSideEffect {
    data class Toast(val message: String) : PostWriteSideEffect
    data object Done : PostWriteSideEffect
}

/** 화면 15/34/35/39/70 게시글 작성/수정 제출. */
class PostWriteViewModel(
    private val submitPost: SubmitPostUseCase,
    private val getPostDetail: GetPostDetailUseCase,
    private val editPostId: Long?,
) : BaseViewModel<PostWriteUiState, PostWriteSideEffect>(PostWriteUiState()) {

    init {
        if (editPostId != null) {
            viewModelScope.launch {
                val detail = getPostDetail(editPostId).first()
                setState { copy(editing = detail?.post, editLoaded = true) }
            }
        } else {
            setState { copy(editLoaded = true) }
        }
    }

    fun submit(draft: PostDraft) {
        if (currentState.isSaving) return
        if (draft.title.isBlank()) {
            sendEffect(PostWriteSideEffect.Toast(DamoimStrings.WRITE_TITLE_REQUIRED))
            return
        }
        setState { copy(isSaving = true) }
        viewModelScope.launch {
            val result = if (editPostId != null) {
                submitPost.update(editPostId, draft)
            } else {
                submitPost.create(draft)
            }
            setState { copy(isSaving = false) }
            handleResult(result, onSuccess = { sendEffect(PostWriteSideEffect.Done) })
        }
    }
}
