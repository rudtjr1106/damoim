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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.usecase.SubmitJoinCodeUseCase
import com.damoim.app.presentation.component.BackTopBar
import com.damoim.app.presentation.component.PrimaryButton
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 화면 03 가입 코드 입력 — Route(상태·이벤트·네비게이션).
 */
@Composable
fun JoinCodeRoute(
    viewModel: JoinCodeViewModel = viewModel { JoinCodeViewModel(AppGraph.submitJoinCodeUseCase) },
    onBack: () -> Unit = {},
    onNavigateComplete: (Club) -> Unit = {},
    onNavigateRejected: (Club, String) -> Unit = { _, _ -> },
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is JoinCodeSideEffect.NavigateToComplete -> onNavigateComplete(effect.club)
                is JoinCodeSideEffect.NavigateToRejected -> onNavigateRejected(effect.club, effect.reason)
            }
        }
    }

    JoinCodeScreen(
        state = state,
        onBack = onBack,
        onCodeChange = viewModel::onCodeChange,
        onSubmit = viewModel::onSubmit,
    )
}

/**
 * 화면 03 가입 코드 입력 — Screen(무상태 UI). 6자리 세그먼트 입력.
 */
@Composable
fun JoinCodeScreen(
    state: JoinCodeUiState = JoinCodeUiState(),
    onBack: () -> Unit = {},
    onCodeChange: (String) -> Unit = {},
    onSubmit: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    val focusRequester = remember { FocusRequester() }
    val inInspection = LocalInspectionMode.current

    // 프리뷰(inspection)에서는 미부착 FocusRequester 크래시 방지를 위해 요청하지 않음
    LaunchedEffect(Unit) {
        if (!inInspection) focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(colors.surface).safeDrawingPadding(),
    ) {
        BackTopBar(onBack = onBack)

        Column(Modifier.padding(horizontal = 24.dp)) {
            Spacer(Modifier.height(8.dp))
            Text(DamoimStrings.JOINCODE_TITLE, style = DamoimTheme.typography.headline, color = colors.textPrimary)
            Spacer(Modifier.height(8.dp))
            Text(DamoimStrings.JOINCODE_SUBTITLE, style = DamoimTheme.typography.body, color = colors.textMuted)
            Spacer(Modifier.height(28.dp))

            CodeCells(
                code = state.code,
                focusRequester = focusRequester,
                onChange = onCodeChange,
                onSubmit = onSubmit,
                isError = state.errorMessage != null,
            )
            Spacer(Modifier.height(12.dp))
            state.errorMessage?.let {
                Text(it, style = DamoimTheme.typography.caption, color = colors.error, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
            }
            Text(
                DamoimStrings.JOINCODE_TEST_HINT,
                style = DamoimTheme.typography.label,
                color = colors.textDisabled,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.weight(1f))
        Column(Modifier.padding(horizontal = 24.dp)) {
            PrimaryButton(
                text = DamoimStrings.JOINCODE_SUBMIT,
                onClick = onSubmit,
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

@Preview
@Composable
private fun JoinCodeScreenPreview() {
    DamoimTheme { JoinCodeScreen(state = JoinCodeUiState(code = "DA3")) }
}
