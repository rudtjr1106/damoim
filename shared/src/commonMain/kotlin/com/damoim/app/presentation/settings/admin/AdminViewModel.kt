package com.damoim.app.presentation.settings.admin

import androidx.lifecycle.viewModelScope
import com.damoim.app.core.mvi.BaseViewModel
import com.damoim.app.core.mvi.UiSideEffect
import com.damoim.app.core.mvi.UiState
import com.damoim.app.domain.model.AdminMember
import com.damoim.app.domain.model.Member
import com.damoim.app.domain.model.PermissionType
import com.damoim.app.domain.usecase.AdminPermissionUseCase
import com.damoim.app.presentation.theme.DamoimStrings
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class AdminUiState(
    val admins: List<AdminMember> = emptyList(),
    val assignable: List<Member> = emptyList(),
) : UiState

sealed interface AdminSideEffect : UiSideEffect {
    data class Toast(val message: String) : AdminSideEffect
}

/** 30 운영진 권한 관리 + 64 ⋯ 메뉴(직함 변경·해제). */
class AdminViewModel(
    private val adminPermission: AdminPermissionUseCase,
) : BaseViewModel<AdminUiState, AdminSideEffect>(AdminUiState()) {

    init {
        viewModelScope.launch {
            combine(adminPermission.observeAdmins(), adminPermission.observeAssignable()) { admins, assignable -> admins to assignable }
                .collect { (admins, assignable) -> setState { copy(admins = admins, assignable = assignable) } }
        }
    }

    fun toggle(userId: Long, type: PermissionType) = viewModelScope.launch { adminPermission.toggle(userId, type) }

    fun addAdmin(memberId: Long) = viewModelScope.launch {
        adminPermission.add(memberId, DamoimStrings.ADMIN_DEFAULT_TITLE)
        sendEffect(AdminSideEffect.Toast(DamoimStrings.TOAST_ADMIN_ADDED))
    }

    fun removeAdmin(userId: Long) = viewModelScope.launch {
        adminPermission.remove(userId)
        sendEffect(AdminSideEffect.Toast(DamoimStrings.TOAST_ADMIN_REMOVED))
    }

    fun changeTitle(userId: Long, title: String) = viewModelScope.launch {
        adminPermission.changeTitle(userId, title.trim().ifBlank { DamoimStrings.ADMIN_DEFAULT_TITLE })
        sendEffect(AdminSideEffect.Toast(DamoimStrings.TOAST_ADMIN_TITLE_CHANGED))
    }
}
