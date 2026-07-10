package com.damoim.app.presentation.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * 휴대폰 번호 표시 변환 — 상태는 숫자만 보관하고 화면에만 3-4-4 하이픈을 붙인다.
 * (상태 자체를 재포맷하면 커서가 꼬이므로 표시 계층에서만 처리)
 */
object PhoneNumberVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(11)
        val formatted = buildString {
            digits.forEachIndexed { i, c ->
                if (i == 3 || i == 7) append('-')
                append(c)
            }
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = when {
                offset <= 3 -> offset
                offset <= 7 -> offset + 1
                else -> offset + 2
            }.coerceAtMost(formatted.length)

            override fun transformedToOriginal(offset: Int): Int = when {
                offset <= 3 -> offset
                offset <= 8 -> offset - 1
                else -> offset - 2
            }.coerceIn(0, digits.length)
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
