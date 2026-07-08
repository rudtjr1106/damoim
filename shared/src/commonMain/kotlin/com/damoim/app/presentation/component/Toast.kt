package com.damoim.app.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.damoim.app.presentation.theme.DamoimTheme
import kotlinx.coroutines.delay

/**
 * 하단 토스트 오버레이 (디자인 58의 기본형). message가 non-null이면 잠깐 떴다가
 * [onDismiss]로 사라진다. 그룹 C의 정식 토스트가 나오기 전까지 공통 피드백용.
 */
@Composable
fun DamoimToastHost(
    message: String?,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(message) {
        if (message != null) {
            delay(2200)
            onDismiss()
        }
    }
    Box(modifier = modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 32.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DamoimTheme.colors.toastSurface)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(
                    text = message.orEmpty(),
                    style = DamoimTheme.typography.bodySmall,
                    color = DamoimTheme.colors.surface,
                )
            }
        }
    }
}
