package com.damoim.app.presentation.auth.joincode

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.usecase.SubmitJoinCodeUseCase
import com.damoim.app.presentation.component.BackTopBar
import com.damoim.app.presentation.component.InfoIcon
import com.damoim.app.presentation.component.WarningIcon
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
    onCreateClub: () -> Unit = {},
    onNavigateComplete: (Club) -> Unit = {},
    onNavigateRejected: (Club, String) -> Unit = { _, _ -> },
    onNavigateHome: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is JoinCodeSideEffect.NavigateToComplete -> onNavigateComplete(effect.club)
                is JoinCodeSideEffect.NavigateToRejected -> onNavigateRejected(effect.club, effect.reason)
                JoinCodeSideEffect.NavigateToHome -> onNavigateHome()
            }
        }
    }

    JoinCodeScreen(
        state = state,
        onBack = onBack,
        onCodeChange = viewModel::onCodeChange,
        onSubmit = viewModel::onSubmit,
        onCreateClub = onCreateClub,
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
    onCreateClub: () -> Unit = {},
) {
    val colors = DamoimTheme.colors
    val focusRequester = remember { FocusRequester() }
    val inInspection = LocalInspectionMode.current

    LaunchedEffect(Unit) {
        if (!inInspection) focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(colors.surface).safeDrawingPadding(),
    ) {
        BackTopBar(onBack = onBack)

        // 타이틀
        Column(Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text(DamoimStrings.JOINCODE_TITLE, style = DamoimTheme.typography.headline, color = colors.textPrimary)
            Spacer(Modifier.height(8.dp))
            Text(DamoimStrings.JOINCODE_SUBTITLE, style = DamoimTheme.typography.body, color = colors.textMuted)
        }

        // 6자리 세그먼트 입력
        CodeCells(
            code = state.code,
            focusRequester = focusRequester,
            onChange = onCodeChange,
            onSubmit = onSubmit,
        )

        // 안내 배너 (에러 시 빨간 안내로 전환)
        Spacer(Modifier.height(8.dp))
        InfoBanner(errorMessage = state.errorMessage)

        Spacer(Modifier.weight(1f))

        // 하단: 신청 버튼 + 동아리 생성 링크
        Column(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 48.dp)) {
            PrimaryButton(
                text = DamoimStrings.JOINCODE_SUBMIT,
                onClick = onSubmit,
                enabled = state.canSubmit,
                loading = state.isSubmitting,
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(DamoimStrings.JOINCODE_CREATE_PROMPT, style = DamoimTheme.typography.bodySmall, color = colors.textMuted)
                Text(
                    DamoimStrings.JOINCODE_CREATE_LINK,
                    style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCreateClub,
                    ),
                )
            }
        }
    }
}

@Composable
private fun CodeCells(
    code: String,
    focusRequester: FocusRequester,
    onChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val colors = DamoimTheme.colors
    val shape = RoundedCornerShape(14.dp)
    val cellTextStyle = DamoimTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)

    Box {
        BasicTextField(
            value = code,
            onValueChange = onChange,
            modifier = Modifier.focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                // Ascii 키보드로 한글 IME 전환을 억제(실입력 필터는 VM.onCodeChange가 담당).
                keyboardType = KeyboardType.Ascii,
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    repeat(SubmitJoinCodeUseCase.CODE_LENGTH) { index ->
                        val char = code.getOrNull(index)?.toString()
                        val isFilled = char != null
                        val isActive = index == code.length && code.length < SubmitJoinCodeUseCase.CODE_LENGTH

                        val cellBg = when {
                            isActive -> colors.surface
                            isFilled -> colors.primaryContainer
                            else -> colors.surfaceInput
                        }
                        val cellModifier = Modifier
                            .size(width = 46.dp, height = 56.dp)
                            .clip(shape)
                            .background(cellBg)
                            .then(
                                when {
                                    isActive -> Modifier.border(2.dp, colors.primary, shape)
                                    isFilled -> Modifier
                                    else -> Modifier.border(1.dp, colors.dividerLight, shape)
                                },
                            )

                        Box(modifier = cellModifier, contentAlignment = Alignment.Center) {
                            if (char != null) {
                                Text(char, style = cellTextStyle, color = colors.textPrimary)
                            } else if (isActive) {
                                BlinkingCursor()
                            }
                        }
                    }
                }
            },
        )
    }
}

/** 활성 셀의 깜빡이는 커서 바. */
@Composable
private fun BlinkingCursor() {
    val colors = DamoimTheme.colors
    val transition = rememberInfiniteTransition(label = "cursor")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "cursorAlpha",
    )
    Box(
        modifier = Modifier
            .width(2.dp)
            .height(24.dp)
            .background(colors.primary.copy(alpha = alpha)),
    )
}

/** 코드 입력 안내 배너. 에러가 있으면 빨간 오류 안내로 바뀐다. */
@Composable
private fun InfoBanner(errorMessage: String?) {
    val colors = DamoimTheme.colors
    val isError = errorMessage != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isError) colors.errorContainer else colors.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isError) {
            WarningIcon(tint = colors.error, modifier = Modifier.size(18.dp))
        } else {
            InfoIcon(tint = colors.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = errorMessage ?: DamoimStrings.JOINCODE_INFO,
            style = DamoimTheme.typography.bodySmall,
            color = if (isError) colors.error else colors.primaryDeep,
        )
    }
}

@Preview
@Composable
private fun JoinCodeScreenPreview() {
    DamoimTheme { JoinCodeScreen(state = JoinCodeUiState(code = "DM29")) }
}
