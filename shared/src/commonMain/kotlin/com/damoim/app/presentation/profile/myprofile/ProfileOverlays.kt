package com.damoim.app.presentation.profile.myprofile

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
import com.damoim.app.presentation.component.DamoimBottomSheet
import com.damoim.app.presentation.component.DamoimDialog
import com.damoim.app.presentation.component.DialogButton
import com.damoim.app.presentation.component.DoorExitIcon
import com.damoim.app.presentation.component.WarningIcon
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

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

