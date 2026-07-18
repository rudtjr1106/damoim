package com.damoim.app.presentation.profile.myprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.platform.PlatformBackHandler
import com.damoim.app.presentation.board.InitialAvatar
import com.damoim.app.presentation.component.NetworkAvatar
import com.damoim.app.presentation.component.BackChevronIcon
import com.damoim.app.presentation.component.BellIcon
import com.damoim.app.presentation.component.CameraIcon
import com.damoim.app.presentation.component.ChevronRightIcon
import com.damoim.app.presentation.component.DoorExitIcon
import com.damoim.app.presentation.component.KakaoBubbleIcon
import com.damoim.app.presentation.component.EditIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.member.DetailBadge
import com.damoim.app.presentation.member.InfoRow
import com.damoim.app.presentation.member.memberRoleLabel
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

private sealed interface ProfileOverlay {
    data object Logout : ProfileOverlay
    data object Leave : ProfileOverlay
}

/**
 * 화면 20 내 프로필 — Route. 동아리 전환은 세션만 갈아타고, 로그아웃/탈퇴/새 참여는 Auth로 복귀한다.
 */
@Composable
fun MyProfileRoute(
    viewModel: MyProfileViewModel = viewModel(key = "my_profile") {
        MyProfileViewModel(AppGraph.getMyMemberUseCase, AppGraph.getCohortsUseCase, AppGraph.getClubInfoUseCase, AppGraph.observeMyContextUseCase, AppGraph.clubSessionUseCase, AppGraph.logoutUseCase)
    },
    onBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onLoggedOut: () -> Unit = {},              // 로그아웃 → 로그인
    onWithdrewToClub: () -> Unit = {},         // 탈퇴 후 잔존 → 새 동아리 홈
    onWithdrewToOnboarding: () -> Unit = {},   // 탈퇴 후 없음 → 온보딩(재로그인 X)
    onOpenNotification: () -> Unit = {},
    onError: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                MyProfileSideEffect.WithdrewToClub -> onWithdrewToClub()
                MyProfileSideEffect.WithdrewNoClub -> onWithdrewToOnboarding()
                MyProfileSideEffect.LoggedOut -> onLoggedOut()
                is MyProfileSideEffect.ActionFailed -> onError(effect.message)
            }
        }
    }
    MyProfileScreen(
        state = state,
        onBack = onBack,
        onEditProfile = onEditProfile,
        onLogout = viewModel::onLogout,
        onWithdraw = viewModel::onWithdraw,
        onOpenNotification = onOpenNotification,
    )
}

@Composable
fun MyProfileScreen(
    state: MyProfileUiState = MyProfileUiState("이서연", "서연", "24기", "2024학년 1기 (24기)", MemberRole.MEMBER, "2024.09.15", "코딩하는 사람들"),
    onBack: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    onWithdraw: () -> Unit = {},
    onOpenNotification: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    var overlay by remember { mutableStateOf<ProfileOverlay?>(null) }
    PlatformBackHandler(enabled = overlay != null) { overlay = null }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(colors.surfaceInput)) {
            Row(Modifier.fillMaxWidth().background(colors.surface).windowInsetsPadding(WindowInsets.statusBars).padding(start = 16.dp, end = 20.dp, top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(24.dp).noRippleClick(onBack), contentAlignment = Alignment.Center) { BackChevronIcon(colors.textPrimary, Modifier.size(24.dp)) }
                Spacer(Modifier.width(8.dp))
                Text(DamoimStrings.MY_PROFILE_TITLE, style = DamoimTheme.typography.titleMedium, color = colors.textPrimary)
            }
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding()) {
                Hero(state, onEditProfile)
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoCard(state)
                    ActionCard(state, onEditProfile, onNotification = onOpenNotification)
                    DangerCard(onLogout = { overlay = ProfileOverlay.Logout }, onLeave = { overlay = ProfileOverlay.Leave })
                    Text(DamoimStrings.APP_VERSION, style = DamoimTheme.typography.label, color = colors.outlineStrong, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }

        when (overlay) {
            ProfileOverlay.Logout -> ConfirmDialog(
                DamoimStrings.LOGOUT_TITLE, DamoimStrings.LOGOUT_BODY, confirmLabel = DamoimStrings.LOGOUT_CONFIRM, destructive = false,
                onDismiss = { overlay = null }, onConfirm = { overlay = null; onLogout() },
            )
            ProfileOverlay.Leave -> ConfirmDialog(
                DamoimStrings.clubLeaveTitle(state.currentClubName), DamoimStrings.CLUB_LEAVE_BODY, note = DamoimStrings.CLUB_LEAVE_NOTE, confirmLabel = DamoimStrings.CLUB_LEAVE_CONFIRM, destructive = true,
                icon = { DoorExitIcon(it, Modifier.size(26.dp)) },
                onDismiss = { overlay = null }, onConfirm = { overlay = null; onWithdraw() },
            )
            null -> Unit
        }
    }
}

@Composable
private fun Hero(state: MyProfileUiState, onEdit: () -> Unit) {
    val colors = DamoimTheme.colors
    Row(Modifier.fillMaxWidth().background(colors.surface).padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box {
            NetworkAvatar(url = state.profileImageUrl, size = 72.dp) {
                InitialAvatar(state.initials.ifBlank { state.name.takeLast(2) }, size = 72.dp, fontSize = 20.sp)
            }
            Box(
                Modifier.align(Alignment.BottomEnd).size(26.dp).clip(CircleShape).background(colors.surface).border(1.dp, colors.divider, CircleShape).noRippleClick(onEdit),
                contentAlignment = Alignment.Center,
            ) { CameraIcon(colors.textSecondary, Modifier.size(12.dp)) }
        }
        Column(Modifier.weight(1f)) {
            Text(state.name, style = DamoimTheme.typography.titleLarge.copy(fontSize = 19.sp), color = colors.textPrimary)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                // 기수가 없으면 빈 파란 배지가 남지 않도록 숨긴다.
                if (state.cohortShort.isNotBlank()) DetailBadge(state.cohortShort, emphasized = true)
                DetailBadge(memberRoleLabel(state.role), emphasized = false)
            }
        }
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
}

@Composable
private fun InfoCard(state: MyProfileUiState) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(colors.surface).padding(horizontal = 18.dp, vertical = 6.dp)) {
        InfoRow(DamoimStrings.PROFILE_INFO_COHORT, state.cohortLabel, valueColor = colors.primaryDark)
        InfoRow(DamoimStrings.MEMBER_INFO_JOINED, state.joinedLabel)
        LinkedRow()
    }
}

@Composable
private fun LinkedRow() {
    val colors = DamoimTheme.colors
    Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(DamoimStrings.PROFILE_INFO_LINKED, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp), color = colors.textMuted, modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(Modifier.size(18.dp).clip(RoundedCornerShape(5.dp)).background(colors.kakao), contentAlignment = Alignment.Center) {
                KakaoBubbleIcon(colors.onKakao, Modifier.size(11.dp))
            }
            Text(DamoimStrings.PROFILE_LINKED_KAKAO, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = colors.textPrimary)
        }
    }
}

@Composable
private fun ActionCard(state: MyProfileUiState, onEdit: () -> Unit, onNotification: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(colors.surface).padding(horizontal = 18.dp, vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth().noRippleClick(onEdit).padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EditIcon(colors.textSecondary, Modifier.size(18.dp))
            Text(DamoimStrings.PROFILE_ROW_EDIT, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
            ChevronRightIcon(colors.outlineStrong, Modifier.size(16.dp))
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
        Row(Modifier.fillMaxWidth().noRippleClick(onNotification).padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BellIcon(colors.textSecondary, Modifier.size(18.dp))
            Text(DamoimStrings.PROFILE_ROW_NOTIFICATION, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
            FakeToggle(on = true)
        }
    }
}

@Composable
private fun FakeToggle(on: Boolean) {
    val colors = DamoimTheme.colors
    Box(Modifier.size(width = 44.dp, height = 26.dp).clip(RoundedCornerShape(999.dp)).background(if (on) colors.primary else colors.outline)) {
        Box(Modifier.align(if (on) Alignment.CenterEnd else Alignment.CenterStart).padding(horizontal = 3.dp).size(20.dp).clip(CircleShape).background(colors.surface))
    }
}

@Composable
private fun DangerCard(onLogout: () -> Unit, onLeave: () -> Unit) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(colors.surface).padding(horizontal = 18.dp, vertical = 6.dp)) {
        Text(DamoimStrings.PROFILE_ROW_LOGOUT, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = colors.textMuted, modifier = Modifier.fillMaxWidth().noRippleClick(onLogout).padding(vertical = 14.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
        Text(DamoimStrings.PROFILE_ROW_LEAVE, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = colors.error, modifier = Modifier.fillMaxWidth().noRippleClick(onLeave).padding(vertical = 14.dp))
    }
}

@Preview
@Composable
private fun MyProfileScreenPreview() {
    DamoimTheme { MyProfileScreen() }
}
