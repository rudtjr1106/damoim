package com.damoim.app.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * 디자인의 인라인 SVG 아이콘들을 Canvas로 옮긴 것.
 * 아이콘 폰트/외부 리소스 의존 없이 commonMain에서 동작하도록 최소한만 직접 그린다.
 */

/** 카카오 말풍선. */
@Composable
fun KakaoBubbleIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            addOval(
                androidx.compose.ui.geometry.Rect(
                    left = w * 0.1f, top = h * 0.16f, right = w * 0.9f, bottom = h * 0.72f,
                ),
            )
            moveTo(w * 0.32f, h * 0.62f)
            lineTo(w * 0.24f, h * 0.86f)
            lineTo(w * 0.46f, h * 0.66f)
            close()
        }
        drawPath(path, color = tint)
    }
}

/** 체크 표시(완료). */
@Composable
fun CheckIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp), strokeWidth: Float = 2.2f) {
    Canvas(modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.53f)
            lineTo(size.width * 0.42f, size.height * 0.72f)
            lineTo(size.width * 0.80f, size.height * 0.30f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = strokeWidth * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

/** 뒤로가기 셰브론 (‹). */
@Composable
fun BackChevronIcon(tint: Color, modifier: Modifier = Modifier.size(24.dp), strokeWidth: Float = 1.8f) {
    Canvas(modifier) {
        val path = Path().apply {
            moveTo(size.width * 0.62f, size.height * 0.22f)
            lineTo(size.width * 0.34f, size.height * 0.5f)
            lineTo(size.width * 0.62f, size.height * 0.78f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = strokeWidth * density, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

/** 느낌표(거절/오류 상태 아이콘 내부). */
@Composable
fun ExclamationIcon(tint: Color, modifier: Modifier = Modifier.size(28.dp), strokeWidth: Float = 2.4f) {
    Canvas(modifier) {
        val cx = size.width / 2f
        drawLine(
            color = tint,
            start = Offset(cx, size.height * 0.24f),
            end = Offset(cx, size.height * 0.58f),
            strokeWidth = strokeWidth * density,
            cap = StrokeCap.Round,
        )
        drawCircle(color = tint, radius = strokeWidth * density * 0.7f, center = Offset(cx, size.height * 0.74f))
    }
}

/** 카메라(프로필 사진 편집 배지). 외곽선 스타일이라 어떤 배경에서도 또렷하다. */
@Composable
fun CameraIcon(tint: Color, modifier: Modifier = Modifier.size(16.dp), strokeWidth: Float = 1.6f) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val sw = strokeWidth * density
        // 몸체
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.12f, h * 0.34f),
            size = Size(w * 0.76f, h * 0.44f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
            style = Stroke(width = sw),
        )
        // 상단 뷰파인더 돌기
        drawLine(tint, Offset(w * 0.40f, h * 0.34f), Offset(w * 0.45f, h * 0.24f), sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.60f, h * 0.34f), Offset(w * 0.55f, h * 0.24f), sw, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.45f, h * 0.24f), Offset(w * 0.55f, h * 0.24f), sw, cap = StrokeCap.Round)
        // 렌즈
        drawCircle(color = tint, radius = w * 0.13f, center = Offset(w * 0.5f, h * 0.56f), style = Stroke(width = sw))
    }
}
