package com.damoim.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import platform.posix.memcpy

/** NSData → ByteArray(첨부 업로드용). */
@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val len = length.toInt()
    if (len == 0) return ByteArray(0)
    val out = ByteArray(len)
    out.usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
    return out
}

/**
 * UIImagePickerController(.camera) 델리게이트. 촬영 결과 UIImage를 JPEG 바이트로 변환해 돌려준다.
 * UIKit이 델리게이트를 약하게 참조하므로, 런처가 이 객체를 강한 참조로 붙잡아 콜백까지 살려둔다.
 */
private class CameraPickerDelegate(
    private val onResult: (ByteArray?) -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val jpeg = image?.let { UIImageJPEGRepresentation(it, 0.9) }
        picker.dismissViewControllerAnimated(true, null)
        onResult(jpeg?.toByteArray())
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, null)
        onResult(null)
    }
}

@Composable
actual fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher = remember {
    object : CameraLauncher {
        // 콜백까지 델리게이트를 살려두는 강한 참조.
        private var delegate: CameraPickerDelegate? = null

        override fun launch() {
            val cameraType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            // 시뮬레이터 등 카메라 미탑재 기기에서는 사용 불가 → 호출부가 준비중/미지원 안내.
            if (!UIImagePickerController.isSourceTypeAvailable(cameraType)) {
                onResult(null); return
            }
            val root = UIApplication.sharedApplication.keyWindow?.rootViewController
            if (root == null) { onResult(null); return }
            val d = CameraPickerDelegate(onResult)
            delegate = d
            val picker = UIImagePickerController()
            picker.sourceType = cameraType
            picker.delegate = d
            root.presentViewController(picker, animated = true, completion = null)
        }
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
