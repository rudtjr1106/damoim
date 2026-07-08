package com.damoim.app.presentation.auth.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.damoim.app.domain.model.Club
import com.damoim.app.presentation.component.ExclamationIcon
import com.damoim.app.presentation.component.PrimaryButton
import com.damoim.app.presentation.component.SecondaryTextButton
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 38 가입 신청 거절됨. 04의 반대 결과. 사유 안내 + 다른 코드 재시도.
 */
@Composable
fun JoinRejectedScreen(
    club: Club,
    reason: String,
    onRetry: () -> Unit,
    onClose: () -> Unit,
) {
    val colors = DamoimTheme.colors
    Column(
        modifier = Modifier.fillMaxSize().background(colors.surface).safeDrawingPadding().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier.size(88.dp).clip(CircleShape).background(colors.errorContainer),
            contentAlignment = Alignment.Center,
        ) { ExclamationIcon(tint = colors.error, modifier = Modifier.size(40.dp), strokeWidth = 2.8f) }

        Spacer(Modifier.height(24.dp))
        Text("가입이 거절되었어요", style = DamoimTheme.typography.headline, color = colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            "${club.name}\n$reason",
            style = DamoimTheme.typography.body,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.weight(1f))
        PrimaryButton(text = "다른 코드 입력하기", onClick = onRetry)
        Spacer(Modifier.height(8.dp))
        SecondaryTextButton(text = "닫기", onClick = onClose)
        Spacer(Modifier.height(24.dp))
    }
}
