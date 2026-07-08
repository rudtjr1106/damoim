package com.damoim.app.presentation.auth.start

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.damoim.app.presentation.component.DamoimLogoMark
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 32 시작하기 — Route(네비게이션). 로그인 세션이 붙으면 userName은 세션에서 받아온다(지금은 Mock).
 */
@Composable
fun StartRoute(
    userName: String = DamoimStrings.PREVIEW_USER_NAME,
    onJoinWithCode: () -> Unit = {},
    onCreateClub: () -> Unit = {},
) {
    StartScreen(
        userName = userName,
        onJoinWithCode = onJoinWithCode,
        onCreateClub = onCreateClub,
    )
}

/**
 * 화면 32 시작하기 — Screen(무상태 UI). 가입 코드로 참여 / 새 동아리 만들기 선택.
 */
@Composable
fun StartScreen(
    userName: String = DamoimStrings.PREVIEW_USER_NAME,
    onJoinWithCode: () -> Unit = {},
    onCreateClub: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(48.dp))
        DamoimLogoMark(modifier = Modifier.size(64.dp), onBrand = false)
        Spacer(Modifier.height(20.dp))
        Text(DamoimStrings.startGreeting(userName), style = DamoimTheme.typography.headline, color = colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text(DamoimStrings.START_SUBTITLE, style = DamoimTheme.typography.body, color = colors.textMuted)

        Spacer(Modifier.height(32.dp))
        ChoiceCard(
            title = DamoimStrings.START_JOIN_TITLE,
            subtitle = DamoimStrings.START_JOIN_SUBTITLE,
            filled = true,
            onClick = onJoinWithCode,
        )
        Spacer(Modifier.height(12.dp))
        ChoiceCard(
            title = DamoimStrings.START_CREATE_TITLE,
            subtitle = DamoimStrings.START_CREATE_SUBTITLE,
            filled = false,
            onClick = onCreateClub,
        )
    }
}

@Composable
private fun ChoiceCard(
    title: String,
    subtitle: String,
    filled: Boolean,
    onClick: () -> Unit,
) {
    val colors = DamoimTheme.colors
    val bg = if (filled) colors.primary else colors.surface
    val titleColor = if (filled) colors.onPrimary else colors.textPrimary
    val subColor = if (filled) colors.onPrimary.copy(alpha = 0.8f) else colors.textMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .then(
                if (filled) Modifier.background(bg)
                else Modifier.background(bg).border(1.dp, colors.divider, RoundedCornerShape(18.dp)),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = DamoimTheme.typography.titleMedium, color = titleColor)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = DamoimTheme.typography.caption, color = subColor)
        }
    }
}

@Preview
@Composable
private fun StartScreenPreview() {
    DamoimTheme { StartScreen() }
}
