package com.damoim.app.presentation.profile.myprofile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.domain.model.ClubMembership
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.presentation.component.CheckIcon
import com.damoim.app.presentation.component.DamoimBottomSheet
import com.damoim.app.presentation.component.DamoimDialog
import com.damoim.app.presentation.component.DialogButton
import com.damoim.app.presentation.component.DoorExitIcon
import com.damoim.app.presentation.component.LockIcon
import com.damoim.app.presentation.component.PlusIcon
import com.damoim.app.presentation.component.WarningIcon
import com.damoim.app.presentation.component.dashedBorder
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.member.memberRoleLabel
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 33 동아리 전환 시트 — 현재 동아리 체크, 다른 동아리 탭 시 전환. 하단 참여/생성은 세션 종료. */
@Composable
internal fun ClubSwitchSheet(
    clubs: List<ClubMembership>,
    currentClubId: Long,
    onDismiss: () -> Unit,
    onSwitch: (Long) -> Unit,
    onJoinOrCreate: () -> Unit,
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
                DashedButton(DamoimStrings.CLUB_SWITCH_JOIN, { LockIcon(colors.textTertiary, Modifier.size(15.dp)) }, Modifier.weight(1f), onJoinOrCreate)
                DashedButton(DamoimStrings.CLUB_SWITCH_CREATE, { PlusIcon(colors.textTertiary, Modifier.size(15.dp)) }, Modifier.weight(1f), onJoinOrCreate)
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
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(if (current) colors.primary else colors.accentSky), contentAlignment = Alignment.Center) {
            Text(club.name.take(1), style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp), color = colors.onPrimary)
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

/**
 * 60 탈퇴 / 로그아웃 공용 확인 다이얼로그. [destructive]면 빨강 확인 버튼.
 * [icon]으로 상단 글리프를 지정(60=문나가기, 로그아웃=경고).
 */
@Composable
internal fun ConfirmDialog(
    title: String,
    body: String,
    note: String? = null,
    confirmLabel: String,
    destructive: Boolean,
    icon: @Composable (Color) -> Unit = { WarningIcon(it, Modifier.size(26.dp)) },
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = DamoimTheme.colors
    DamoimDialog(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(64.dp).clip(CircleShape).background(if (destructive) colors.errorContainer else colors.surfaceVariant), contentAlignment = Alignment.Center) {
                    icon(if (destructive) colors.error else colors.textTertiary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(title, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp), color = colors.textPrimary, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text(body, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, lineHeight = 21.sp), color = colors.textMuted, textAlign = TextAlign.Center)
                }
            }
            if (note != null) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceInput).padding(horizontal = 14.dp, vertical = 11.dp)) {
                    Text(note, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted, modifier = Modifier.fillMaxWidth())
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogButton(DamoimStrings.COMMON_CANCEL, bg = colors.surfaceVariant, fg = colors.textTertiary, modifier = Modifier.weight(1f), onClick = onDismiss)
                DialogButton(confirmLabel, bg = if (destructive) colors.error else colors.primary, fg = colors.onPrimary, modifier = Modifier.weight(1f), onClick = onConfirm)
            }
        }
    }
}

