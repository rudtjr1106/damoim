package com.damoim.app.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 다모임 공용 텍스트 입력 컴포넌트. (UMC-Product `UTextField` 방식)
 *
 * `BasicTextField` + 커스텀 decorationBox로, 포커스 시 테두리색이 전환되고
 * placeholder를 직접 오버레이한다. 색·타이포는 [DamoimTheme] 토큰을 기본값으로 쓴다.
 *
 * 라벨/에러 문구는 이 컴포넌트 밖(호출부)에서 배치한다(합성 자유도 유지).
 * 6자리 코드 입력처럼 특수한 입력은 별도 컴포넌트를 사용한다.
 *
 * @param leading 입력창 왼쪽 슬롯(아이콘 등). null이면 없음
 * @param trailing 입력창 오른쪽 슬롯(지우기 버튼 등). null이면 없음
 * @param strokeColor 비포커스 테두리색. 기본값 투명(테두리 없음처럼 보임)
 * @param focusStrokeColor 포커스 테두리·커서색. 기본값 primary
 * @param interactionSource 포커스 상태를 호출부에서 관찰해야 할 때 외부 주입
 */
@Composable
fun DamoimTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true,
    isError: Boolean = false,
    textStyle: TextStyle = DamoimTheme.typography.body,
    textColor: Color = DamoimTheme.colors.textPrimary,
    placeholderColor: Color = DamoimTheme.colors.textDisabled,
    backgroundColor: Color = DamoimTheme.colors.surface,
    strokeColor: Color = DamoimTheme.colors.outline,
    focusStrokeColor: Color = DamoimTheme.colors.primary,
    errorColor: Color = DamoimTheme.colors.error,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.5.dp,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val isFocused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(cornerRadius)
    val currentStroke = when {
        isError -> errorColor
        isFocused && enabled -> focusStrokeColor
        else -> strokeColor
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = singleLine,
        textStyle = textStyle.copy(color = textColor),
        cursorBrush = SolidColor(if (isError) errorColor else focusStrokeColor),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor, shape)
                    .border(borderWidth, currentStroke, shape)
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
            ) {
                if (leading != null) {
                    leading()
                    Spacer(Modifier.width(10.dp))
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = textStyle, color = placeholderColor)
                    }
                    innerTextField()
                }
                if (trailing != null) {
                    Spacer(Modifier.width(10.dp))
                    trailing()
                }
            }
        },
    )
}

@Preview
@Composable
private fun DamoimTextFieldPreview() {
    DamoimTheme {
        Column(
            modifier = Modifier
                .background(DamoimTheme.colors.surface)
                .padding(16.dp),
        ) {
            var filled by remember { mutableStateOf("서연") }
            var empty by remember { mutableStateOf("") }
            DamoimTextField(value = filled, onValueChange = { filled = it }, placeholder = "이름을 입력해주세요")
            Box(Modifier.padding(top = 12.dp)) {
                DamoimTextField(value = empty, onValueChange = { empty = it }, placeholder = "이름을 입력해주세요")
            }
        }
    }
}
