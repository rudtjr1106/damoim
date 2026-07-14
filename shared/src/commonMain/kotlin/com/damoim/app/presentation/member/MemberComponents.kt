package com.damoim.app.presentation.member

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.domain.model.Member
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.domain.model.MemberStatus
import com.damoim.app.presentation.component.CrownIcon
import com.damoim.app.presentation.component.NetworkAvatar
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 명부 역할 → 한국어 라벨. */
fun memberRoleLabel(role: MemberRole): String = when (role) {
    MemberRole.LEADER -> DamoimStrings.ROLE_LEADER
    MemberRole.STAFF -> DamoimStrings.ROLE_STAFF
    MemberRole.MEMBER -> DamoimStrings.ROLE_MEMBER
}

/** 회원 원형 아바타 — 프로필 사진이 있으면 실사진, 없으면 이니셜(휴면=회색, 활동=primaryContainerHigh/primaryDeep). */
@Composable
fun MemberAvatar(member: Member, size: Dp, fontSize: androidx.compose.ui.unit.TextUnit = 13.sp) {
    val colors = DamoimTheme.colors
    val dormant = member.status == MemberStatus.DORMANT
    NetworkAvatar(url = member.profileImageUrl, size = size) {
        Box(
            modifier = Modifier.size(size).clip(CircleShape).background(if (dormant) colors.surfaceVariant else colors.primaryContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                member.initials,
                style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold, fontSize = fontSize),
                color = if (dormant) colors.textMuted else colors.primaryDeep,
            )
        }
    }
}

/** 상태 알약(활동/휴면). */
@Composable
fun StatusPill(status: MemberStatus, modifier: Modifier = Modifier) {
    val colors = DamoimTheme.colors
    val active = status == MemberStatus.ACTIVE
    Text(
        if (active) DamoimStrings.MEMBER_STATUS_ACTIVE else DamoimStrings.MEMBER_STATUS_DORMANT,
        style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold),
        color = if (active) colors.primaryDark else colors.textMuted,
        modifier = modifier.clip(RoundedCornerShape(999.dp))
            .background(if (active) colors.primaryContainer else colors.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

/** 17 필터 칩(전체/운영진/기수). 활성 = textPrimary 배경(게시판 칩과 동일 계열). */
@Composable
fun MemberFilterChip(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Text(
        label,
        style = DamoimTheme.typography.caption.copy(fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold),
        color = if (active) colors.onPrimary else colors.textTertiary,
        modifier = Modifier.clip(RoundedCornerShape(999.dp))
            .background(if (active) colors.textPrimary else colors.surfaceVariant)
            .noRippleClick(onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    )
}

/** 17 회원 행 — 아바타 + 이름(리더 왕관) + "기수 · 역할" + 상태 알약. */
@Composable
fun MemberRow(member: Member, cohortShort: String, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth().noRippleClick(onClick).padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MemberAvatar(member, size = 44.dp)
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(member.name, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp), color = colors.textPrimary)
                if (member.role == MemberRole.LEADER) CrownIcon(tint = colors.primary, modifier = Modifier.size(13.dp))
            }
            Spacer(Modifier.size(2.dp))
            Text("$cohortShort · ${memberRoleLabel(member.role)}", style = DamoimTheme.typography.caption, color = colors.textMuted)
        }
        StatusPill(member.status)
    }
}

/** 18 회원 상세용 알약(기수/역할/상태). primary 계열 or 중립. */
@Composable
fun DetailBadge(text: String, emphasized: Boolean, modifier: Modifier = Modifier) {
    val colors = DamoimTheme.colors
    Text(
        text,
        style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
        color = if (emphasized) colors.primaryDark else colors.textTertiary,
        modifier = modifier.clip(RoundedCornerShape(999.dp))
            .background(if (emphasized) colors.primaryContainer else colors.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

/** 라벨/값 한 줄(18 상세·20 프로필 정보 카드). */
@Composable
fun InfoRow(label: String, value: String, showDivider: Boolean = true, valueColor: Color? = null) {
    val colors = DamoimTheme.colors
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp), color = colors.textMuted, modifier = Modifier.weight(1f))
            Text(value, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = valueColor ?: colors.textPrimary)
        }
        if (showDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
    }
}
