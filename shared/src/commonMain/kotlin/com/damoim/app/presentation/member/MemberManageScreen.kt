package com.damoim.app.presentation.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.presentation.component.BottomNavBar
import com.damoim.app.presentation.component.ChevronRightIcon
import com.damoim.app.presentation.component.ListIcon
import com.damoim.app.presentation.component.MainTab
import com.damoim.app.presentation.component.PeopleIcon
import com.damoim.app.presentation.component.PersonPlusIcon
import com.damoim.app.presentation.component.UserSingleIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 16 회원 관리 허브 (동아리장 전용) — Route. 회원 탭 루트, 하단 탭바 '회원' 활성.
 * 카드 → 17 목록 / 09 가입 신청 / 19 기수 관리 / 20 내 프로필.
 */
@Composable
fun MemberManageRoute(
    viewModel: MemberManageViewModel = viewModel(key = "member_manage") {
        MemberManageViewModel(AppGraph.getMembersUseCase, AppGraph.getClubInfoUseCase, AppGraph.getCohortsUseCase, AppGraph.getJoinApplicantsUseCase)
    },
    onOpenList: () -> Unit = {},
    onOpenJoinManage: () -> Unit = {},
    onOpenCohorts: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onTabSelect: (MainTab) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    MemberManageScreen(state, onOpenList, onOpenJoinManage, onOpenCohorts, onOpenProfile, onTabSelect)
}

@Composable
fun MemberManageScreen(
    state: MemberManageUiState = MemberManageUiState(38, 4, 3, "23기 ~ 25기", 3, "김민준", "23기", MemberRole.LEADER),
    onOpenList: () -> Unit = {},
    onOpenJoinManage: () -> Unit = {},
    onOpenCohorts: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onTabSelect: (MainTab) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surfaceInput)) {
        Box(Modifier.weight(1f)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                // 헤더 + 통계 (흰 배경)
                Column(Modifier.fillMaxWidth().background(colors.surface)) {
                    Text(
                        DamoimStrings.MEMBER_HUB_TITLE,
                        style = DamoimTheme.typography.headline,
                        color = colors.textPrimary,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        StatCard(state.totalCount.toString(), DamoimStrings.MEMBER_STAT_TOTAL, Modifier.weight(1f))
                        StatCard(state.staffCount.toString(), DamoimStrings.MEMBER_STAT_STAFF, Modifier.weight(1f))
                        StatCard(state.cohortCount.toString(), DamoimStrings.MEMBER_STAT_COHORT, Modifier.weight(1f))
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
                }
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    HubCard({ PeopleIcon(colors.primaryDark, Modifier.size(20.dp)) }, DamoimStrings.MEMBER_HUB_LIST, DamoimStrings.memberHubListSub(state.totalCount), onClick = onOpenList)
                    HubCard({ PersonPlusIcon(colors.primaryDark, Modifier.size(20.dp)) }, DamoimStrings.MEMBER_HUB_JOIN, DamoimStrings.memberHubJoinSub(state.pendingCount), badge = state.pendingCount, onClick = onOpenJoinManage)
                    HubCard({ ListIcon(colors.primaryDark, Modifier.size(20.dp)) }, DamoimStrings.MEMBER_HUB_COHORT, DamoimStrings.memberHubCohortSub(state.cohortRange), onClick = onOpenCohorts)
                    HubCard({ UserSingleIcon(colors.primaryDark, Modifier.size(20.dp)) }, DamoimStrings.MEMBER_HUB_PROFILE, DamoimStrings.memberHubProfileSub(state.myName, state.myCohortShort, memberRoleLabel(state.myRole)), onClick = onOpenProfile)
                }
            }
        }
        BottomNavBar(selected = MainTab.MEMBERS, onSelect = onTabSelect)
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = DamoimTheme.colors
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(colors.primaryContainer).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(value, style = DamoimTheme.typography.titleLarge.copy(fontSize = 22.sp), color = colors.primaryDeep)
        Text(label, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.SemiBold), color = colors.textTertiary)
    }
}

@Composable
private fun HubCard(icon: @Composable () -> Unit, title: String, subtitle: String, badge: Int = 0, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(colors.surface)
            .noRippleClick(onClick).padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(colors.primaryContainer), contentAlignment = Alignment.Center) { icon() }
            if (badge > 0) {
                Text(
                    badge.toString(),
                    style = DamoimTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = colors.onPrimary,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-6).dp).clip(RoundedCornerShape(999.dp)).background(colors.error).padding(horizontal = 5.dp, vertical = 1.dp),
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = colors.textPrimary)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = DamoimTheme.typography.caption, color = colors.textMuted)
        }
        ChevronRightIcon(tint = colors.outlineStrong, modifier = Modifier.size(18.dp))
    }
}

@Preview
@Composable
private fun MemberManageScreenPreview() {
    DamoimTheme { MemberManageScreen() }
}
