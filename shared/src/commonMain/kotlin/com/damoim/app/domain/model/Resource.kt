package com.damoim.app.domain.model

/**
 * 자료실 파일(67/68/69). 게시글 첨부([PostAttachment.FileDoc])와 달리 id·업로더·폴더·
 * 다운로드 수를 갖는 1급 엔티티다.
 *
 * [sizeLabel]은 표시용("1.4MB"), [sizeBytes]는 저장공간 집계용. 서버 도입 전까지
 * 업로드 시에는 라벨을 역파싱해 바이트를 얻는다(문서 피커가 라벨만 넘겨줌).
 */
data class ResourceFile(
    val id: Long,
    val title: String,
    val fileName: String,
    val ext: String,                 // 대문자 확장자 — 배지 라벨(PDF/XLSX/…)
    val description: String = "",
    val folder: ResourceFolder,
    val sizeLabel: String,
    val sizeBytes: Long,
    val uploaderId: Long,
    val uploaderName: String,
    val uploaderIsLeader: Boolean = false,
    val uploadedLabel: String,       // "3일 전" / 업로드 직후 "방금 전"
    val downloadCount: Int = 0,
    val visibility: ResourceVisibility = ResourceVisibility.ALL_MEMBERS,
    val cohortIds: List<Long> = emptyList(),   // COHORT_ONLY일 때 공개 대상 기수
    val pageCount: Int? = null,      // 68 "문서 미리보기 · 12쪽"
    val createdAt: Long,             // 정렬용 단조 증가 값
)

/** 자료 폴더(카테고리). 67 필터 칩 = 전체(null) + 아래 4종. */
enum class ResourceFolder { DOCS, ACCOUNTING, PRESENTATION, PHOTOS }

/** 69 공개 범위. */
enum class ResourceVisibility { ALL_MEMBERS, COHORT_ONLY }

/** 69 자료 업로드 입력값. */
data class ResourceDraft(
    val fileName: String,
    val sizeLabel: String,
    val title: String,
    val description: String,
    val folder: ResourceFolder,
    val visibility: ResourceVisibility,
    val cohortIds: List<Long> = emptyList(),
)
