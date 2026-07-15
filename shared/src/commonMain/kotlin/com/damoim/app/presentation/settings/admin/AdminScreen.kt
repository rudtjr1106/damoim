package com.damoim.app.presentation.settings.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.AdminMember
import com.damoim.app.domain.model.PermissionType
import com.damoim.app.presentation.component.InfoIcon
import com.damoim.app.presentation.component.MoreIcon
import com.damoim.app.presentation.component.NetworkAvatar
import com.damoim.app.presentation.component.PlusIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.settings.DamoimSwitch
import com.damoim.app.presentation.settings.SettingsTopBar
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

@Composable
fun AdminRoute(
    viewModel: AdminViewModel = viewModel(key = "admin") { AdminViewModel(AppGraph.adminPermissionUseCase) },
    onBack: () -> Unit = {},
    onOpenMember: (Long) -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.sideEffect.collect { if (it is AdminSideEffect.Toast) onToast(it.message) } }
    AdminScreen(state, onBack, viewModel::toggle, viewModel::addAdmin, viewModel::removeAdmin, viewModel::changeTitle, onOpenMember)
}

private sealed interface AdminOverlay {
    data object AddSheet : AdminOverlay
    data class Menu(val admin: AdminMember) : AdminOverlay
    data class TitleDialog(val admin: AdminMember) : AdminOverlay
    data class RemoveConfirm(val admin: AdminMember) : AdminOverlay
}

@Composable
fun AdminScreen(
    state: AdminUiState = AdminUiState(),
    onBack: () -> Unit = {},
    onToggle: (Long, PermissionType) -> Unit = { _, _ -> },
    onAddAdmin: (Long) -> Unit = {},
    onRemoveAdmin: (Long) -> Unit = {},
    onChangeTitle: (Long, String) -> Unit = { _, _ -> },
    onOpenMember: (Long) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    var overlay by remember { mutableStateOf<AdminOverlay?>(null) }

    Box(Modifier.fillMaxSize().background(colors.surfaceInput)) {
        Column(Modifier.fillMaxSize()) {
            SettingsTopBar(DamoimStrings.ADMIN_TITLE, onBack, trailing = {
                Row(Modifier.noRippleClick { overlay = AdminOverlay.AddSheet }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PlusIcon(colors.primary, Modifier.size(13.dp))
                    Text(DamoimStrings.ADMIN_ADD, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = colors.primary)
                }
            })
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                state.admins.forEach { admin ->
                    AdminCard(admin, onToggle = { type -> onToggle(admin.userId, type) }, onMenu = { overlay = AdminOverlay.Menu(admin) })
                }
                // 안내 배너
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.primaryContainer).padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoIcon(colors.primary, Modifier.size(18.dp))
                    Text(DamoimStrings.ADMIN_INFO, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp), color = colors.primaryDeep)
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        when (val o = overlay) {
            AdminOverlay.AddSheet -> AdminAddSheet(state.assignable, onSelect = { overlay = null; onAddAdmin(it) }, onDismiss = { overlay = null })
            is AdminOverlay.Menu -> AdminMenuSheet(
                admin = o.admin,
                onChangeTitle = { overlay = AdminOverlay.TitleDialog(o.admin) },
                onDetail = { overlay = null; onOpenMember(o.admin.userId) },
                onRemove = { overlay = AdminOverlay.RemoveConfirm(o.admin) },
                onDismiss = { overlay = null },
            )
            is AdminOverlay.TitleDialog -> AdminTitleDialog(o.admin, onSave = { overlay = null; onChangeTitle(o.admin.userId, it) }, onDismiss = { overlay = null })
            is AdminOverlay.RemoveConfirm -> com.damoim.app.presentation.component.DamoimConfirmDialog(
                title = DamoimStrings.ADMIN_REMOVE_TITLE, desc = DamoimStrings.adminRemoveDesc(o.admin.name),
                confirm = DamoimStrings.ADMIN_REMOVE_CONFIRM, destructive = true,
                onConfirm = { overlay = null; onRemoveAdmin(o.admin.userId) }, onDismiss = { overlay = null },
            )
            null -> {}
        }
    }
}

@Composable
private fun AdminCard(admin: AdminMember, onToggle: (PermissionType) -> Unit, onMenu: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(colors.surface).padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NetworkAvatar(url = admin.imageUrl, size = 44.dp) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(colors.primaryContainerHigh), contentAlignment = Alignment.Center) {
                    Text(admin.initials, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 13.sp), color = colors.primaryDeep)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(admin.name, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = colors.textPrimary)
                Text("${admin.cohortLabel} · ${admin.title}", style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
            }
            Box(Modifier.size(24.dp).noRippleClick(onMenu), contentAlignment = Alignment.Center) { MoreIcon(colors.outlineStrong, Modifier.size(20.dp)) }
        }
        Column {
            PermissionType.entries.forEachIndexed { i, type ->
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(type.label, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.5.sp), color = colors.textSecondary, modifier = Modifier.weight(1f))
                    DamoimSwitch(on = type in admin.permissions, onToggle = { onToggle(type) })
                }
                if (i < PermissionType.entries.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
            }
        }
    }
}
