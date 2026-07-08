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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.presentation.component.DamoimLogoMark
import com.damoim.app.presentation.component.KakaoButton
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 01 로그인/온보딩 — Route(상태·이벤트·네비게이션).
 * 카카오 로그인을 직접 수행하고 결과에 따라 프로필 설정/시작하기로 이동한다.
 */
@Composable
fun LoginRoute(
    viewModel: LoginViewModel = viewModel { LoginViewModel(AppGraph.loginWithKakaoUseCase) },
    onNavigateProfileSetup: () -> Unit = {},
    onNavigateStart: () -> Unit = {},
    onShowError: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                LoginSideEffect.NavigateToProfileSetup -> onNavigateProfileSetup()
                LoginSideEffect.NavigateToStart -> onNavigateStart()
                is LoginSideEffect.ShowError -> onShowError(effect.message)
            }
        }
    }

    LoginScreen(
        isLoading = state.isLoading,
        onKakaoClick = viewModel::onKakaoLogin,
    )
}

/**
 * 화면 01 로그인/온보딩 — Screen(무상태 UI). 브랜드 배경 + 로고 + 카카오 시작하기.
 */
@Composable
fun LoginScreen(
    isLoading: Boolean = false,
    onKakaoClick: () -> Unit = {},
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
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DamoimLogoMark(modifier = Modifier.size(88.dp), onBrand = true)
            Spacer(Modifier.height(20.dp))
            Text(text = DamoimStrings.APP_NAME, style = DamoimTheme.typography.display, color = colors.onPrimary)
            Spacer(Modifier.height(8.dp))
            Text(
                text = DamoimStrings.LOGIN_TAGLINE,
                style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.Normal),
                color = colors.onPrimary.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
        }

        KakaoButton(text = DamoimStrings.LOGIN_KAKAO_START, onClick = onKakaoClick, loading = isLoading)
        Spacer(Modifier.height(12.dp))
        Text(
            text = DamoimStrings.LOGIN_FOOTER,
            style = DamoimTheme.typography.caption,
            color = colors.onPrimary.copy(alpha = 0.55f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 40.dp),
        )
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    DamoimTheme { LoginScreen() }
}
