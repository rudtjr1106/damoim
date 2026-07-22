package com.damoim.app.presentation.resource.archive

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.Cohort
import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.domain.model.ResourceFolder
import com.damoim.app.domain.model.ResourceVisibility
import com.damoim.app.domain.repository.StorageUsage
import com.damoim.app.domain.usecase.GetCohortsUseCase
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
    val cohorts: List<Cohort> = emptyList(),          // 기수 필터 칩 목록
    val selectedCohortId: Long? = null,               // null = 전체 기수
) : UiState {
    /**
     * 폴더 목록에 기수 필터를 적용한 최종 표시 목록. cohortIds는 목록 항목마다 이미 채워져
     * 있으므로 서버 재조회가 없다. 기수를 고르면 그 기수가 볼 수 있는 자료(전체 공개 +
     * 해당 기수 한정)를 보여준다. (자료 검색은 전용 검색 화면이 담당)
     */
    val visibleResources: List<ResourceFile> get() {
        val cohortId = selectedCohortId
        return resources.filter { r ->
            cohortId == null || r.visibility == ResourceVisibility.ALL_MEMBERS || cohortId in r.cohortIds
        }
    }

    /** 폴더/기수 필터가 하나라도 걸려 있으면 카운트/빈 상태를 필터 결과 기준으로 판단한다. */
    val isFiltered: Boolean get() = folder != null || selectedCohortId != null

    /**
     * 자료실 자체가 비어있음(신규 동아리) — 폴더 필터로 인한 빈 목록과 구분한다.
     * storage가 아직 null이면 판단을 보류한다(목록만 먼저 도착한 프레임에서 빈 상태가 번쩍이는 것 방지).
     */
    val isEmpty: Boolean get() = !isLoading && storage != null && storage.count == 0

    /** 자료는 있는데 (폴더/기수) 필터 결과만 비어있음. */
    val isFolderEmpty: Boolean get() = !isLoading && !isEmpty && visibleResources.isEmpty()
}

sealed interface ArchiveSideEffect : UiSideEffect

/** 화면 67 자료실 홈. 폴더 칩 전환 시 스토어를 다시 구독하고, 저장공간은 업로드/삭제에 자동 반영된다. */
class ArchiveViewModel(
    getResources: GetResourcesUseCase,
    getStorageUsage: GetStorageUsageUseCase,
    getCohorts: GetCohortsUseCase,
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
        viewModelScope.launch {
            getCohorts().collect { list -> setState { copy(cohorts = list) } }
        }
    }

    fun onSelectFolder(folder: ResourceFolder?) {
        if (currentState.folder == folder) return
        setState { copy(folder = folder) }
        folderFlow.value = folder
    }

    /** 기수 필터는 이미 받아온 목록을 클라에서 거르므로 서버 재조회가 없다. */
    fun onSelectCohort(cohortId: Long?) {
        if (currentState.selectedCohortId == cohortId) return
        setState { copy(selectedCohortId = cohortId) }
    }
}
