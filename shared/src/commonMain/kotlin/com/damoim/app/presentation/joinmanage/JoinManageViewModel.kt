package com.damoim.app.presentation.joinmanage

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.JoinApplicant
import com.damoim.app.domain.model.ProcessedApplicant
import com.damoim.app.domain.usecase.DecideApplicantUseCase
import com.damoim.app.domain.usecase.GetJoinApplicantsUseCase
import kotlinx.coroutines.launch

/** 09 탭 — 대기 / 처리 완료. */
enum class JoinManageTab { PENDING, DONE }

data class JoinManageUiState(
    val isLoading: Boolean = true,
    val pending: List<JoinApplicant> = emptyList(),
    val processed: List<ProcessedApplicant> = emptyList(),
    val tab: JoinManageTab = JoinManageTab.PENDING,
) : UiState

sealed interface JoinManageSideEffect : UiSideEffect {
    data class Toast(val message: String) : JoinManageSideEffect
}

/** 화면 09 가입 신청 관리. 승인/거절이 스토어에 반영돼 처리 완료 탭·홈 통계·알림까지 갱신된다. */
class JoinManageViewModel(
    getApplicants: GetJoinApplicantsUseCase,
    private val decideApplicant: DecideApplicantUseCase,
) : BaseViewModel<JoinManageUiState, JoinManageSideEffect>(JoinManageUiState()) {

    init {
        viewModelScope.launch {
            getApplicants().collect { board ->
                setState { copy(isLoading = false, pending = board.pending, processed = board.processed) }
            }
        }
    }

    fun onSelectTab(tab: JoinManageTab) = setState { copy(tab = tab) }

    fun onDecide(applicant: JoinApplicant, approve: Boolean) = viewModelScope.launch {
        handleResult(
            decideApplicant(applicant.id, approve),
            onSuccess = {
                sendEffect(JoinManageSideEffect.Toast("${applicant.name}님을 ${if (approve) "승인" else "거절"}했어요"))
            },
            // 41 회원 정원 초과 등 서버 거절 메시지를 그대로 표면화(기존엔 조용히 무시됐다).
            onFailure = { sendEffect(JoinManageSideEffect.Toast(it.message)) },
        )
    }
}
