package com.damoim.app.presentation.settings.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.presentation.component.BottomNavBar
import com.damoim.app.presentation.component.ChevronRightIcon
import com.damoim.app.presentation.component.NetworkImage
import com.damoim.app.presentation.component.MainTab
import com.damoim.app.presentation.component.WarningIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.settings.SettingsRow
import com.damoim.app.presentation.settings.SettingsSection
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

@Composable
fun SettingsHomeRoute(
    isLeader: Boolean = false,
    isStaff: Boolean = false,
    canManageClubSettings: Boolean = false,
    viewModel: SettingsHomeViewModel = viewModel(key = "settingsHome") {
        SettingsHomeViewModel(AppGraph.getClubInfoUseCase, AppGraph.subscriptionUseCase)
    },
    onOpenMyProfile: () -> Unit = {},
    onOpenClubSettings: () -> Unit = {},
    onOpenAdmin: () -> Unit = {},
    onOpenPlan: () -> Unit = {},
    onOpenSubscription: () -> Unit = {},
    onOpenNotif: () -> Unit = {},
    onOpenInquiry: () -> Unit = {},
    onOpenBlocked: () -> Unit = {},
    onTabSelect: (MainTab) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    SettingsHomeScreen(
        state = state,
        isLeader = isLeader,
        isStaff = isStaff,
        canManageClubSettings = canManageClubSettings,
        onOpenMyProfile = onOpenMyProfile,
        onOpenClubSettings = onOpenClubSettings,
        onOpenAdmin = onOpenAdmin,
        onOpenPlan = onOpenPlan,
        onOpenSubscription = onOpenSubscription,
        onOpenNotif = onOpenNotif,
        onOpenInquiry = onOpenInquiry,
        onOpenBlocked = onOpenBlocked,
        onTabSelect = onTabSelect,
    )
}

@Composable
fun SettingsHomeScreen(
    state: SettingsHomeUiState = SettingsHomeUiState(clubName = "코딩하는 사람들", memberCount = 38, joinCode = "DM29AX", overLimit = true, memberUsed = 38),
    isLeader: Boolean = false,
    isStaff: Boolean = false,
    canManageClubSettings: Boolean = false,
    onOpenMyProfile: () -> Unit = {},
    onOpenClubSettings: () -> Unit = {},
    onOpenAdmin: () -> Unit = {},
    onOpenPlan: () -> Unit = {},
    onOpenSubscription: () -> Unit = {},
    onOpenNotif: () -> Unit = {},
    onOpenInquiry: () -> Unit = {},
    onOpenBlocked: () -> Unit = {},
    onTabSelect: (MainTab) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surfaceInput)) {
        // 헤더(탭 루트 — 뒤로가기 없음)
        Box(Modifier.fillMaxWidth().background(colors.surface).windowInsetsPadding(WindowInsets.statusBars).padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 16.dp)) {
            Text(DamoimStrings.SETTINGS_HOME_TITLE, style = DamoimTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp), color = colors.textPrimary)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // 동아리 카드 — 일반 부원은 동아리 정보 설정으로 이동 불가
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(colors.surface).then(if (canManageClubSettings) Modifier.noRippleClick(onOpenClubSettings) else Modifier).padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                // 이미지 URL이 있어도 로드에 실패하면(서버에 바이트 없음 등) 이니셜로 폴백한다
                val clubInitialBox: @Composable () -> Unit = {
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).background(colors.primary), contentAlignment = Alignment.Center) {
                        Text(state.clubInitial, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 20.sp), color = colors.onPrimary)
                    }
                }
                if (!state.clubImageUrl.isNullOrBlank()) {
                    NetworkImage(url = state.clubImageUrl, modifier = Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)), cornerRadius = 18.dp, fallback = clubInitialBox)
                } else {
                    clubInitialBox()
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(state.clubName, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp), color = colors.textPrimary)
                    Text(DamoimStrings.settingsClubMeta(state.memberCount, state.planName), style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
                }
                if (canManageClubSettings) ChevronRightIcon(colors.outlineStrong, Modifier.size(18.dp))
            }

            // 인원 초과 경고 — 유일한 액션(플랜 업그레이드)이 운영진 전용이라 운영진에게만 노출
            if (state.overLimit && isStaff) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(colors.errorSurface).border(1.dp, colors.errorBorder, RoundedCornerShape(18.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        WarningIcon(colors.error, Modifier.size(20.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(DamoimStrings.settingsOverLimit(state.memberUsed, state.memberLimit), style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 14.sp), color = colors.errorStrong)
                            Text(DamoimStrings.SETTINGS_OVERLIMIT_BODY, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal, lineHeight = 18.sp), color = colors.errorMuted)
                        }
                    }
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.error).noRippleClick(onOpenPlan).padding(vertical = 13.dp), contentAlignment = Alignment.Center) {
                        Text(DamoimStrings.SETTINGS_UPGRADE, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = colors.onPrimary)
                    }
                }
            }

            SettingsSection(DamoimStrings.SETTINGS_SEC_ME) {
                SettingsRow(DamoimStrings.MY_PROFILE_TITLE, onOpenMyProfile, showDivider = false)
            }
            // 동아리 관리 — 정보 설정·가입 코드는 CLUB_SETTINGS 권한, 운영진 권한 관리는 동아리장 전용.
            if (canManageClubSettings) {
                SettingsSection(DamoimStrings.SETTINGS_SEC_CLUB) {
                    SettingsRow(DamoimStrings.SETTINGS_CLUB_INFO, onOpenClubSettings)
                    // 운영진 권한 행이 뒤따르지 않으면(설정 권한만 있는 STAFF) 가입 코드가 마지막이라 구분선 제거.
                    SettingsRow(DamoimStrings.SETTINGS_JOIN_CODE, onOpenClubSettings, showDivider = isLeader, trailing = {
                        Text(state.joinCode, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = colors.primaryDark)
                    })
                    if (isLeader) SettingsRow(DamoimStrings.SETTINGS_ADMIN_PERM, onOpenAdmin, showDivider = false)
                }
            }
            // 구독/결제는 운영진 전용 — 일반 부원에겐 구독 플랜·결제 내역 진입점을 감춘다.
            if (isStaff) {
                SettingsSection(DamoimStrings.SETTINGS_SEC_SUBSCRIPTION) {
                    SettingsRow(DamoimStrings.SETTINGS_PLAN_INFO, onOpenPlan, trailing = {
                        Text(state.planName, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = colors.textMuted)
                    })
                    SettingsRow(DamoimStrings.SETTINGS_PAYMENT_HISTORY, onOpenSubscription, showDivider = false)
                }
            }
            SettingsSection(DamoimStrings.SETTINGS_SEC_ETC) {
                SettingsRow(DamoimStrings.SETTINGS_NOTIF, onOpenNotif)
                SettingsRow(DamoimStrings.SETTINGS_INQUIRY, onOpenInquiry)
                SettingsRow(DamoimStrings.SETTINGS_BLOCKED, onOpenBlocked, showDivider = false)
            }
            Spacer(Modifier.height(8.dp))
        }

        BottomNavBar(selected = MainTab.SETTINGS, onSelect = onTabSelect)
    }
}
