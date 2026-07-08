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
import androidx.compose.ui.unit.dp
import com.damoim.app.presentation.component.DamoimLogoMark
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 32 시작하기(로그인 후 동아리 없음). 가입 코드로 참여 / 새 동아리 만들기 선택.
 */
@Composable
fun StartScreen(
    onJoinWithCode: () -> Unit,
    onCreateClub: () -> Unit,
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
        Text("반가워요, 서연님!\n어떻게 시작할까요?", style = DamoimTheme.typography.headline, color = colors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text("동아리에 참여하거나 새로 만들 수 있어요", style = DamoimTheme.typography.body, color = colors.textMuted)

        Spacer(Modifier.height(32.dp))
        // 주(primary) 액션 — 가입 코드로 참여
        ChoiceCard(
            title = "가입 코드로 참여하기",
            subtitle = "동아리장에게 받은 코드가 있어요",
            filled = true,
            onClick = onJoinWithCode,
        )
        Spacer(Modifier.height(12.dp))
        // 보조 액션 — 새 동아리 만들기
        ChoiceCard(
            title = "새 동아리 만들기",
            subtitle = "내가 동아리장이 되어 개설해요",
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
