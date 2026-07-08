package com.damoim.app.presentation.auth.kakao

import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.DefaultKakaoConsentItems
import com.damoim.app.presentation.component.CheckIcon
import com.damoim.app.presentation.component.KakaoBubbleIcon
import com.damoim.app.presentation.component.PrimaryButton
import com.damoim.app.presentation.component.SecondaryTextButton
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 02 카카오 로그인(동의). 하단 시트 형태로 동의 항목을 보여주고 로그인 진행.
 */
@Composable
fun KakaoConsentScreen(
    onNavigateProfileSetup: () -> Unit,
    onNavigateStart: () -> Unit,
    onShowError: (String) -> Unit,
    onCancel: () -> Unit,
    viewModel: KakaoConsentViewModel = viewModel { KakaoConsentViewModel(AppGraph.loginWithKakaoUseCase) },
) {
    val state by viewModel.uiState.collectAsState()
    val colors = DamoimTheme.colors

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                KakaoConsentSideEffect.NavigateToProfileSetup -> onNavigateProfileSetup()
                KakaoConsentSideEffect.NavigateToStart -> onNavigateStart()
                is KakaoConsentSideEffect.ShowError -> onShowError(effect.message)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(colors.scrim),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(colors.surface)
                .padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 40.dp),
        ) {
            // 핸들
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(44.dp).height(5.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(colors.divider),
            )
            Spacer(Modifier.height(20.dp))

            // 헤더
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(colors.kakao),
                    contentAlignment = Alignment.Center,
                ) { KakaoBubbleIcon(tint = colors.onKakao, modifier = Modifier.size(22.dp)) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("카카오 계정으로 계속하기", style = DamoimTheme.typography.titleMedium, color = colors.textPrimary)
                    Text("다모임이 아래 정보를 받습니다", style = DamoimTheme.typography.caption, color = colors.textMuted)
                }
            }
            Spacer(Modifier.height(20.dp))

            // 동의 항목
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceInput)
                    .padding(horizontal = 16.dp),
            ) {
                DefaultKakaoConsentItems.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CheckIcon(tint = colors.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(item.label, style = DamoimTheme.typography.body, color = colors.textPrimary, modifier = Modifier.weight(1f))
                        Text(
                            text = if (item.required) "필수" else "선택",
                            style = DamoimTheme.typography.label,
                            color = colors.textDisabled,
                        )
                    }
                    if (index < DefaultKakaoConsentItems.lastIndex) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            PrimaryButton(
                text = "동의하고 계속하기",
                onClick = viewModel::onAgree,
                loading = state.isLoading,
            )
            Spacer(Modifier.height(8.dp))
            SecondaryTextButton(text = "취소", onClick = onCancel)
        }
    }
}
