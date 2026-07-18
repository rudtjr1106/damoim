package com.damoim.app.presentation.board.write

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.BoardPost
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.PostDraft
import com.damoim.app.domain.usecase.GetPostDetailUseCase
import com.damoim.app.domain.usecase.ObserveMyContextUseCase
import com.damoim.app.domain.usecase.SubmitPostUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PostWriteUiState(
    val isSaving: Boolean = false,
    /** 수정 모드일 때 원본 글(프리필용). 로드 전 null. */
    val editing: BoardPost? = null,
    val editLoaded: Boolean = false,
    /** 동아리장 여부 — 공지 카테고리 선택은 운영진 전용. */
    val isAdmin: Boolean = false,
) : UiState

sealed interface PostWriteSideEffect : UiSideEffect {
    data class Toast(val message: String) : PostWriteSideEffect
    data object Done : PostWriteSideEffect
}

/** 화면 15/34/35/39/70 게시글 작성/수정 제출 + 임시저장. */
class PostWriteViewModel(
    private val submitPost: SubmitPostUseCase,
    private val getPostDetail: GetPostDetailUseCase,
    observeMyContext: ObserveMyContextUseCase,
    private val editPostId: Long?,
) : BaseViewModel<PostWriteUiState, PostWriteSideEffect>(PostWriteUiState()) {

    init {
        if (editPostId != null) {
            viewModelScope.launch {
                // 공유 replay 플로우가 첫 방출로 null/실패값을 줄 수 있어, 첫 유효 상세를 기다린다.
                // (기존 .first()는 그 null을 잡아 사진 등 첨부가 빈 채로 편집이 열리곤 했다.)
                val detail = getPostDetail(editPostId).filterNotNull().first()
                setState { copy(editing = detail.post, editLoaded = true) }
            }
        } else {
            setState { copy(editLoaded = true) }
        }
        // 현재 사용자의 역할을 관찰 — 공지 선택 권한 판정용.
        viewModelScope.launch {
            observeMyContext().collect { ctx -> setState { copy(isAdmin = ctx.role == ClubRole.LEADER) } }
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
            handleResult(
                result,
                onSuccess = { sendEffect(PostWriteSideEffect.Done) },
                // 서버 거절(예: 투표 항목 부족)을 조용히 삼키지 않고 표시한다.
                onFailure = { error -> sendEffect(PostWriteSideEffect.Toast(error.message)) },
            )
        }
    }

    /** 진입 시점의 임시저장 초안 조회 — 화면(Route)에서 컴포지션마다 읽는다(VM 재사용 대비). */
    fun loadDraft(): PostDraft? = submitPost.loadDraft()

    fun saveDraft(draft: PostDraft) = viewModelScope.launch {
        handleResult(submitPost.saveDraft(draft), onSuccess = {
            sendEffect(PostWriteSideEffect.Toast(DamoimStrings.TOAST_DRAFT_SAVED))
        })
    }
}
