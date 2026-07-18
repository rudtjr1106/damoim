package com.damoim.app.presentation.club

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.damoim.app.domain.model.ClubMembership
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.presentation.component.CheckIcon
import com.damoim.app.presentation.component.DamoimBottomSheet
import com.damoim.app.presentation.component.LockIcon
import com.damoim.app.presentation.component.NetworkImage
import com.damoim.app.presentation.component.PlusIcon
import com.damoim.app.presentation.component.dashedBorder
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 33 동아리 전환 오버레이 — 설정(42)·홈 동아리명(46) 어디서든 재사용. [visible]일 때만 시트를 띄우고,
 * 전환 시 세션만 갈아탄 뒤 [onSwitched](보통 홈으로 리셋)를 호출한다. 참여/생성은 세션 종료로 이어진다.
 */
@Composable
fun ClubSwitchOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSwitched: () -> Unit,
    onJoin: () -> Unit,
    onCreate: () -> Unit,
    viewModel: ClubSwitchViewModel = viewModel(key = "club_switch") {
        ClubSwitchViewModel(AppGraph.getJoinedClubsUseCase, AppGraph.getClubInfoUseCase, AppGraph.clubSessionUseCase)
    },
) {
    if (!visible) return
    val state by viewModel.uiState.collectAsState()
    ClubSwitchSheet(
        clubs = state.joinedClubs,
        currentClubId = state.currentClubId,
        onDismiss = onDismiss,
        onSwitch = { id -> onDismiss(); viewModel.switch(id); onSwitched() },
        onJoin = { onDismiss(); onJoin() },
        onCreate = { onDismiss(); onCreate() },
    )
}

/** 33 동아리 전환 시트 — 현재 동아리 체크, 다른 동아리 탭 시 전환. 하단 참여/생성은 세션 종료. */
@Composable
fun ClubSwitchSheet(
    clubs: List<ClubMembership>,
    currentClubId: Long,
    onDismiss: () -> Unit,
    onSwitch: (Long) -> Unit,
    onJoin: () -> Unit,
    onCreate: () -> Unit,
) {
    val colors = DamoimTheme.colors
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(DamoimStrings.CLUB_SWITCH_TITLE, style = DamoimTheme.typography.titleLarge.copy(fontSize = 19.sp), color = colors.textPrimary, modifier = Modifier.padding(horizontal = 4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                clubs.forEach { m ->
                    ClubRow(m, current = m.club.id == currentClubId, onClick = { if (m.club.id != currentClubId) onSwitch(m.club.id) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DashedButton(DamoimStrings.CLUB_SWITCH_JOIN, { LockIcon(colors.textTertiary, Modifier.size(15.dp)) }, Modifier.weight(1f), onJoin)
                DashedButton(DamoimStrings.CLUB_SWITCH_CREATE, { PlusIcon(colors.textTertiary, Modifier.size(15.dp)) }, Modifier.weight(1f), onCreate)
            }
        }
    }
}

@Composable
private fun ClubRow(membership: ClubMembership, current: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    val club = membership.club
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape)
            .then(if (current) Modifier.background(colors.primaryContainer) else Modifier)
            .border(if (current) 1.5.dp else 1.dp, if (current) colors.primary else colors.divider, shape)
            .noRippleClick(onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 이미지 URL이 있어도 로드에 실패하면(서버에 바이트 없음 등) 이니셜로 폴백한다
        val clubInitialBox: @Composable () -> Unit = {
            Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(if (current) colors.primary else colors.accentSky), contentAlignment = Alignment.Center) {
                Text(club.name.take(1), style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp), color = colors.onPrimary)
            }
        }
        if (!club.imageUrl.isNullOrBlank()) {
            NetworkImage(url = club.imageUrl, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)), cornerRadius = 16.dp, fallback = clubInitialBox)
        } else {
            clubInitialBox()
        }
        Column(Modifier.weight(1f)) {
            Text(club.name, style = DamoimTheme.typography.body.copy(fontWeight = if (current) FontWeight.ExtraBold else FontWeight.Bold, fontSize = 15.sp), color = colors.textPrimary)
            Spacer(Modifier.height(2.dp))
            Text(if (membership.role == ClubRole.LEADER) DamoimStrings.ROLE_LEADER else DamoimStrings.ROLE_MEMBER, style = DamoimTheme.typography.caption, color = colors.textMuted)
        }
        if (current) CheckIcon(colors.primary, Modifier.size(20.dp))
    }
}

@Composable
private fun DashedButton(text: String, icon: @Composable () -> Unit, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Row(
        modifier.dashedBorder(colors.outline, 1.5.dp, 14.dp).noRippleClick(onClick).padding(14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
        Spacer(Modifier.width(6.dp))
        Text(text, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.textTertiary)
    }
}
