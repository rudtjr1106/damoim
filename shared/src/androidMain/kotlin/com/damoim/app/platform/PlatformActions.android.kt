package com.damoim.app.platform

import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import com.damoim.app.domain.model.PurchaseProof
import com.damoim.app.domain.model.ResourceDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher {
    // 미리보기 촬영(썸네일) → JPEG 바이트로 인코딩해 첨부 업로드에 사용.
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        onResult(
            bitmap?.let { bmp ->
                java.io.ByteArrayOutputStream().use { out ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    out.toByteArray()
                }
            },
        )
    }
    return remember(launcher) {
        object : CameraLauncher {
            override fun launch() {
                runCatching { launcher.launch(null) }.onFailure { onResult(null) }
            }
        }
    }
}

@Composable
actual fun rememberDocumentPickerLauncher(onResult: (PickedDocument?) -> Unit): DocumentPickerLauncher {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) {
            onResult(null)
            return@rememberLauncherForActivityResult
        }
        // 파일 읽기는 IO 디스패처에서(메인 스레드 readBytes는 ANR 위험).
        scope.launch {
            val doc = withContext(Dispatchers.IO) {
                var name = "문서"
                var size = 0L
                runCatching {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (cursor.moveToFirst()) {
                            if (nameIdx >= 0) name = cursor.getString(nameIdx) ?: name
                            if (sizeIdx >= 0) size = cursor.getLong(sizeIdx)
                        }
                    }
                }
                // 신고된 크기가 상한 초과면 읽지 않고 포기(OOM 방지). size<=0(미상)은 읽어보되 상한 컷.
                if (size > ResourceDraft.MAX_UPLOAD_BYTES) return@withContext null
                val bytes = runCatching {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }.getOrNull() ?: return@withContext null
                if (bytes.size > ResourceDraft.MAX_UPLOAD_BYTES) return@withContext null
                val contentType = context.contentResolver.getType(uri)
                val realSize = if (size > 0) size else bytes.size.toLong()
                PickedDocument(name = name, sizeLabel = formatSize(realSize), bytes = bytes, contentType = contentType)
            }
            onResult(doc) // doc == null → 상한초과/읽기실패 → 화면은 취소처럼 처리
        }
    }
    return remember(launcher) {
        object : DocumentPickerLauncher {
            override fun launch() {
                runCatching {
                    launcher.launch(arrayOf("application/pdf", "application/*", "text/*"))
                }.onFailure { onResult(null) }
            }
        }
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1_048_576 -> {
        val mb = bytes * 10 / 1_048_576
        "${mb / 10}.${mb % 10}MB"
    }
    bytes >= 1024 -> "${bytes / 1024}KB"
    bytes > 0 -> "${bytes}B"
    else -> ""
}

@Composable
actual fun rememberSubscriptionBilling(): (String, String, (BillingResult, PurchaseProof?) -> Unit) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { productId: String, priceLabel: String, onResult: (BillingResult, PurchaseProof?) -> Unit ->
            // 실제 배포 시 Play BillingClient.launchBillingFlow(productId) → 성공 시 purchaseToken을
            // PurchaseProof("PLAY", productId, token)로 전달. 데모는 모의 다이얼로그(증빙 없음).
            android.app.AlertDialog.Builder(context)
                .setTitle("인앱 결제 (모의)")
                .setMessage("다모임 프리미엄 구독\n$priceLabel\n상품: $productId\n\n실제 결제는 Play Console 등록 후 연동됩니다.")
                .setPositiveButton("결제 성공") { _, _ -> onResult(BillingResult.SUCCESS, null) }
                .setNeutralButton("결제 실패") { _, _ -> onResult(BillingResult.FAILURE, null) }
                .setNegativeButton("취소") { _, _ -> onResult(BillingResult.CANCELLED, null) }
                .setOnCancelListener { onResult(BillingResult.CANCELLED, null) }
                .show()
            Unit
        }
    }
}

@Composable
actual fun rememberEmailComposer(): (String, String, String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { address: String, subject: String, body: String ->
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$address")
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            runCatching { context.startActivity(Intent.createChooser(intent, null)) }
            Unit
        }
    }
}

@Composable
actual fun rememberShareText(): (String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { text ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            runCatching { context.startActivity(Intent.createChooser(intent, null)) }
        }
    }
}

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}

actual suspend fun compressImage(bytes: ByteArray, maxDimension: Int, quality: Int): ByteArray =
    withContext(Dispatchers.Default) {
        runCatching {
            // 1) 경계만 디코드해 크기 파악(전체 디코드 없이 — 고해상 원본 OOM 방지).
            val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext bytes  // 이미지 아님
            if (bounds.outWidth <= maxDimension && bounds.outHeight <= maxDimension && bytes.size <= COMPRESS_SKIP_BYTES) {
                return@withContext bytes  // 이미 작음 — 재인코딩 열화·낭비 방지
            }
            // 2) inSampleSize(2의 거듭제곱)로 근사 축소 디코드 후, 정확한 치수로 한 번 더 보정.
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= maxDimension || bounds.outHeight / (sample * 2) >= maxDimension) sample *= 2
            val opts = android.graphics.BitmapFactory.Options().apply { inSampleSize = sample }
            val decoded = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                ?: return@withContext bytes
            val scale = minOf(maxDimension.toFloat() / decoded.width, maxDimension.toFloat() / decoded.height, 1f)
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    decoded,
                    (decoded.width * scale).toInt().coerceAtLeast(1),
                    (decoded.height * scale).toInt().coerceAtLeast(1),
                    true,
                )
            } else {
                decoded
            }
            // 3) 알파(PNG 투명)는 JPEG에서 검정으로 눌리므로 흰 배경에 합성 후 인코딩.
            val opaque = if (scaled.hasAlpha()) {
                Bitmap.createBitmap(scaled.width, scaled.height, Bitmap.Config.ARGB_8888).also { canvasBmp ->
                    android.graphics.Canvas(canvasBmp).apply {
                        drawColor(android.graphics.Color.WHITE)
                        drawBitmap(scaled, 0f, 0f, null)
                    }
                }
            } else {
                scaled
            }
            val out = java.io.ByteArrayOutputStream().use { s ->
                opaque.compress(Bitmap.CompressFormat.JPEG, quality, s)
                s.toByteArray()
            }
            if (out.size < bytes.size) out else bytes  // 오히려 커지면 원본 유지
        }.getOrDefault(bytes)
    }
