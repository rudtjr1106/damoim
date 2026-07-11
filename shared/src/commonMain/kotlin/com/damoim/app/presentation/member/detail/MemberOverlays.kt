package com.damoim.app.presentation.member.detail

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.domain.model.Cohort
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.presentation.component.CheckIcon
import com.damoim.app.presentation.component.DamoimBottomSheet
import com.damoim.app.presentation.component.DamoimDialog
import com.damoim.app.presentation.component.DialogButton
import com.damoim.app.presentation.component.PersonMinusIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 42 기수 변경 시트 — 단일 선택 라디오. */
@Composable
internal fun CohortChangeSheet(
    memberName: String,
    currentCohortLabel: String,
    cohorts: List<Cohort>,
    currentCohortId: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val colors = DamoimTheme.colors
    var selected by remember { mutableStateOf(currentCohortId) }
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(Modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
                Text(DamoimStrings.cohortChangeTitle(memberName), style = DamoimTheme.typography.titleLarge.copy(fontSize = 19.sp), color = colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(DamoimStrings.cohortChangeCurrent(currentCohortLabel), style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cohorts.forEach { c ->
                    RadioRow(c.label, DamoimStrings.memberCountLabel(c.memberCount), c.id == selected) { selected = c.id }
                }
            }
            DialogButton(
                DamoimStrings.COHORT_CHANGE_CONFIRM,
                bg = colors.primary, fg = colors.onPrimary,
                modifier = Modifier.fillMaxWidth().alpha(if (selected == currentCohortId) 0.35f else 1f),
                onClick = { if (selected != currentCohortId) onConfirm(selected) },
            )
        }
    }
}

/** 18 역할 변경 시트(신설) — 운영진 / 일반 회원 단일 선택. */
@Composable
internal fun RoleChangeSheet(
    memberName: String,
    currentRole: MemberRole,
    onDismiss: () -> Unit,
    onConfirm: (MemberRole) -> Unit,
) {
    val colors = DamoimTheme.colors
    // 동아리장 위임은 범위 밖 — 운영진/일반 사이 전환만
    val initial = if (currentRole == MemberRole.STAFF) MemberRole.STAFF else MemberRole.MEMBER
    var selected by remember { mutableStateOf(initial) }
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 40.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(DamoimStrings.roleChangeTitle(memberName), style = DamoimTheme.typography.titleLarge.copy(fontSize = 19.sp), color = colors.textPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RadioRow(DamoimStrings.ROLE_STAFF, DamoimStrings.ROLE_CHANGE_STAFF_DESC, selected == MemberRole.STAFF) { selected = MemberRole.STAFF }
                RadioRow(DamoimStrings.ROLE_MEMBER, DamoimStrings.ROLE_CHANGE_MEMBER_DESC, selected == MemberRole.MEMBER) { selected = MemberRole.MEMBER }
            }
            DialogButton(
                DamoimStrings.ROLE_CHANGE_CONFIRM,
                bg = colors.primary, fg = colors.onPrimary,
                modifier = Modifier.fillMaxWidth().alpha(if (selected == initial) 0.35f else 1f),
                onClick = { if (selected != initial) onConfirm(selected) },
            )
        }
    }
}

/** 42/역할 공용 라디오 카드 행. */
@Composable
private fun RadioRow(title: String, trailing: String, selected: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier.fillMaxWidth().clip(shape)
            .then(if (selected) Modifier.background(colors.primaryContainer) else Modifier)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) colors.primary else colors.divider, shape)
            .noRippleClick(onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(20.dp).clip(CircleShape).then(if (selected) Modifier.border(6.dp, colors.primary, CircleShape) else Modifier.border(1.5.dp, colors.outline, CircleShape)),
        )
        Text(title, style = DamoimTheme.typography.body.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold, fontSize = 15.sp), color = if (selected) colors.textPrimary else colors.textSecondary, modifier = Modifier.weight(1f))
        Text(trailing, style = DamoimTheme.typography.caption, color = if (selected) colors.textTertiary else colors.textDisabled)
    }
}

/** 43 내보내기 확인 다이얼로그. */
@Composable
internal fun RemoveMemberDialog(memberName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val colors = DamoimTheme.colors
    DamoimDialog(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(64.dp).clip(CircleShape).background(colors.errorContainer), contentAlignment = Alignment.Center) {
                    PersonMinusIcon(colors.error, Modifier.size(26.dp))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(DamoimStrings.memberRemoveTitle(memberName), style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp), color = colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text(DamoimStrings.MEMBER_REMOVE_BODY, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, lineHeight = 21.sp), color = colors.textMuted, textAlign = TextAlign.Center)
                }
            }
            // 재가입 차단 옵션(표시용) — 42/44 표시용 컨트롤과 동일 취급
            var blockRejoin by remember { mutableStateOf(false) }
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceInput).noRippleClick { blockRejoin = !blockRejoin }.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    Modifier.size(18.dp).clip(RoundedCornerShape(5.dp))
                        .then(if (blockRejoin) Modifier.background(colors.error) else Modifier.border(1.5.dp, colors.outline, RoundedCornerShape(5.dp))),
                    contentAlignment = Alignment.Center,
                ) { if (blockRejoin) CheckIcon(colors.onPrimary, Modifier.size(12.dp)) }
                Text(DamoimStrings.MEMBER_REMOVE_BLOCK_REJOIN, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DialogButton(DamoimStrings.COMMON_CANCEL, bg = colors.surfaceVariant, fg = colors.textTertiary, modifier = Modifier.weight(1f), onClick = onDismiss)
                DialogButton(DamoimStrings.MEMBER_REMOVE_CONFIRM, bg = colors.error, fg = colors.onPrimary, modifier = Modifier.weight(1f), onClick = onConfirm)
            }
        }
    }
}
