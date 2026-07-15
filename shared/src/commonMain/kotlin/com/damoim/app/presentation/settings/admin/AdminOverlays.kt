package com.damoim.app.presentation.settings.admin

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.domain.model.AdminMember
import com.damoim.app.domain.model.Member
import com.damoim.app.presentation.component.DamoimBottomSheet
import com.damoim.app.presentation.component.DamoimDialog
import com.damoim.app.presentation.component.DialogButton
import com.damoim.app.presentation.component.EditIcon
import com.damoim.app.presentation.component.NetworkAvatar
import com.damoim.app.presentation.component.PersonMinusIcon
import com.damoim.app.presentation.component.SheetActionRow
import com.damoim.app.presentation.component.SheetCloseButton
import com.damoim.app.presentation.component.UserSingleIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

// ── GG1 운영진 추가(회원 선택) ──
@Composable
internal fun AdminAddSheet(assignable: List<Member>, onSelect: (Long) -> Unit, onDismiss: () -> Unit) {
    val colors = DamoimTheme.colors
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 44.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(DamoimStrings.ADMIN_ADD_SHEET_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = colors.textPrimary)
                Text(DamoimStrings.ADMIN_ADD_SHEET_SUB, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp), color = colors.textMuted)
            }
            if (assignable.isEmpty()) {
                Text(DamoimStrings.ADMIN_ADD_EMPTY, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textDisabled, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), textAlign = TextAlign.Center)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    assignable.forEach { m ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.surfaceInput).noRippleClick { onSelect(m.id) }.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            NetworkAvatar(url = m.profileImageUrl, size = 40.dp) {
                                Box(Modifier.size(40.dp).clip(CircleShape).background(colors.primaryContainerHigh), contentAlignment = Alignment.Center) {
                                    Text(m.initials, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 12.sp), color = colors.primaryDeep)
                                }
                            }
                            Text(m.name, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            SheetCloseButton(onDismiss)
        }
    }
}

// ── 64 운영진 ⋯ 메뉴 ──
@Composable
internal fun AdminMenuSheet(admin: AdminMember, onChangeTitle: () -> Unit, onDetail: () -> Unit, onRemove: () -> Unit, onDismiss: () -> Unit) {
    val colors = DamoimTheme.colors
    DamoimBottomSheet(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 44.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.surfaceInput).padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NetworkAvatar(url = admin.imageUrl, size = 40.dp) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(colors.primaryContainerHigh), contentAlignment = Alignment.Center) {
                        Text(admin.initials, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 12.sp), color = colors.primaryDeep)
                    }
                }
                Column {
                    Text(admin.name, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = colors.textPrimary)
                    Text("${admin.cohortLabel} · ${admin.title}", style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
                }
            }
            Column(Modifier.fillMaxWidth()) {
                SheetActionRow(DamoimStrings.ADMIN_MENU_TITLE, onChangeTitle, icon = { EditIcon(colors.textSecondary, Modifier.size(19.dp)) })
                SheetActionRow(DamoimStrings.ADMIN_MENU_DETAIL, onDetail, icon = { UserSingleIcon(colors.textSecondary, Modifier.size(19.dp)) })
                SheetActionRow(DamoimStrings.ADMIN_MENU_REMOVE, onRemove, textColor = colors.error, icon = { PersonMinusIcon(colors.error, Modifier.size(19.dp)) }, showDivider = false)
            }
            SheetCloseButton(onDismiss)
        }
    }
}

// ── GG2 직함 변경 다이얼로그 ──
@Composable
internal fun AdminTitleDialog(admin: AdminMember, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    val colors = DamoimTheme.colors
    var text by remember { mutableStateOf(admin.title) }
    DamoimDialog(onDismiss = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(DamoimStrings.ADMIN_TITLE_DIALOG, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = colors.textPrimary)
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceInput).border(1.dp, colors.divider, RoundedCornerShape(12.dp)).padding(horizontal = 14.dp, vertical = 13.dp)) {
                if (text.isEmpty()) Text(DamoimStrings.ADMIN_TITLE_PLACEHOLDER, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.Normal), color = colors.textDisabled)
                BasicTextField(text, { text = it }, textStyle = DamoimTheme.typography.body.copy(color = colors.textPrimary, fontWeight = FontWeight.SemiBold), cursorBrush = SolidColor(colors.primary), singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DialogButton(DamoimStrings.COMMON_CANCEL, colors.surfaceVariant, colors.textTertiary, Modifier.weight(1f), onDismiss)
                DialogButton(DamoimStrings.ADMIN_TITLE_SAVE, colors.primary, colors.onPrimary, Modifier.weight(1f)) { onSave(text) }
            }
        }
    }
}
