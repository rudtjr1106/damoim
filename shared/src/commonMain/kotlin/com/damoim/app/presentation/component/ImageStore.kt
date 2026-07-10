package com.damoim.app.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 첨부 이미지 인메모리 저장소. 게시글 첨부는 도메인엔 라벨(String)만 남기고
 * 실제 비트맵은 여기 보관한다 — 서버 도입 시 라벨 자리에 URL이 들어가고
 * 이 저장소는 이미지 로더 캐시로 대체된다.
 */
object ImageStore {
    private val images = mutableStateMapOf<String, ImageBitmap>()
    private var counter = 0

    /** 비트맵 저장 후 참조 라벨 반환. */
    fun put(bitmap: ImageBitmap): String {
        val label = "img_${counter++}"
        images[label] = bitmap
        return label
    }

    operator fun get(label: String): ImageBitmap? = images[label]
}

/**
 * 첨부 이미지 렌더러 — [ImageStore]에 비트맵이 있으면 실제 이미지, 없으면(시드 데이터 등)
 * 회색 플레이스홀더에 라벨을 표시한다.
 */
@Composable
fun AttachedImage(
    label: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val bitmap = ImageStore[label]
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
        )
    } else {
        val colors = DamoimTheme.colors
        Box(
            modifier = modifier.clip(RoundedCornerShape(cornerRadius)).background(colors.surfaceInput),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, style = DamoimTheme.typography.label, color = colors.textDisabled)
        }
    }
}
