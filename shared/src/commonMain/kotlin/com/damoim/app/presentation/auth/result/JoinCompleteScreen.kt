package com.damoim.app.presentation.auth.result

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.damoim.app.domain.model.Club
import com.damoim.app.presentation.component.CheckIcon
import com.damoim.app.presentation.component.NetworkImage
import com.damoim.app.presentation.component.PrimaryButton
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 프리뷰/기본값용 샘플 동아리. */
internal val PreviewClub = Club(
    id = 10L,
    name = "UMC 앱디자인 동아리",
    category = "IT · 개발",
    description = "매 기수 실제 앱을 함께 만드는 동아리",
    memberCount = 28,
    emblemColor = 0xFF2F6DD3,
)

/**
 * 화면 04 가입 신청 완료 — Route(네비게이션).
 */
@Composable
fun JoinCompleteRoute(
    club: Club = PreviewClub,
    onConfirm: () -> Unit = {},
) {
    JoinCompleteScreen(club = club, onConfirm = onConfirm)
}

/**
 * 화면 04 가입 신청 완료 — Screen(무상태 UI). 동아리 요약 + 승인 대기 안내.
 */
@Composable
fun JoinCompleteScreen(
    club: Club = PreviewClub,
    onConfirm: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(
        modifier = Modifier.fillMaxSize().background(colors.surface).safeDrawingPadding().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier.size(88.dp).clip(CircleShape).background(colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { CheckIcon(tint = colors.primary, modifier = Modifier.size(40.dp)) }

        Spacer(Modifier.height(24.dp))
        Text(DamoimStrings.JOIN_COMPLETE_TITLE, style = DamoimTheme.typography.headline, color = colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            DamoimStrings.JOIN_COMPLETE_MESSAGE,
            style = DamoimTheme.typography.body,
            color = colors.textMuted,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(28.dp))
        ClubSummaryCard(club)

        Spacer(Modifier.weight(1f))
        PrimaryButton(text = DamoimStrings.COMMON_CONFIRM, onClick = onConfirm)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
internal fun ClubSummaryCard(club: Club) {
    val colors = DamoimTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surfaceVariant)
            .border(1.dp, colors.divider, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!club.imageUrl.isNullOrBlank()) {
            NetworkImage(url = club.imageUrl, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)), cornerRadius = 14.dp)
        } else {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(club.emblemColor)),
                contentAlignment = Alignment.Center,
            ) {
                Text(club.name.take(1), style = DamoimTheme.typography.titleMedium, color = colors.onPrimary)
            }
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(club.name, style = DamoimTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(2.dp))
            Text(
                DamoimStrings.clubMeta(club.category, club.memberCount),
                style = DamoimTheme.typography.caption,
                color = colors.textMuted,
            )
        }
    }
}

@Preview
@Composable
private fun JoinCompleteScreenPreview() {
    DamoimTheme { JoinCompleteScreen() }
}
