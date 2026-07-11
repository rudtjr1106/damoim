package com.damoim.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * 플랫폼 기능 브릿지 (expect/actual).
 *
 * - 사진 갤러리는 Peekaboo(공통)로 처리하므로 여기 없음.
 * - 카메라/문서 피커/공유 시트/시스템 뒤로가기를 각 플랫폼 구현으로 연결한다.
 *   iOS는 아직 스텁(카메라·문서는 null 반환 → 호출부가 준비 중 안내).
 */

/** 카메라 촬영 런처. [launch] 호출 → 촬영 결과(취소/미지원이면 null). */
interface CameraLauncher {
    fun launch()
}

@Composable
expect fun rememberCameraLauncher(onResult: (ImageBitmap?) -> Unit): CameraLauncher

/** 문서 피커 결과. */
data class PickedDocument(val name: String, val sizeLabel: String)

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
 * 구독 인앱 결제 런처. 반환 람다를 호출하면(가격 라벨 전달) 네이티브 인앱 결제 UI가 뜨고
 * 결과를 [onResult]로 돌려준다. 데모는 모의 결제(성공/실패/취소 선택), 배포 시 Play Billing/StoreKit으로 교체.
 */
@Composable
expect fun rememberSubscriptionBilling(): (priceLabel: String, onResult: (BillingResult) -> Unit) -> Unit

/** 이메일 작성기 열기(수신자·제목·본문). Android=ACTION_SENDTO(mailto), iOS=mailto URL. */
@Composable
expect fun rememberEmailComposer(): (address: String, subject: String, body: String) -> Unit

/** 시스템 뒤로가기 처리. [enabled]일 때 뒤로가기를 가로채 [onBack]을 실행한다(iOS는 no-op). */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
