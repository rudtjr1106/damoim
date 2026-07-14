package com.damoim.app.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.damoim.app.data.remote.core.RawHttp
import com.damoim.app.presentation.theme.DamoimTheme
import com.preat.peekaboo.image.picker.toImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 원격 이미지 URL → [ImageBitmap] 인메모리 캐시(세션 지속). presigned URL은 서명 쿼리(`?...`)가
 * 매 발급마다 바뀌므로 **경로 부분만** 키로 삼아 같은 오브젝트 재조회 시 캐시가 적중하게 한다.
 * ImageStore(로컬 비트맵)를 대체하는 원격 로더 캐시.
 */
object NetworkImageCache {
    private val cache = mutableStateMapOf<String, ImageBitmap>()

    /** presigned 서명 쿼리를 제외한 안정 키(오브젝트 경로). */
    private fun keyOf(url: String): String = url.substringBefore("?")

    operator fun get(url: String): ImageBitmap? = cache[keyOf(url)]
    fun put(url: String, bmp: ImageBitmap) { cache[keyOf(url)] = bmp }
}

/**
 * URL 이미지를 비동기로 받아 렌더한다. 로딩 전/실패 시 회색 플레이스홀더.
 * [localKey]가 주어지면(업로드 전 방금 고른 사진) ImageStore 로컬 비트맵을 우선 표시한다.
 */
@Composable
fun NetworkImage(
    url: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    contentScale: ContentScale = ContentScale.Crop,
    localKey: String? = null,
) {
    val colors = DamoimTheme.colors
    val localBitmap = localKey?.let { ImageStore[it] }
    var remote by remember(url) { mutableStateOf(url?.let { NetworkImageCache[it] }) }
    LaunchedEffect(url) {
        if (url != null && !url.isBlank() && remote == null) {
            val bytes = RawHttp.getBytes(url)
            if (bytes != null) {
                val bmp = withContext(Dispatchers.Default) { runCatching { bytes.toImageBitmap() }.getOrNull() }
                if (bmp != null) {
                    NetworkImageCache.put(url, bmp)
                    remote = bmp
                }
            }
        }
    }
    val bitmap = localBitmap ?: remote
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
        )
    } else {
        Box(modifier.clip(RoundedCornerShape(cornerRadius)).background(colors.surfaceInput))
    }
}

/**
 * 원형 프로필 아바타. [url]이 있으면 네트워크 이미지, 없거나 **로드 실패**(만료된 presigned URL 등)면
 * [fallback](보통 이니셜 아바타)을 렌더한다 — 회색 빈 원이 영구히 남는 것을 방지.
 */
@Composable
fun NetworkAvatar(
    url: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit,
) {
    if (url.isNullOrBlank()) {
        fallback()
        return
    }
    var bitmap by remember(url) { mutableStateOf(NetworkImageCache[url]) }
    var failed by remember(url) { mutableStateOf(false) }
    LaunchedEffect(url) {
        if (bitmap == null) {
            val bytes = RawHttp.getBytes(url)
            val bmp = bytes?.let { withContext(Dispatchers.Default) { runCatching { it.toImageBitmap() }.getOrNull() } }
            if (bmp != null) {
                NetworkImageCache.put(url, bmp)
                bitmap = bmp
            } else {
                failed = true
            }
        }
    }
    val loaded = bitmap
    when {
        loaded != null -> Image(
            bitmap = loaded,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(CircleShape),
        )
        failed -> fallback()
        else -> Box(modifier.size(size).clip(CircleShape).background(DamoimTheme.colors.surfaceInput))
    }
}
