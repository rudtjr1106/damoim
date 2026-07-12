package com.damoim.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher =
    remember {
        object : CameraLauncher {
            // iOS 카메라(UIImagePickerController) 연동은 추후 — 호출부가 준비 중 안내 처리
            override fun launch() = onResult(null)
        }
    }

@Composable
actual fun rememberDocumentPickerLauncher(onResult: (PickedDocument?) -> Unit): DocumentPickerLauncher =
    remember {
        object : DocumentPickerLauncher {
            // iOS 문서 피커(UIDocumentPickerViewController) 연동은 추후
            override fun launch() = onResult(null)
        }
    }

@Composable
actual fun rememberShareText(): (String) -> Unit = remember {
    { text ->
        runCatching {
            val controller = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(controller, animated = true, completion = null)
        }
    }
}

@Composable
actual fun rememberSubscriptionBilling(): (String, (BillingResult) -> Unit) -> Unit = remember {
    // iOS StoreKit 연동은 추후 — 데모는 성공 처리
    { _, onResult -> onResult(BillingResult.SUCCESS) }
}

@Composable
actual fun rememberEmailComposer(): (String, String, String) -> Unit = remember {
    { address, _, _ ->
        runCatching {
            NSURL(string = "mailto:$address").let { UIApplication.sharedApplication.openURL(it) }
        }
        Unit
    }
}

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS는 시스템 뒤로가기 제스처가 없어 no-op (화면 내 뒤로 버튼으로 처리)
}
