package com.damoim.app.platform

import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import android.content.Intent

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
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) {
            onResult(null)
        } else {
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
            onResult(PickedDocument(name, formatSize(size)))
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
