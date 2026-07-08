package com.damoim.app.presentation.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.damoim.app.presentation.component.DamoimLogoMark
import com.damoim.app.presentation.component.KakaoButton
import androidx.compose.material3.Text
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 01 로그인 / 온보딩. 브랜드 배경 + 로고 + 카카오 시작하기.
 */
@Composable
fun LoginScreen(
    onKakaoClick: () -> Unit,
) {
    val colors = DamoimTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primary)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 로고 + 서비스명 (중앙)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DamoimLogoMark(modifier = Modifier.size(88.dp), onBrand = true)
            Spacer(Modifier.height(20.dp))
            Text(text = "다모임", style = DamoimTheme.typography.display, color = colors.onPrimary)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "우리 동아리의 모든 것을\n한곳에서",
                style = DamoimTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal),
                color = colors.onPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
        }

        // 하단 CTA
        KakaoButton(text = "카카오로 시작하기", onClick = onKakaoClick)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "로그인 후 가입 코드로 동아리에 참여할 수 있어요\n시작하면 이용약관 및 개인정보 처리방침에 동의하게 됩니다",
            style = DamoimTheme.typography.caption,
            color = colors.onPrimary.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 40.dp),
        )
    }
}
