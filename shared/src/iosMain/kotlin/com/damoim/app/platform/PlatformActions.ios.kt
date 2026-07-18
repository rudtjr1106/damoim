package com.damoim.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.damoim.app.domain.model.PurchaseProof
import com.damoim.app.domain.model.ResourceDraft
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.StoreKit.SKPayment
import platform.StoreKit.SKPaymentQueue
import platform.StoreKit.SKPaymentTransaction
import platform.StoreKit.SKPaymentTransactionObserverProtocol
import platform.StoreKit.SKPaymentTransactionState
import platform.StoreKit.SKProduct
import platform.StoreKit.SKProductsRequest
import platform.StoreKit.SKProductsRequestDelegateProtocol
import platform.StoreKit.SKProductsResponse
import platform.StoreKit.SKRequest
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIRectFill
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeData
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.UniformTypeIdentifiers.UTTypeMovie
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
            val types: List<UTType> = listOf(UTTypePDF, UTTypePlainText, UTTypeImage, UTTypeMovie, UTTypeData)
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

// iOS 기기 캘린더 추가는 현재 no-op(EventKit 연동 시 구현). Android만 실제 동작.
@Composable
actual fun rememberCalendarAdder(): (CalendarEvent) -> Unit = remember { { _ -> } }

/**
 * StoreKit 2(Swift) 결제 구현 주입점. iosApp이 앱 시작 시 `IosBillingRegistry.impl`을 등록하면 그걸 우선 쓰고,
 * 없으면 아래 StoreKit 1 폴백을 쓴다. onResult의 두번째 인자 = 서버 검증용 JWS 서명 트랜잭션(성공 시).
 */
interface IosSubscriptionBilling {
    fun purchase(productId: String, onResult: (BillingResult, String?) -> Unit)
}

object IosBillingRegistry {
    var impl: IosSubscriptionBilling? = null
}

/**
 * StoreKit 1(SKPaymentQueue) 폴백 — StoreKit 2 브릿지가 등록되지 않았을 때. 앱 전역 단일 옵저버로 관찰.
 * (StoreKit 2는 Swift async/await 전용이라 K/N 직접 호출 불가 → Swift 브릿지 또는 이 StoreKit 1.)
 *
 * ⚠️ `object`가 아니라 `class` + [storeKitBilling] lazy 싱글턴이어야 한다 — Kotlin/Native는 Obj-C 클래스를
 * 상속한 object 선언의 코드 생성을 지원하지 않아 프레임워크 **링크 단계에서** 컴파일러가 죽는다
 * ("Allocation of Obj-C class ... should have been lowered"). compileKotlinIos*는 통과하므로 링크까지
 * 돌려봐야 드러난다.
 */
private const val ERROR_PAYMENT_CANCELLED = 2L  // SKErrorPaymentCancelled

/** 앱 전역 단일 옵저버. `object`로 두면 안 된다 — 아래 클래스 주석 참고. */
private val storeKitBilling: StoreKitBilling by lazy { StoreKitBilling() }

private class StoreKitBilling : NSObject(), SKPaymentTransactionObserverProtocol, SKProductsRequestDelegateProtocol {
    private var callback: ((BillingResult, String?) -> Unit)? = null
    private var productsRequest: SKProductsRequest? = null
    private var observerAdded = false

    fun purchase(productId: String, onResult: (BillingResult, String?) -> Unit) {
        if (!SKPaymentQueue.canMakePayments()) { onResult(BillingResult.FAILURE, null); return }
        if (callback != null) { onResult(BillingResult.FAILURE, null); return }  // 이미 진행 중
        callback = onResult
        if (!observerAdded) {
            SKPaymentQueue.defaultQueue().addTransactionObserver(this)
            observerAdded = true
        }
        val request = SKProductsRequest(productIdentifiers = setOf(productId))
        request.delegate = this
        productsRequest = request
        request.start()  // 상품 조회 → didReceiveResponse에서 결제 시작
    }

    override fun productsRequest(request: SKProductsRequest, didReceiveResponse: SKProductsResponse) {
        productsRequest = null
        val product = didReceiveResponse.products.firstOrNull() as? SKProduct
        if (product == null) { finish(BillingResult.FAILURE, null); return }  // 미등록 상품
        SKPaymentQueue.defaultQueue().addPayment(SKPayment.paymentWithProduct(product))
    }

    override fun request(request: SKRequest, didFailWithError: NSError) {
        productsRequest = null
        finish(BillingResult.FAILURE, null)  // 상품 조회 실패
    }

    override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
        updatedTransactions.forEach { any ->
            val tx = any as? SKPaymentTransaction ?: return@forEach
            when (tx.transactionState) {
                SKPaymentTransactionState.SKPaymentTransactionStatePurchased,
                SKPaymentTransactionState.SKPaymentTransactionStateRestored -> {
                    queue.finishTransaction(tx)  // ⚠️ 이상적으론 서버 영수증 검증 후 finish
                    // StoreKit 1 폴백은 서버 검증용 증빙을 만들지 않는다(dev 전용).
                    // 프로덕션은 StoreKit 2 브릿지(IosBillingRegistry.impl)가 JWS를 제공한다.
                    finish(BillingResult.SUCCESS, null)
                }
                SKPaymentTransactionState.SKPaymentTransactionStateFailed -> {
                    val cancelled = tx.error?.code == ERROR_PAYMENT_CANCELLED
                    queue.finishTransaction(tx)
                    finish(if (cancelled) BillingResult.CANCELLED else BillingResult.FAILURE, null)
                }
                else -> Unit  // Purchasing/Deferred — 진행 중, 대기
            }
        }
    }

    private fun finish(result: BillingResult, token: String?) {
        val cb = callback
        callback = null
        cb?.invoke(result, token)
    }
}

@Composable
actual fun rememberSubscriptionBilling(): (String, String, (BillingResult, PurchaseProof?) -> Unit) -> Unit = remember {
    { productId, _, onResult ->
        // 성공 시 token을 PurchaseProof로 감싸 서버 검증에 전달.
        val handle: (BillingResult, String?) -> Unit = { result, token ->
            val proof = if (result == BillingResult.SUCCESS && token != null) {
                PurchaseProof(platform = "APP_STORE", productId = productId, token = token)
            } else {
                null
            }
            onResult(result, proof)
        }
        val swift = IosBillingRegistry.impl
        if (swift != null) swift.purchase(productId, handle) else storeKitBilling.purchase(productId, handle)
    }
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

@OptIn(ExperimentalForeignApi::class)
actual suspend fun compressImage(bytes: ByteArray, maxDimension: Int, quality: Int): ByteArray =
    withContext(Dispatchers.Default) {
        if (bytes.isEmpty()) return@withContext bytes
        val data = bytes.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), bytes.size.convert())
        }
        val image = UIImage.imageWithData(data) ?: return@withContext bytes  // 이미지 아님
        val w = image.size.useContents { width }
        val h = image.size.useContents { height }
        if (w <= 0.0 || h <= 0.0) return@withContext bytes
        val scale = minOf(maxDimension / w, maxDimension / h, 1.0)
        if (scale >= 1.0 && bytes.size <= COMPRESS_SKIP_BYTES) return@withContext bytes  // 이미 작음
        // scale 인자 1.0 고정 — 0.0(기기 스케일)이면 레티나에서 2~3배 픽셀로 커진다.
        UIGraphicsBeginImageContextWithOptions(CGSizeMake(w * scale, h * scale), true, 1.0)
        UIColor.whiteColor.setFill()  // 알파(PNG 투명)는 흰 배경으로
        UIRectFill(CGRectMake(0.0, 0.0, w * scale, h * scale))
        image.drawInRect(CGRectMake(0.0, 0.0, w * scale, h * scale))  // EXIF 방향 반영해 업라이트로
        val resized = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        val jpeg = (resized ?: image).let { UIImageJPEGRepresentation(it, quality / 100.0) }
            ?: return@withContext bytes
        val out = jpeg.toByteArray()
        if (out.size < bytes.size || scale < 1.0) out else bytes
    }
