package com.damoim.app.presentation.auth.start

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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damoim.app.presentation.component.ChevronRightIcon
import com.damoim.app.presentation.component.DamoimLogoBadge
import com.damoim.app.presentation.component.LockIcon
import com.damoim.app.presentation.component.PeopleIcon
import com.damoim.app.presentation.component.PlusIcon
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
 * 화면 32 시작하기 — Screen(무상태 UI). 소속 동아리 없음 상태.
 * 상단 바(로고+아바타) · 빈 상태 일러스트 · 참여/생성 카드 2개 · 하단 안내문.
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
            .safeDrawingPadding(),
    ) {
        // 상단 바: 로고 + "다모임" | 아바타
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DamoimLogoBadge(modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(8.dp))
                Text(DamoimStrings.APP_NAME, style = DamoimTheme.typography.titleMedium, color = colors.textPrimary)
            }
            Box(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(colors.primaryContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    userName.take(2),
                    style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold),
                    color = colors.primaryDeep,
                )
            }
        }

        // 본문(수직 중앙)
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            // 빈 상태 일러스트 + 타이틀
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                EmptyClubIllustration()
                Spacer(Modifier.height(14.dp))
                Text(DamoimStrings.START_EMPTY_TITLE, style = DamoimTheme.typography.titleLarge, color = colors.textPrimary)
                Spacer(Modifier.height(6.dp))
                Text(DamoimStrings.START_EMPTY_SUBTITLE, style = DamoimTheme.typography.body, color = colors.textMuted)
            }

            Spacer(Modifier.height(32.dp))

            // 선택 카드 2개
            ChoiceCard(
                filled = true,
                icon = { LockIcon(tint = colors.onPrimary, modifier = Modifier.size(21.dp)) },
                title = DamoimStrings.START_JOIN_TITLE,
                subtitle = DamoimStrings.START_JOIN_SUBTITLE,
                subtitleColor = colors.textMuted,
                onClick = onJoinWithCode,
            )
            Spacer(Modifier.height(10.dp))
            ChoiceCard(
                filled = false,
                icon = { PlusIcon(tint = colors.textTertiary, modifier = Modifier.size(21.dp)) },
                title = DamoimStrings.START_CREATE_TITLE,
                subtitle = DamoimStrings.START_CREATE_SUBTITLE,
                subtitleColor = colors.textMuted,
                onClick = onCreateClub,
            )
        }

        // 하단 안내
        Text(
            DamoimStrings.START_FOOTER,
            style = DamoimTheme.typography.caption,
            color = colors.textDisabled,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 44.dp),
        )
    }
}

/** 소속 동아리 없음 빈 상태 일러스트 (120dp, 점선 테두리 + 사람 아이콘). */
@Composable
private fun EmptyClubIllustration() {
    val colors = DamoimTheme.colors
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(colors.primaryContainer)
            .drawBehind {
                drawRoundRect(
                    color = colors.accentSkySoft,
                    cornerRadius = CornerRadius(36.dp.toPx(), 36.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
                    ),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        PeopleIcon(tint = colors.textMuted, modifier = Modifier.size(48.dp))
    }
}

/**
 * 선택 카드 — 아이콘 박스 + 제목/부제 + 오른쪽 화살표.
 * filled=true 이면 primary 강조(파란 테두리·틴트 배경·파란 아이콘 박스).
 */
@Composable
private fun ChoiceCard(
    filled: Boolean,
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    subtitleColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    val colors = DamoimTheme.colors
    val shape = RoundedCornerShape(18.dp)
    val containerColor = if (filled) colors.primaryContainer else colors.surface
    val borderColor = if (filled) colors.primary else colors.divider
    val borderWidth = if (filled) 1.5.dp else 1.dp
    val iconBoxColor = if (filled) colors.primary else colors.surfaceVariant
    val chevronTint = if (filled) colors.primary else colors.outlineStrong

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(containerColor)
            .border(borderWidth, borderColor, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(iconBoxColor),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = DamoimTheme.typography.titleMedium.copy(fontSize = 15.sp), color = colors.textPrimary)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = DamoimTheme.typography.caption, color = subtitleColor)
        }
        Spacer(Modifier.width(14.dp))
        ChevronRightIcon(tint = chevronTint, modifier = Modifier.size(18.dp))
    }
}

@Preview
@Composable
private fun StartScreenPreview() {
    DamoimTheme { StartScreen() }
}
