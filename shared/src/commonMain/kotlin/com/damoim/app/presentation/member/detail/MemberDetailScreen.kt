package com.damoim.app.presentation.member.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.MemberDetail
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.domain.model.MemberStatus
import com.damoim.app.platform.PlatformBackHandler
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.CrownIcon
import com.damoim.app.presentation.component.ListIcon
import com.damoim.app.presentation.component.PersonMinusIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.member.DetailBadge
import com.damoim.app.presentation.member.InfoRow
import com.damoim.app.presentation.member.MemberAvatar
import com.damoim.app.presentation.member.list.previewCohorts
import com.damoim.app.presentation.member.memberRoleLabel
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

private sealed interface DetailOverlay {
    data object Cohort : DetailOverlay
    data object Role : DetailOverlay
    data object Remove : DetailOverlay
}

/** 화면 18 회원 상세 — Route. */
@Composable
fun MemberDetailRoute(
    memberId: Long,
    viewModel: MemberDetailViewModel = viewModel(key = "member_$memberId") {
        MemberDetailViewModel(AppGraph.getMemberDetailUseCase, AppGraph.getCohortsUseCase, AppGraph.observeMyContextUseCase, AppGraph.memberActionUseCase, memberId)
    },
    onBack: () -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is MemberDetailSideEffect.Toast -> onToast(effect.message)
                MemberDetailSideEffect.Removed -> { onBack(); onToast(DamoimStrings.TOAST_MEMBER_REMOVED) }
            }
        }
    }
    MemberDetailScreen(state, onBack, viewModel::onChangeCohort, viewModel::onChangeRole, viewModel::onRemove)
}

@Composable
fun MemberDetailScreen(
    state: MemberDetailUiState = MemberDetailUiState(isLoading = false, detail = previewDetail(), cohorts = previewCohorts(), isLeader = true),
    onBack: () -> Unit = {},
    onChangeCohort: (Long) -> Unit = {},
    onChangeRole: (MemberRole) -> Unit = {},
    onRemove: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    val detail = state.detail
    var overlay by remember { mutableStateOf<DetailOverlay?>(null) }
    PlatformBackHandler(enabled = overlay != null) { overlay = null }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(colors.surfaceInput)) {
            Row(Modifier.fillMaxWidth().background(colors.surface).windowInsetsPadding(WindowInsets.statusBars).padding(start = 16.dp, end = 20.dp, top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(24.dp).noRippleClick(onBack), contentAlignment = Alignment.Center) { BackChevronIcon(colors.textPrimary, Modifier.size(24.dp)) }
                Spacer(Modifier.width(8.dp))
                Text(DamoimStrings.MEMBER_DETAIL_TITLE, style = DamoimTheme.typography.titleMedium, color = colors.textPrimary)
            }
            if (detail != null) {
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding()) {
                    ProfileBlock(detail)
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        InfoCard(detail)
                        if (state.canManage) LeaderActions(detail, onCohort = { overlay = DetailOverlay.Cohort }, onRole = { overlay = DetailOverlay.Role }, onRemove = { overlay = DetailOverlay.Remove })
                    }
                }
            } else {
                Spacer(Modifier.weight(1f))
            }
        }

        when (overlay) {
            DetailOverlay.Cohort -> detail?.let {
                CohortChangeSheet(
                    memberName = it.member.name, currentCohortLabel = it.cohortLabel,
                    cohorts = state.cohorts, currentCohortId = it.member.cohortId,
                    onDismiss = { overlay = null },
                    onConfirm = { cid -> overlay = null; onChangeCohort(cid) },
                )
            }
            DetailOverlay.Role -> detail?.let {
                RoleChangeSheet(
                    memberName = it.member.name, currentRole = it.member.role,
                    onDismiss = { overlay = null },
                    onConfirm = { role -> overlay = null; onChangeRole(role) },
                )
            }
            DetailOverlay.Remove -> detail?.let {
                RemoveMemberDialog(it.member.name, onDismiss = { overlay = null }, onConfirm = { overlay = null; onRemove() })
            }
            null -> Unit
        }
    }
}

@Composable
private fun ProfileBlock(detail: MemberDetail) {
    val colors = DamoimTheme.colors
    val m = detail.member
    Column(
        Modifier.fillMaxWidth().background(colors.surface).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MemberAvatar(m, size = 84.dp, fontSize = 24.sp)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(m.name, style = DamoimTheme.typography.titleLarge, color = colors.textPrimary)
                if (m.role == MemberRole.LEADER) CrownIcon(colors.primary, Modifier.size(16.dp))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DetailBadge(detail.cohortLabel.substringAfterLast('(').removeSuffix(")").ifBlank { detail.cohortLabel }, emphasized = true)
            DetailBadge(memberRoleLabel(m.role), emphasized = false)
            DetailBadge(if (m.status == MemberStatus.ACTIVE) DamoimStrings.MEMBER_STATUS_ACTIVE else DamoimStrings.MEMBER_STATUS_DORMANT, emphasized = m.status == MemberStatus.ACTIVE)
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
}

@Composable
private fun InfoCard(detail: MemberDetail) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(colors.surface).padding(horizontal = 18.dp, vertical = 6.dp)) {
        InfoRow(DamoimStrings.MEMBER_INFO_JOINED, detail.member.joinedLabel)
        InfoRow(DamoimStrings.MEMBER_INFO_POSTS, DamoimStrings.memberInfoPosts(detail.postCount))
        InfoRow(DamoimStrings.MEMBER_INFO_EVENTS, DamoimStrings.memberInfoEvents(detail.eventCount))
        InfoRow(DamoimStrings.MEMBER_INFO_LAST_ACTIVE, detail.lastActiveLabel, showDivider = false)
    }
}

@Composable
private fun LeaderActions(detail: MemberDetail, onCohort: () -> Unit, onRole: () -> Unit, onRemove: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(DamoimStrings.MEMBER_LEADER_ACTIONS, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp), color = colors.textMuted, modifier = Modifier.padding(horizontal = 4.dp))
        Column(Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(colors.surface).padding(horizontal = 18.dp, vertical = 6.dp)) {
            ActionRow({ ListIcon(colors.textSecondary, Modifier.size(18.dp)) }, DamoimStrings.MEMBER_CHANGE_COHORT, trailing = detail.cohortLabel.substringAfterLast('(').removeSuffix(")"), trailingEmphasis = true, onClick = onCohort)
            ActionRow({ CrownIcon(colors.textSecondary, Modifier.size(18.dp)) }, DamoimStrings.MEMBER_CHANGE_ROLE, trailing = memberRoleLabel(detail.member.role), trailingEmphasis = false, onClick = onRole)
            ActionRow({ PersonMinusIcon(colors.error, Modifier.size(18.dp)) }, DamoimStrings.MEMBER_REMOVE, destructive = true, showDivider = false, onClick = onRemove)
        }
    }
}

@Composable
private fun ActionRow(icon: @Composable () -> Unit, title: String, trailing: String? = null, trailingEmphasis: Boolean = false, destructive: Boolean = false, showDivider: Boolean = true, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Column {
        Row(Modifier.fillMaxWidth().noRippleClick(onClick).padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            icon()
            Text(title, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = if (destructive) colors.error else colors.textPrimary, modifier = Modifier.weight(1f))
            if (trailing != null) Text(trailing, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = if (trailingEmphasis) colors.primary else colors.textMuted)
        }
        if (showDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
    }
}

internal fun previewDetail() = MemberDetail(
    member = com.damoim.app.domain.model.Member(501, "이서연", "서연", 24, MemberRole.MEMBER, joinedLabel = "2024.09.15"),
    cohortLabel = "2024학년 1기 (24기)", postCount = 14, eventCount = 8, lastActiveLabel = "1시간 전",
)

@Preview
@Composable
private fun MemberDetailScreenPreview() {
    DamoimTheme { MemberDetailScreen() }
}
