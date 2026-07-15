package com.damoim.app.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme
import org.jetbrains.compose.resources.painterResource
import damoim.shared.generated.resources.Res
import damoim.shared.generated.resources.ic_kakao_login

/**
 * 공용 플로팅 액션 버튼(+). 게시판·일정 등에서 동일한 모양으로 쓴다.
 * 그림자는 은은하게(6dp) — 과한 그림자 대신 살짝 떠 보이는 정도.
 */
@Composable
fun DamoimFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DamoimTheme.colors
    Box(
        modifier = modifier
            .size(56.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(colors.primary)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { PlusIcon(tint = colors.onPrimary, modifier = Modifier.size(24.dp)) }
}

/**
 * 풀폭 기본 CTA 버튼. (디자인: primary 배경, radius 14, 15/700 흰 텍스트)
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = DamoimTheme.colors
    val clickable = enabled && !loading
    // 비활성(로딩 아님)일 때는 디자인처럼 primary를 흐리게(opacity .35) — 회색이 아니라 흐린 파랑
    val faded = !enabled && !loading
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .alpha(if (faded) 0.35f else 1f)
            .background(colors.primary)
            .clickable(
                enabled = clickable,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = colors.onPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text = text, style = DamoimTheme.typography.button, color = colors.onPrimary)
        }
    }
}

/**
 * 카카오 로그인 버튼. UMC-Product의 공식 카카오 버튼 벡터(ic_kakao_login, 350×48)를 그대로 사용한다.
 * 배경·심볼·"카카오 로그인" 텍스트가 벡터에 포함되어 있어 전체를 클릭 가능한 이미지로 렌더한다.
 */
@Composable
fun KakaoLoginButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    val colors = DamoimTheme.colors
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(350f / 48f)
            .clip(shape),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            Box(Modifier.fillMaxSize().background(colors.kakao))
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = colors.onKakao,
                strokeWidth = 2.dp,
            )
        } else {
            Image(
                painter = painterResource(Res.drawable.ic_kakao_login),
                contentDescription = DamoimStrings.LOGIN_KAKAO,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    ),
            )
        }
    }
}

/**
 * 보조 텍스트 버튼 (취소·건너뛰기 등). 배경 없음, 뮤트 텍스트.
 */
@Composable
fun SecondaryTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = DamoimTheme.typography.bodyStrong,
            color = DamoimTheme.colors.textMuted,
        )
    }
}
