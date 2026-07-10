package com.damoim.app.presentation.resource

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.domain.model.ResourceFolder
import com.damoim.app.domain.repository.StorageUsage
import com.damoim.app.domain.usecase.GetResourcesUseCase
import com.damoim.app.domain.usecase.GetStorageUsageUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

data class ArchiveUiState(
    val isLoading: Boolean = true,
    val resources: List<ResourceFile> = emptyList(),  // 선택된 폴더 기준 목록
    val storage: StorageUsage? = null,                // 저장공간(항상 전체 기준)
    val folder: ResourceFolder? = null,               // null = 전체 칩
) : UiState {
    /**
     * 자료실 자체가 비어있음(신규 동아리) — 폴더 필터로 인한 빈 목록과 구분한다.
     * storage가 아직 null이면 판단을 보류한다(목록만 먼저 도착한 프레임에서 빈 상태가 번쩍이는 것 방지).
     */
    val isEmpty: Boolean get() = !isLoading && storage != null && storage.count == 0

    /** 자료는 있는데 이 폴더만 비어있음. */
    val isFolderEmpty: Boolean get() = !isLoading && !isEmpty && resources.isEmpty()
}

sealed interface ArchiveSideEffect : UiSideEffect

/** 화면 67 자료실 홈. 폴더 칩 전환 시 스토어를 다시 구독하고, 저장공간은 업로드/삭제에 자동 반영된다. */
class ArchiveViewModel(
    getResources: GetResourcesUseCase,
    getStorageUsage: GetStorageUsageUseCase,
) : BaseViewModel<ArchiveUiState, ArchiveSideEffect>(ArchiveUiState()) {

    private val folderFlow = MutableStateFlow<ResourceFolder?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeResources(getResources: GetResourcesUseCase) = viewModelScope.launch {
        folderFlow.flatMapLatest { folder -> getResources(folder) }.collect { list ->
            setState { copy(isLoading = false, resources = list) }
        }
    }

    init {
        observeResources(getResources)
        viewModelScope.launch {
            getStorageUsage().collect { usage -> setState { copy(storage = usage) } }
        }
    }

    fun onSelectFolder(folder: ResourceFolder?) {
        if (currentState.folder == folder) return
        setState { copy(folder = folder) }
        folderFlow.value = folder
    }
}
