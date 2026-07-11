package com.damoim.app.presentation.settings.result

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.presentation.component.CheckIcon
import com.damoim.app.presentation.component.WarningIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 49 결제 완료 / 50 결제 실패 (동일 레이아웃, [success]로 분기). */
@Composable
fun PaymentResultScreen(
    success: Boolean,
    onDone: () -> Unit = {},
    onRetry: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surface).padding(horizontal = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(80.dp).clip(CircleShape).background(if (success) colors.primaryContainer else colors.errorSurface), contentAlignment = Alignment.Center) {
            if (success) CheckIcon(colors.primary, Modifier.size(40.dp)) else WarningIcon(colors.error, Modifier.size(40.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(if (success) DamoimStrings.PAY_DONE_TITLE else DamoimStrings.PAY_FAIL_TITLE, style = DamoimTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp), color = colors.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(if (success) DamoimStrings.PAY_DONE_DESC else DamoimStrings.PAY_FAIL_DESC, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.Normal, lineHeight = 22.sp), color = colors.textMuted, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        if (success) {
            CtaButton(DamoimStrings.PAY_DONE_CTA, colors.primary, colors.onPrimary, onDone)
        } else {
            CtaButton(DamoimStrings.PAY_FAIL_RETRY, colors.primary, colors.onPrimary, onRetry)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().noRippleClick(onDone).padding(vertical = 14.dp), horizontalArrangement = Arrangement.Center) {
                Text(DamoimStrings.PAY_FAIL_LATER, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = colors.textMuted)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CtaButton(text: String, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(bg).noRippleClick(onClick).padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
        Text(text, style = DamoimTheme.typography.button, color = fg)
    }
}
