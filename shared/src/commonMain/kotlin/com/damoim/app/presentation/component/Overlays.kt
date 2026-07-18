package com.damoim.app.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

/** 클릭 소비용(리플 없음) modifier. */
internal fun Modifier.noRippleClick(onClick: () -> Unit): Modifier = composed {
    clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
}

/** 점선 테두리(드롭존·대시 버튼·타일). Compose에 dashed border modifier가 없어 직접 그린다. */
fun Modifier.dashedBorder(color: Color, width: Dp, cornerRadius: Dp): Modifier = drawBehind {
    val stroke = width.toPx()
    drawRoundRect(
        color = color,
        style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(stroke * 4, stroke * 3))),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
    )
}

/**
 * 하단 바텀시트 오버레이. **호출한 화면 위에** scrim + 슬라이드업 시트로 렌더된다(새 화면 push 아님).
 * 호출부: `if (showSheet) DamoimBottomSheet(onDismiss = { showSheet = false }) { ...content... }`
 *
 * @param showGrabber 상단 그래버 핸들 표시 여부
 * @param content 시트 본문(ColumnScope). 좌우/하단 패딩은 content가 직접 준다(디자인마다 다름).
 */
@Composable
fun DamoimBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    showGrabber: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = DamoimTheme.colors
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var visible by remember { mutableStateOf(false) }
    // 그래버를 아래로 끌면 닫히도록 하는 드래그 상태.
    val scope = rememberCoroutineScope()
    val dragY = remember { Animatable(0f) }
    val dismissThresholdPx = with(LocalDensity.current) { 90.dp.toPx() }
    // 시트가 뜨면 입력 포커스를 해제해 소프트 키보드를 내린다(입력 중 시트/다이얼로그 진입 시 키보드 잔류 방지).
    LaunchedEffect(Unit) {
        focusManager.clearFocus(force = true)
        keyboard?.hide()
        visible = true
    }
    Box(modifier.fillMaxSize()) {
        AnimatedVisibility(visible, enter = fadeIn(tween(180)), exit = fadeOut(tween(150))) {
            Box(Modifier.fillMaxSize().background(colors.scrim).noRippleClick(onDismiss))
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(tween(240)) { it } + fadeIn(tween(200)),
            exit = slideOutVertically(tween(200)) { it } + fadeOut(tween(150)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    // 드래그한 만큼 시트를 아래로 민다(아래 방향만).
                    .offset { IntOffset(0, dragY.value.roundToInt()) }
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(colors.surface)
                    // 시트 자체는 클릭을 소비(뒤 scrim으로 전달 안 되게)
                    .noRippleClick { },
            ) {
                if (showGrabber) SheetGrabber(
                    onDrag = { delta -> scope.launch { dragY.snapTo((dragY.value + delta).coerceAtLeast(0f)) } },
                    onDragEnd = {
                        scope.launch {
                            if (dragY.value >= dismissThresholdPx) onDismiss()
                            else dragY.animateTo(0f, tween(180))
                        }
                    },
                )
                content()
            }
        }
    }
}

/** 중앙 다이얼로그 오버레이. scrim + 페이드/스케일 인. */
@Composable
fun DamoimDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = DamoimTheme.colors
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var visible by remember { mutableStateOf(false) }
    // 다이얼로그가 뜨면 입력 포커스를 해제해 소프트 키보드를 내린다.
    LaunchedEffect(Unit) {
        focusManager.clearFocus(force = true)
        keyboard?.hide()
        visible = true
    }
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(visible, enter = fadeIn(tween(180)), exit = fadeOut(tween(150))) {
            Box(Modifier.fillMaxSize().background(colors.scrim).noRippleClick(onDismiss))
        }
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(180)) + scaleIn(tween(200), initialScale = 0.92f),
            exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.92f),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                    .clip(RoundedCornerShape(24.dp)).background(colors.surface).noRippleClick { },
            ) { content() }
        }
    }
}

/**
 * 시트 상단 그래버 핸들(44×5). [onDrag]가 주어지면 아래로 끌어 시트를 닫을 수 있는 드래그 핸들이 된다.
 * 터치 영역을 넓히려 상하 패딩을 넉넉히 준다.
 */
@Composable
fun SheetGrabber(
    onDrag: ((Float) -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
) {
    Box(
        Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 10.dp)
            .then(
                if (onDrag != null) {
                    Modifier.pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, dragAmount -> change.consume(); onDrag(dragAmount) },
                            onDragEnd = { onDragEnd?.invoke() },
                            onDragCancel = { onDragEnd?.invoke() },
                        )
                    }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.width(44.dp).height(5.dp).clip(RoundedCornerShape(99.dp)).background(DamoimTheme.colors.divider))
    }
}

/**
 * 시트 액션 메뉴 행(54/55). 좌측 아이콘 + 텍스트 (+ 우측 슬롯). 파괴적 액션은 [textColor]=error.
 */
@Composable
fun SheetActionRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = DamoimTheme.colors.textPrimary,
    icon: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    showDivider: Boolean = true,
) {
    val colors = DamoimTheme.colors
    Column {
        Row(
            modifier = modifier.fillMaxWidth().noRippleClick(onClick).padding(horizontal = 6.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (icon != null) icon()
            Text(text, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp), color = textColor, modifier = Modifier.weight(1f))
            if (trailing != null) trailing()
        }
        if (showDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
    }
}

/** 공용 확인 다이얼로그(경고 아이콘 + 제목 + 설명 + 취소/확정). 파괴적이면 [destructive]. */
@Composable
fun DamoimConfirmDialog(
    title: String,
    desc: String,
    confirm: String,
    destructive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = DamoimTheme.colors
    val accent = if (destructive) colors.error else colors.primary
    DamoimDialog(onDismiss = onDismiss) {
        androidx.compose.foundation.layout.Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(999.dp)).background(if (destructive) colors.errorContainer else colors.primaryContainer), contentAlignment = Alignment.Center) {
                WarningIcon(accent, Modifier.size(26.dp))
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
            Text(title, style = DamoimTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold), color = colors.textPrimary)
            Text(desc, style = DamoimTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Normal), color = colors.textMuted, modifier = Modifier.padding(bottom = 8.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                DialogButton(DamoimStrings.COMMON_CANCEL, colors.surfaceVariant, colors.textTertiary, Modifier.weight(1f), onDismiss)
                DialogButton(confirm, accent, colors.onPrimary, Modifier.weight(1f), onConfirm)
            }
        }
    }
}

/** 다이얼로그 하단 액션 버튼(취소/삭제 등). 파괴적 액션은 [bg]=error. */
@Composable
fun DialogButton(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).background(bg).noRippleClick(onClick).padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = DamoimTheme.typography.bodyStrong, color = fg) }
}

/** 시트 하단 '닫기' 버튼(surfaceVariant). */
@Composable
fun SheetCloseButton(onClick: () -> Unit, text: String = DamoimStrings.COMMON_CLOSE) {
    val colors = DamoimTheme.colors
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.surfaceVariant)
            .noRippleClick(onClick).padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = DamoimTheme.typography.bodyStrong, color = colors.textTertiary)
    }
}
