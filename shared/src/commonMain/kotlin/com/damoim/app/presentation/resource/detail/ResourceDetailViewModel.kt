package com.damoim.app.presentation.resource.detail

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.Cohort
import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.domain.usecase.GetCohortsUseCase
import com.damoim.app.domain.usecase.GetResourceDetailUseCase
import com.damoim.app.domain.usecase.ObserveMyContextUseCase
import com.damoim.app.domain.usecase.ResourceActionUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.launch

data class ResourceDetailUiState(
    val isLoading: Boolean = true,
    val resource: ResourceFile? = null,
    val myUserId: Long = 0,
    val isLeader: Boolean = false,
    val cohorts: List<Cohort> = emptyList(),
) : UiState {
    /** 내가 올린 자료이거나 동아리장이면 삭제할 수 있다. */
    val canDelete: Boolean get() = resource != null && (resource.uploaderId == myUserId || isLeader)

    /** 공개 대상 기수 요약("24기 · 25기"). 전체 공개면 빈 문자열. */
    val cohortSummary: String
        get() = resource?.cohortIds.orEmpty()
            .let { ids -> cohorts.filter { it.id in ids } }
            .joinToString(" · ") { it.short }
}

sealed interface ResourceDetailSideEffect : UiSideEffect {
    data class Toast(val message: String) : ResourceDetailSideEffect
    data object Deleted : ResourceDetailSideEffect
}

/**
 * 화면 68 자료 상세. 다운로드는 카운트를 실제로 올리고(스토어), 삭제는 목록·저장공간에 즉시 반영된다.
 */
class ResourceDetailViewModel(
    getResourceDetail: GetResourceDetailUseCase,
    observeMyContext: ObserveMyContextUseCase,
    getCohorts: GetCohortsUseCase,
    private val resourceAction: ResourceActionUseCase,
    private val resourceId: Long,
) : BaseViewModel<ResourceDetailUiState, ResourceDetailSideEffect>(ResourceDetailUiState()) {

    init {
        viewModelScope.launch {
            getResourceDetail(resourceId).collect { resource ->
                setState { copy(isLoading = false, resource = resource) }
            }
        }
        viewModelScope.launch {
            observeMyContext().collect { ctx ->
                setState { copy(myUserId = ctx.userId, isLeader = ctx.role == ClubRole.LEADER) }
            }
        }
        viewModelScope.launch {
            getCohorts().collect { list -> setState { copy(cohorts = list) } }
        }
    }

    fun onDownload() = viewModelScope.launch {
        handleResult(resourceAction.download(resourceId), onSuccess = {
            sendEffect(ResourceDetailSideEffect.Toast(DamoimStrings.TOAST_FILE_DOWNLOADED))
        })
    }

    fun onDelete() = viewModelScope.launch {
        handleResult(resourceAction.delete(resourceId), onSuccess = {
            sendEffect(ResourceDetailSideEffect.Deleted)
        })
    }
}
