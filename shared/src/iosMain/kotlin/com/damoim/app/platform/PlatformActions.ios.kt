package com.damoim.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.damoim.app.domain.model.ResourceDraft
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeData
import platform.UniformTypeIdentifiers.UTTypePDF
import platform.UniformTypeIdentifiers.UTTypePlainText
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

/**
 * UIDocumentPickerViewController 델리게이트. asCopy로 앱 tmp에 복사된 파일을 읽어 바이트로 반환한다.
 * (asCopy=true라 보안 스코프 접근 없이 바로 읽힘.) 상한 초과·읽기 실패는 null(호출부가 취소처럼 처리).
 */
private class DocPickerDelegate(
    private val onResult: (PickedDocument?) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        onResult(url?.let { readPickedDocument(it) })
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onResult(null)
    }
}

/** 선택 파일 → PickedDocument. 전체 로드 전 파일 크기를 선확인(OOM 방지), 상한(25MB) 초과·실패면 null. */
@OptIn(ExperimentalForeignApi::class)
private fun readPickedDocument(url: NSURL): PickedDocument? {
    val path = url.path ?: return null
    val size = (NSFileManager.defaultManager.attributesOfItemAtPath(path, null)?.get(NSFileSize) as? NSNumber)
        ?.longLongValue ?: -1L
    if (size < 0 || size > ResourceDraft.MAX_UPLOAD_BYTES) return null
    val data = NSData.dataWithContentsOfURL(url) ?: return null
    if (data.length.toLong() > ResourceDraft.MAX_UPLOAD_BYTES) return null
    val name = url.lastPathComponent ?: "문서"
    return PickedDocument(name = name, sizeLabel = formatFileSize(size), bytes = data.toByteArray(), contentType = null)
}

private fun formatFileSize(bytes: Long): String = when {
    bytes >= 1_048_576 -> { val mb = bytes * 10 / 1_048_576; "${mb / 10}.${mb % 10}MB" }
    bytes >= 1024 -> "${bytes / 1024}KB"
    bytes > 0 -> "${bytes}B"
    else -> ""
}

@Composable
actual fun rememberDocumentPickerLauncher(onResult: (PickedDocument?) -> Unit): DocumentPickerLauncher = remember {
    object : DocumentPickerLauncher {
        // 콜백까지 델리게이트를 살려두는 강한 참조.
        private var delegate: DocPickerDelegate? = null

        override fun launch() {
            val root = UIApplication.sharedApplication.keyWindow?.rootViewController
            if (root == null) { onResult(null); return }
            val d = DocPickerDelegate(onResult)
            delegate = d
            val types: List<UTType> = listOf(UTTypePDF, UTTypePlainText, UTTypeData)
            val picker = UIDocumentPickerViewController(forOpeningContentTypes = types, asCopy = true)
            picker.delegate = d
            root.presentViewController(picker, animated = true, completion = null)
        }
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
