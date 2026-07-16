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
 * 원격 이미지 URL → [ImageBitmap] 인메모리 캐시. presigned URL은 서명 쿼리(`?...`)가 매 발급마다
 * 바뀌므로 **경로 부분만** 키로 삼아 같은 오브젝트 재조회 시 캐시가 적중하게 한다.
 *
 * 상한 있는 대략적 LRU(최대 [MAX_ENTRIES]) — 전엔 무제한이라 세션 내내 본 이미지가 전부 메모리에
 * 쌓여 앱이 무거워졌다. put 시 재삽입으로 최신화하고, 초과분은 가장 오래된 항목부터 제거한다.
 * 접근은 모두 컴포지션(Main) 스레드에서만 이뤄진다(디코드는 Default에서 하고 put은 Main으로 복귀).
 */
object NetworkImageCache {
    private const val MAX_ENTRIES = 80
    private val cache = LinkedHashMap<String, ImageBitmap>()

    /** presigned 서명 쿼리를 제외한 안정 키(오브젝트 경로). */
    private fun keyOf(url: String): String = url.substringBefore("?")

    operator fun get(url: String): ImageBitmap? = cache[keyOf(url)]

    fun put(url: String, bmp: ImageBitmap) {
        val key = keyOf(url)
        cache.remove(key)          // 재삽입 → 최근 사용으로 갱신(뒤로 이동)
        cache[key] = bmp
        while (cache.size > MAX_ENTRIES) {
            val it = cache.keys.iterator()
            if (it.hasNext()) { it.next(); it.remove() } else break
        }
    }
}

/**
 * URL 이미지를 비동기로 받아 렌더한다. 로딩 전/실패 시 회색 플레이스홀더.
 * [localKey]가 주어지면(업로드 전 방금 고른 사진) ImageStore 로컬 비트맵을 우선 표시한다.
 * [fallback]이 주어지면 **로드 실패**(서버에 바이트가 없거나 만료된 URL 등) 시 회색 박스 대신 이를
 * 렌더한다 — URL은 비어있지 않은데 바이트가 없는 경우 빈 회색 박스가 영구히 남는 것을 방지.
 * (실패는 캐시하지 않으므로 다시 업로드하면 즉시 정상 렌더된다.)
 */
@Composable
fun NetworkImage(
    url: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    contentScale: ContentScale = ContentScale.Crop,
    localKey: String? = null,
    fallback: (@Composable () -> Unit)? = null,
) {
    val colors = DamoimTheme.colors
    val localBitmap = localKey?.let { ImageStore[it] }
    var remote by remember(url) { mutableStateOf(url?.let { NetworkImageCache[it] }) }
    var failed by remember(url) { mutableStateOf(false) }
    LaunchedEffect(url) {
        if (url != null && !url.isBlank() && remote == null) {
            val bytes = RawHttp.getBytes(url)
            val bmp = bytes?.let { withContext(Dispatchers.Default) { runCatching { it.toImageBitmap() }.getOrNull() } }
            if (bmp != null) {
                NetworkImageCache.put(url, bmp)
                remote = bmp
            } else {
                failed = true
            }
        }
    }
    val bitmap = localBitmap ?: remote
    when {
        bitmap != null -> Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = contentScale,
            modifier = modifier.clip(RoundedCornerShape(cornerRadius)),
        )
        failed && fallback != null -> fallback()
        else -> Box(modifier.clip(RoundedCornerShape(cornerRadius)).background(colors.surfaceInput))
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
