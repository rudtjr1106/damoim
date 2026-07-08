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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.damoim.app.domain.model.Club
import com.damoim.app.presentation.component.ExclamationIcon
import com.damoim.app.presentation.component.PrimaryButton
import com.damoim.app.presentation.component.SecondaryTextButton
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

private const val PreviewRejectReason = "이번 기수 모집이 마감되었어요. 다음 모집 때 다시 신청해주세요."

/**
 * 화면 38 가입 신청 거절됨 — Route(네비게이션).
 */
@Composable
fun JoinRejectedRoute(
    club: Club = PreviewClub,
    reason: String = PreviewRejectReason,
    onRetry: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    JoinRejectedScreen(club = club, reason = reason, onRetry = onRetry, onClose = onClose)
}

/**
 * 화면 38 가입 신청 거절됨 — Screen(무상태 UI). 04의 반대 결과.
 */
@Composable
fun JoinRejectedScreen(
    club: Club = PreviewClub,
    reason: String = PreviewRejectReason,
    onRetry: () -> Unit = {},
    onClose: () -> Unit = {},
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
        Text(DamoimStrings.JOIN_REJECTED_TITLE, style = DamoimTheme.typography.headline, color = colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            "${club.name}\n$reason",
            style = DamoimTheme.typography.body,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.weight(1f))
        PrimaryButton(text = DamoimStrings.JOIN_REJECTED_RETRY, onClick = onRetry)
        Spacer(Modifier.height(8.dp))
        SecondaryTextButton(text = DamoimStrings.COMMON_CLOSE, onClick = onClose)
        Spacer(Modifier.height(24.dp))
    }
}

@Preview
@Composable
private fun JoinRejectedScreenPreview() {
    DamoimTheme { JoinRejectedScreen() }
}
