package com.damoim.app.presentation.auth.joincode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.usecase.SubmitJoinCodeUseCase
import com.damoim.app.presentation.component.BackTopBar
import com.damoim.app.presentation.component.PrimaryButton
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 03 가입 코드 입력. 6자리 세그먼트 입력 → 신청.
 */
@Composable
fun JoinCodeScreen(
    onBack: () -> Unit,
    onNavigateComplete: (com.damoim.app.domain.model.Club) -> Unit,
    onNavigateRejected: (com.damoim.app.domain.model.Club, String) -> Unit,
    viewModel: JoinCodeViewModel = viewModel { JoinCodeViewModel(AppGraph.submitJoinCodeUseCase) },
) {
    val state by viewModel.uiState.collectAsState()
    val colors = DamoimTheme.colors
    val focusRequester = remember { FocusRequester() }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is JoinCodeSideEffect.NavigateToComplete -> onNavigateComplete(effect.club)
                is JoinCodeSideEffect.NavigateToRejected -> onNavigateRejected(effect.club, effect.reason)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(colors.surface).safeDrawingPadding(),
    ) {
        BackTopBar(onBack = onBack)

        Column(Modifier.padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(8.dp))
            Text("가입 코드를\n입력해주세요", style = DamoimTheme.typography.headline, color = colors.textPrimary)
            Spacer(Modifier.height(8.dp))
            Text(
                "동아리장에게 받은 6자리 코드를 입력하면\n가입 신청이 전달돼요",
                style = DamoimTheme.typography.body,
                color = colors.textMuted,
            )
            Spacer(Modifier.height(28.dp))

            CodeCells(
                code = state.code,
                focusRequester = focusRequester,
                onChange = viewModel::onCodeChange,
                onSubmit = viewModel::onSubmit,
                isError = state.errorMessage != null,
            )
            Spacer(Modifier.height(12.dp))
            state.errorMessage?.let {
                Text(it, style = DamoimTheme.typography.caption, color = colors.error, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
            }
            // 데모용 코드 안내 (서버 연동 시 제거)
            Text(
                "테스트 코드 — DAMOIM: 신청 완료 · REJECT: 거절 · EXPIRE: 오류",
                style = DamoimTheme.typography.label,
                color = colors.textDisabled,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.weight(1f))
        Column(Modifier.padding(horizontal = 24.dp)) {
            PrimaryButton(
                text = "가입 신청하기",
                onClick = viewModel::onSubmit,
                enabled = state.canSubmit,
                loading = state.isSubmitting,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CodeCells(
    code: String,
    focusRequester: FocusRequester,
    onChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isError: Boolean,
) {
    val colors = DamoimTheme.colors
    Box {
        // 보이지 않는 입력 필드가 포커스/키보드를 담당 (셀은 decoration으로 렌더)
        BasicTextField(
            value = code,
            onValueChange = onChange,
            modifier = Modifier.focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    repeat(SubmitJoinCodeUseCase.CODE_LENGTH) { index ->
                        val char = code.getOrNull(index)?.toString() ?: ""
                        val active = index == code.length
                        val borderColor = when {
                            isError -> colors.error
                            active -> colors.primary
                            char.isNotEmpty() -> colors.outlineStrong
                            else -> colors.outline
                        }
                        Box(
                            modifier = Modifier
                                .size(width = 46.dp, height = 56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.surfaceInput)
                                .border(if (active || char.isNotEmpty()) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(char, style = DamoimTheme.typography.titleLarge, color = colors.textPrimary)
                        }
                    }
                }
            },
        )
    }
}
