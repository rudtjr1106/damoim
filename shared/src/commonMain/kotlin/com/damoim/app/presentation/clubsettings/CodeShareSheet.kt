package com.damoim.app.presentation.clubsettings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.presentation.component.CopyIcon
import com.damoim.app.presentation.component.KakaoBubbleIcon
import com.damoim.app.presentation.component.LinkIcon
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 59 가입 코드 공유 시트. 08에서 코드 카드의 공유 아이콘을 누르면 뜬다.
 * 딤 배경 + 하단 시트(QR·코드·카카오/링크).
 */
@Composable
fun CodeShareSheet(
    code: String,
    onKakaoShare: () -> Unit,
    onCopyLink: () -> Unit,
    onCopyCode: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = DamoimTheme.colors
    Box(
        modifier = Modifier.fillMaxSize().background(colors.scrim)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(colors.surface)
                // 시트 내부 클릭이 딤으로 전달되지 않도록 소비
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 44.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(Modifier.width(44.dp).height(5.dp).clip(RoundedCornerShape(99.dp)).background(colors.divider))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(DamoimStrings.SHARE_TITLE, style = DamoimTheme.typography.titleLarge, color = colors.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(DamoimStrings.SHARE_SUBTITLE, style = DamoimTheme.typography.bodySmall, color = colors.textMuted)
            }

            // QR + 코드
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.primaryContainer).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier.size(140.dp).clip(RoundedCornerShape(16.dp)).background(colors.surface).border(1.dp, colors.divider, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) { Text("QR", style = DamoimTheme.typography.caption, color = colors.textDisabled) }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(code, style = DamoimTheme.typography.headline.copy(fontSize = 28.sp, letterSpacing = 3.sp), color = colors.textPrimary)
                    Box(
                        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(colors.surface).border(1.dp, colors.divider, RoundedCornerShape(10.dp))
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCopyCode),
                        contentAlignment = Alignment.Center,
                    ) { CopyIcon(tint = colors.textSecondary, modifier = Modifier.size(15.dp)) }
                }
            }

            // 공유 버튼들
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.kakao)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onKakaoShare)
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                ) {
                    KakaoBubbleIcon(tint = colors.onKakao, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(DamoimStrings.SHARE_KAKAO, style = DamoimTheme.typography.bodyStrong, color = colors.onKakao)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).border(1.dp, colors.divider, RoundedCornerShape(14.dp))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCopyLink)
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                ) {
                    LinkIcon(tint = colors.textSecondary, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(DamoimStrings.SHARE_COPY_LINK, style = DamoimTheme.typography.bodyStrong, color = colors.textSecondary)
                }
            }
            Text(DamoimStrings.SHARE_HINT, style = DamoimTheme.typography.caption, color = colors.textDisabled, textAlign = TextAlign.Center)
        }
    }
}
