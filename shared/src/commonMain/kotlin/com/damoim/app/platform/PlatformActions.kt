package com.damoim.app.platform

import androidx.compose.runtime.Composable
import com.damoim.app.domain.model.PurchaseProof

/**
 * 플랫폼 기능 브릿지 (expect/actual).
 *
 * - 사진 갤러리는 Peekaboo(공통)로 처리하므로 여기 없음.
 * - 카메라/문서 피커/공유 시트/시스템 뒤로가기를 각 플랫폼 구현으로 연결한다.
 *   iOS는 아직 스텁(카메라·문서는 null 반환 → 호출부가 준비 중 안내).
 */

/** 카메라 촬영 런처. [launch] 호출 → 촬영 결과 JPEG 바이트(취소/미지원이면 null). */
interface CameraLauncher {
    fun launch()
}

/** 촬영 결과는 JPEG 바이트로 돌려준다 — S3 업로드(첨부)에 그대로 쓰고, 미리보기는 디코드해 표시. */
@Composable
expect fun rememberCameraLauncher(onResult: (ByteArray?) -> Unit): CameraLauncher

/**
 * 문서 피커 결과. [bytes]는 실제 파일 내용(presigned PUT용), [contentType]은 MIME(예: "application/pdf").
 * 취소/미지원/상한초과면 이 객체가 만들어지지 않고 onResult(null)이 온다.
 */
data class PickedDocument(
    val name: String,
    val sizeLabel: String,
    val bytes: ByteArray,
    val contentType: String? = null,
)

/** 문서(PDF 등) 피커 런처. [launch] 호출 → 선택 결과(취소/미지원이면 null). */
interface DocumentPickerLauncher {
    fun launch()
}

@Composable
expect fun rememberDocumentPickerLauncher(onResult: (PickedDocument?) -> Unit): DocumentPickerLauncher

/** 시스템 공유 시트로 텍스트 공유. 반환된 람다를 호출하면 공유 시트가 뜬다. */
@Composable
expect fun rememberShareText(): (String) -> Unit

/** 인앱 결제 결과(G 그룹 구독). */
enum class BillingResult { SUCCESS, FAILURE, CANCELLED }

/**
 * 구독 인앱 결제 런처. 반환 람다를 호출하면([productId]로 스토어 상품 지정) 네이티브 결제가 시작되고
 * 결과를 [onResult]로 돌려준다(성공 시 서버 검증용 [PurchaseProof] 동봉).
 * iOS=StoreKit(StoreKit2 브릿지 우선, 없으면 StoreKit1), Android=Play Billing(현재 모의).
 * [priceLabel]은 표시용(스토어가 실제 가격을 보여주므로 iOS에선 미사용).
 */
@Composable
expect fun rememberSubscriptionBilling(): (productId: String, priceLabel: String, onResult: (BillingResult, PurchaseProof?) -> Unit) -> Unit

/** 이메일 작성기 열기(수신자·제목·본문). Android=ACTION_SENDTO(mailto), iOS=mailto URL. */
@Composable
expect fun rememberEmailComposer(): (address: String, subject: String, body: String) -> Unit

/** 시스템 뒤로가기 처리. [enabled]일 때 뒤로가기를 가로채 [onBack]을 실행한다(iOS는 no-op). */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)

/**
 * 업로드 전 이미지 최적화 — 최대 변을 [maxDimension]px로 축소하고 JPEG(q=[quality])로 재압축한다.
 * 이미 충분히 작으면(치수·용량 모두) 원본 인스턴스를 그대로 반환 — 호출부는 `결과 === 원본`으로
 * contentType 유지 여부를 판단한다(재인코딩됐으면 "image/jpeg"). 디코드 실패(비이미지·손상)나
 * 재압축이 오히려 커지는 경우도 원본 반환(업로드는 계속 진행).
 */
expect suspend fun compressImage(bytes: ByteArray, maxDimension: Int = 1280, quality: Int = 85): ByteArray

/** [compressImage]가 재인코딩을 생략하는 용량 하한 — 이보다 작고 치수도 상한 이내면 그대로 둔다. */
const val COMPRESS_SKIP_BYTES: Int = 300 * 1024
