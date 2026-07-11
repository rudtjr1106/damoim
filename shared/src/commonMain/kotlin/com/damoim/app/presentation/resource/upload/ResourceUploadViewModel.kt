package com.damoim.app.presentation.resource.upload

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.Cohort
import com.damoim.app.domain.model.ResourceDraft
import com.damoim.app.domain.usecase.GetCohortsUseCase
import com.damoim.app.domain.usecase.ObserveMyContextUseCase
import com.damoim.app.domain.usecase.UploadResourceUseCase
import kotlinx.coroutines.launch

data class ResourceUploadUiState(
    val isSubmitting: Boolean = false,
    val role: ClubRole? = null,        // null = 아직 모름 (기본 폴더 결정을 미룬다)
    val cohorts: List<Cohort> = emptyList(),
) : UiState {
    val isLeader: Boolean get() = role == ClubRole.LEADER
}

sealed interface ResourceUploadSideEffect : UiSideEffect {
    data class Toast(val message: String) : ResourceUploadSideEffect
    data object Uploaded : ResourceUploadSideEffect
}

/**
 * 화면 69 자료 올리기. 폼 상태(선택한 파일·제목·설명·폴더·공개범위)는 화면의 remember가 들고,
 * ViewModel은 역할/기수 구독과 제출만 담당한다 — 수동 백스택에서 VM이 재사용되므로
 * 폼을 VM에 두면 다시 들어왔을 때 이전 입력이 남는다.
 */
class ResourceUploadViewModel(
    private val uploadResource: UploadResourceUseCase,
    observeMyContext: ObserveMyContextUseCase,
    getCohorts: GetCohortsUseCase,
) : BaseViewModel<ResourceUploadUiState, ResourceUploadSideEffect>(ResourceUploadUiState()) {

    init {
        viewModelScope.launch {
            observeMyContext().collect { ctx -> setState { copy(role = ctx.role) } }
        }
        viewModelScope.launch {
            getCohorts().collect { list -> setState { copy(cohorts = list) } }
        }
    }

    fun onSubmit(draft: ResourceDraft) {
        if (currentState.isSubmitting) return
        setState { copy(isSubmitting = true) }
        viewModelScope.launch {
            val result = uploadResource(draft)
            setState { copy(isSubmitting = false) }
            handleResult(
                result,
                onSuccess = { sendEffect(ResourceUploadSideEffect.Uploaded) },
                onFailure = { error -> sendEffect(ResourceUploadSideEffect.Toast(error.message)) },
            )
        }
    }
}
