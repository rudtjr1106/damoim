package com.damoim.app.presentation.settings.inquiry

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.platform.rememberEmailComposer
import com.damoim.app.presentation.component.DamoimTextField
import com.damoim.app.presentation.component.ImageIcon
import com.damoim.app.presentation.component.InfoIcon
import com.damoim.app.presentation.component.dashedBorder
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.settings.SettingsTopBar
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

@Composable
fun InquiryRoute(
    onBack: () -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val email = rememberEmailComposer()
    InquiryScreen(
        onBack = onBack,
        onToast = onToast,
        onSend = { type, subject, content ->
            email(DamoimStrings.INQUIRY_EMAIL, "${DamoimStrings.INQUIRY_SUBJECT_PREFIX} $type · $subject", content)
            onToast(DamoimStrings.TOAST_INQUIRY_SENT)
            onBack()
        },
    )
}

@Composable
fun InquiryScreen(
    onBack: () -> Unit = {},
    onToast: (String) -> Unit = {},
    onSend: (type: String, subject: String, content: String) -> Unit = { _, _, _ -> },
) {
    val colors = DamoimTheme.colors
    var type by remember { mutableStateOf(DamoimStrings.INQUIRY_TYPES.first()) }
    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val canSend = subject.isNotBlank() && content.isNotBlank()

    Column(Modifier.fillMaxSize().background(colors.surface)) {
        SettingsTopBar(DamoimStrings.INQUIRY_TITLE, onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            // 문의 유형
            Field(DamoimStrings.INQUIRY_TYPE) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DamoimStrings.INQUIRY_TYPES.forEach { t ->
                        val on = t == type
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (on) colors.primaryContainer else colors.surfaceInput).border(if (on) 1.5.dp else 1.dp, if (on) colors.primary else colors.divider, RoundedCornerShape(10.dp)).noRippleClick { type = t }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(t, style = DamoimTheme.typography.label.copy(fontWeight = if (on) FontWeight.Bold else FontWeight.SemiBold, fontSize = 11.5.sp), color = if (on) colors.primaryDark else colors.textTertiary)
                        }
                    }
                }
            }
            Field(DamoimStrings.INQUIRY_SUBJECT) { DamoimTextField(subject, { subject = it }, placeholder = DamoimStrings.INQUIRY_SUBJECT_PLACEHOLDER) }
            Field(DamoimStrings.INQUIRY_CONTENT) { DamoimTextField(content, { content = it }, placeholder = DamoimStrings.INQUIRY_CONTENT_PLACEHOLDER, singleLine = false, modifier = Modifier.heightIn(min = 120.dp)) }
            // 스크린샷 첨부 (메일 앱에서 첨부)
            Field(DamoimStrings.INQUIRY_ATTACH) {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).dashedBorder(colors.outline, 1.5.dp, 12.dp).noRippleClick { onToast(DamoimStrings.TOAST_INQUIRY_ATTACH) }.padding(vertical = 18.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    ImageIcon(colors.textMuted, Modifier.size(18.dp)); Spacer(Modifier.size(8.dp))
                    Text(DamoimStrings.INQUIRY_ATTACH_HINT, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold), color = colors.textMuted)
                }
            }
            // 도움말 센터 (GG6 — 대응 화면 없어 준비중 안내)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.primaryContainer).noRippleClick { onToast(DamoimStrings.TOAST_COMING_SOON) }.padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                InfoIcon(colors.primary, Modifier.size(18.dp))
                Column(Modifier.weight(1f)) {
                    Text(DamoimStrings.INQUIRY_HELP, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp), color = colors.primaryDeep)
                    Text(DamoimStrings.INQUIRY_HELP_HINT, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Normal, fontSize = 11.5.sp), color = colors.primaryDeep.copy(alpha = 0.7f))
                }
            }
        }
        // 보내기
        Box(Modifier.fillMaxWidth().background(colors.surface).padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp)) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (canSend) colors.primary else colors.primary.copy(alpha = 0.35f)).then(if (canSend) Modifier.noRippleClick { onSend(type, subject, content) } else Modifier).padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                Text(DamoimStrings.INQUIRY_SEND, style = DamoimTheme.typography.button, color = colors.onPrimary)
            }
        }
    }
}

@Composable
private fun Field(label: String, content: @Composable () -> Unit) {
    val colors = DamoimTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = colors.textSecondary)
        content()
    }
}
