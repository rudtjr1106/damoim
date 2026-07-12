package com.damoim.app.platform

import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import com.damoim.app.domain.model.ResourceDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun rememberCameraLauncher(onResult: (ImageBitmap?) -> Unit): CameraLauncher {
    // 미리보기 촬영(풀사이즈 파일 저장 불필요 — 첨부 표시용)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        onResult(bitmap?.asImageBitmap())
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
actual fun rememberSubscriptionBilling(): (String, (BillingResult) -> Unit) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { priceLabel: String, onResult: (BillingResult) -> Unit ->
            // 실제 배포 시 Google Play BillingClient.launchBillingFlow로 교체. 데모는 모의 결제 다이얼로그.
            android.app.AlertDialog.Builder(context)
                .setTitle("인앱 결제 (모의)")
                .setMessage("다모임 프리미엄 구독\n$priceLabel\n\n실제 결제는 스토어 등록 후 연동됩니다.")
                .setPositiveButton("결제 성공") { _, _ -> onResult(BillingResult.SUCCESS) }
                .setNeutralButton("결제 실패") { _, _ -> onResult(BillingResult.FAILURE) }
                .setNegativeButton("취소") { _, _ -> onResult(BillingResult.CANCELLED) }
                .setOnCancelListener { onResult(BillingResult.CANCELLED) }
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
