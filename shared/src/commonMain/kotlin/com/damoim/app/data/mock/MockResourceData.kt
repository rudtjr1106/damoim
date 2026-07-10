package com.damoim.app.data.mock

import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.domain.model.ResourceFolder
import com.damoim.app.domain.model.ResourceVisibility

/**
 * 자료실(D) 시드 데이터 + 파일 크기 유틸.
 *
 * 시드 12건의 크기 합계는 약 1.18GB로, 디자인 67의 저장공간 바(1.2GB / 5GB · 24%)와
 * "전체 자료 12"를 실제 데이터로 재현한다. 상단 5건이 디자인에 그려진 목록 그대로다.
 */
internal object MockResourceData {

    private const val KB = 1024L
    private const val MB = 1024L * 1024
    private const val GB = 1024L * 1024 * 1024

    const val QUOTA_BYTES = 5 * GB
    const val QUOTA_LABEL = "5GB"

    /** 표시 라벨("1.4MB") → 바이트. 문서 피커가 라벨만 넘겨주므로 역파싱해 집계에 쓴다. */
    fun parseSizeToBytes(label: String): Long {
        val number = label.takeWhile { it.isDigit() || it == '.' }
        if (number.isEmpty()) return 0L
        val value = number.toDoubleOrNull() ?: return 0L
        val unit = label.drop(number.length).trim().uppercase()
        val multiplier = when (unit) {
            "GB" -> GB
            "MB" -> MB
            "KB" -> KB
            else -> 1L
        }
        return (value * multiplier).toLong()
    }

    /** 저장공간 집계 라벨. GB만 소수점 한 자리(Kotlin common엔 String.format이 없다). */
    fun formatStorageLabel(bytes: Long): String = when {
        bytes >= GB -> {
            val tenths = (bytes * 10 + GB / 2) / GB
            "${tenths / 10}.${tenths % 10}GB"
        }
        bytes >= MB -> "${(bytes + MB / 2) / MB}MB"
        bytes >= KB -> "${(bytes + KB / 2) / KB}KB"
        else -> "0KB"
    }

    private const val LEADER_ID = 2001L      // 김민준(동아리장)
    private const val YUJIN_ID = 2004L       // 최유진
    private const val DOYUN_ID = 2007L       // 강도윤

    fun seedResources(): List<ResourceFile> = listOf(
        // ── 디자인 67에 그려진 5건 (최신순) ──
        ResourceFile(
            id = 9001, title = "동아리 회칙 v3.2", fileName = "동아리 회칙 v3.2.pdf", ext = "PDF",
            description = "2025년 정기총회에서 개정된 최신 회칙입니다. 회비 규정과 임원 선출 절차가 일부 변경되었으니 신입 부원은 꼭 확인해주세요.",
            folder = ResourceFolder.DOCS, sizeLabel = "1.4MB", sizeBytes = 1_468_006,
            uploaderId = LEADER_ID, uploaderName = "김민준", uploaderIsLeader = true,
            uploadedLabel = "3일 전", downloadCount = 28, pageCount = 12, createdAt = 120,
        ),
        ResourceFile(
            id = 9002, title = "2025 상반기 회계내역", fileName = "2025 상반기 회계내역.xlsx", ext = "XLSX",
            folder = ResourceFolder.ACCOUNTING, sizeLabel = "88KB", sizeBytes = 90_112,
            uploaderId = DOYUN_ID, uploaderName = "강도윤",
            uploadedLabel = "1주 전", downloadCount = 15, createdAt = 110,
        ),
        ResourceFile(
            id = 9003, title = "신입 OT 발표자료", fileName = "신입 OT 발표자료.pptx", ext = "PPTX",
            folder = ResourceFolder.PRESENTATION, sizeLabel = "6.2MB", sizeBytes = 6_501_171,
            uploaderId = YUJIN_ID, uploaderName = "최유진",
            uploadedLabel = "2주 전", downloadCount = 41, createdAt = 100,
        ),
        ResourceFile(
            id = 9004, title = "동아리 소개서 최종본", fileName = "동아리 소개서 최종본.hwp", ext = "HWP",
            folder = ResourceFolder.DOCS, sizeLabel = "240KB", sizeBytes = 245_760,
            uploaderId = LEADER_ID, uploaderName = "김민준", uploaderIsLeader = true,
            uploadedLabel = "3주 전", downloadCount = 33, createdAt = 90,
        ),
        ResourceFile(
            id = 9005, title = "2024 MT 사진 모음", fileName = "2024 MT 사진 모음.zip", ext = "ZIP",
            folder = ResourceFolder.PHOTOS, sizeLabel = "84MB", sizeBytes = 88_080_384,
            uploaderId = YUJIN_ID, uploaderName = "최유진",
            uploadedLabel = "1개월 전", downloadCount = 57, createdAt = 80,
        ),
        // ── 스크롤 아래(전체 12건·저장공간 1.2GB를 만드는 나머지) ──
        ResourceFile(
            id = 9006, title = "2024 정기공연 사진 원본", fileName = "2024 정기공연 사진 원본.zip", ext = "ZIP",
            folder = ResourceFolder.PHOTOS, sizeLabel = "620MB", sizeBytes = 650_117_120,
            uploaderId = YUJIN_ID, uploaderName = "최유진",
            uploadedLabel = "1개월 전", downloadCount = 19, createdAt = 70,
        ),
        ResourceFile(
            id = 9007, title = "2024 MT 영상 클립", fileName = "2024 MT 영상 클립.zip", ext = "ZIP",
            folder = ResourceFolder.PHOTOS, sizeLabel = "480MB", sizeBytes = 503_316_480,
            uploaderId = YUJIN_ID, uploaderName = "최유진",
            uploadedLabel = "2개월 전", downloadCount = 12, createdAt = 60,
        ),
        ResourceFile(
            id = 9008, title = "2024 하반기 회계내역", fileName = "2024 하반기 회계내역.xlsx", ext = "XLSX",
            folder = ResourceFolder.ACCOUNTING, sizeLabel = "76KB", sizeBytes = 77_824,
            uploaderId = DOYUN_ID, uploaderName = "강도윤",
            uploadedLabel = "2개월 전", downloadCount = 9, createdAt = 50,
        ),
        ResourceFile(
            id = 9009, title = "동아리 소개 PPT (2024)", fileName = "동아리 소개 PPT (2024).pptx", ext = "PPTX",
            folder = ResourceFolder.PRESENTATION, sizeLabel = "5.1MB", sizeBytes = 5_347_737,
            uploaderId = LEADER_ID, uploaderName = "김민준", uploaderIsLeader = true,
            uploadedLabel = "3개월 전", downloadCount = 24, createdAt = 40,
        ),
        ResourceFile(
            id = 9010, title = "회칙 v3.1 (구버전)", fileName = "회칙 v3.1.pdf", ext = "PDF",
            folder = ResourceFolder.DOCS, sizeLabel = "1.3MB", sizeBytes = 1_363_148,
            uploaderId = LEADER_ID, uploaderName = "김민준", uploaderIsLeader = true,
            uploadedLabel = "3개월 전", downloadCount = 6, pageCount = 11, createdAt = 30,
        ),
        ResourceFile(
            id = 9011, title = "부원 명단 양식", fileName = "부원 명단 양식.hwp", ext = "HWP",
            folder = ResourceFolder.DOCS, sizeLabel = "56KB", sizeBytes = 57_344,
            uploaderId = DOYUN_ID, uploaderName = "강도윤",
            uploadedLabel = "4개월 전", downloadCount = 31, createdAt = 20,
        ),
        ResourceFile(
            id = 9012, title = "2024 신입 모집 포스터", fileName = "2024 신입 모집 포스터.pdf", ext = "PDF",
            folder = ResourceFolder.PRESENTATION, sizeLabel = "12MB", sizeBytes = 12_582_912,
            uploaderId = YUJIN_ID, uploaderName = "최유진",
            uploadedLabel = "5개월 전", downloadCount = 48, pageCount = 1,
            visibility = ResourceVisibility.ALL_MEMBERS, createdAt = 10,
        ),
    )
}
