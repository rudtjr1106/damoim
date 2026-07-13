package com.damoim.app.domain.model

/**
 * 게시판(C 그룹) 도메인 모델.
 *
 * 게시글은 카테고리(공지/자유/모집)에 따라 표시가 달라지고, 첨부·투표·모집정보를 선택적으로 갖는다.
 * 서버 도입 전까지 데이터는 전부 인메모리 Mock(`data/mock/MockStore.kt`)이 관리하며,
 * 좋아요/댓글/투표/신청 등 상호작용은 스토어 상태 변경으로 실제 동작한다.
 */

/** 게시글. 목록/상세 공용. 상세 전용 필드는 목록에선 비어 있을 수 있다. */
data class BoardPost(
    val id: Long,
    val category: BoardCategory,          // 공지/자유/모집 (Home.kt에 정의됨)
    val title: String,
    val content: String = "",             // 상세 본문(줄바꿈 포함)
    val preview: String = "",             // 목록 미리보기(한 줄)
    val authorId: Long = 0,               // 작성자 식별(내 글 여부 판정 — ⋯메뉴 분기)
    val authorName: String,
    val authorInitials: String,           // 아바타 이니셜 "민준"
    val authorGisu: String? = null,       // "24기"
    val timeLabel: String,                // "10분 전"
    val dateLabel: String? = null,        // 상세: "2025.06.01"
    val createdAt: Long = 0,              // 정렬용 단조 증가 값(서버 도입 시 타임스탬프로 교체)
    val viewCount: Int = 0,
    val likeCount: Int = 0,
    val likedByMe: Boolean = false,       // 내가 좋아요 눌렀는지(토글)
    val commentCount: Int = 0,
    val isPinned: Boolean = false,        // 필독
    val isAuthorLeader: Boolean = false,  // 작성자명 옆 왕관
    val hasThumbnail: Boolean = false,    // 목록 우측 이미지 썸네일 여부
    val thumbnailUrl: String? = null,     // 목록 썸네일 presigned URL(첫 이미지 첨부)
    val readRate: Int? = null,            // 공지 확인율(%) — 필독 공지에만
    val attachments: List<PostAttachment> = emptyList(),
    val poll: Poll? = null,               // 투표 게시글(35/36)
    val recruit: RecruitInfo? = null,     // 모집 게시글(13/39/84)
)

/** 게시글 첨부. 이미지/파일/링크(og 미리보기) 3종. */
sealed interface PostAttachment {
    /** 사진. [url]=presigned view URL, [storageKey]=수정 시 재참조용 S3 키, [localKey]=업로드 전 로컬 미리보기. */
    data class Image(val url: String? = null, val storageKey: String? = null, val localKey: String? = null) : PostAttachment
    /** 문서 파일. [url]=다운로드 URL, [storageKey]=수정 시 재참조용 S3 키. */
    data class FileDoc(val name: String, val size: String, val url: String? = null, val storageKey: String? = null) : PostAttachment
    /** 링크 미리보기(og). [url]=전체 URL(클릭 시 웹 이동), title/domain은 표시용. */
    data class Link(val title: String, val domain: String, val url: String = "") : PostAttachment
}

/**
 * 투표(35 작성 / 36 상세). 옵션별 실제 득표수를 갖고, 내 투표는 [myVotes]에 인덱스로 담긴다.
 * 비율(%)은 득표수에서 파생 — 투표/재투표 시 스토어가 카운트를 갱신한다.
 */
data class Poll(
    val options: List<PollOption>,
    val anonymous: Boolean,               // 익명 투표
    val multiSelect: Boolean,             // 복수 선택 허용
    val deadlineLabel: String,            // "마감 D-2"
    val deadlineEpochMillis: Long? = null, // 실제 마감 순간(편집 프리필용, 서버가 내리면 채워짐)
    val myVotes: Set<Int> = emptySet(),   // 내가 고른 옵션 인덱스(복수 선택이면 여러 개)
) {
    val totalVotes: Int get() = options.sumOf { it.votes }
    val hasVoted: Boolean get() = myVotes.isNotEmpty()
    fun percentOf(index: Int): Int =
        if (totalVotes == 0) 0 else options[index].votes * 100 / totalVotes
}

/** 투표 항목(라벨 + 득표수). */
data class PollOption(val label: String, val votes: Int = 0)

/** 모집 정보(13 게시판 / 39 작성 / 84 상세). */
data class RecruitInfo(
    val status: RecruitStatus,
    val dday: String? = null,             // "D-7" (모집중일 때)
    val current: Int,                     // 현재 인원
    val capacity: Int,                    // 정원
    val deadlineLabel: String? = null,    // "6.17 (화) 자정"
    val deadlineEpochMillis: Long? = null, // 실제 마감 순간(편집 프리필용)
    val method: String? = null,           // "선착순"
    val applicants: List<RecruitApplicant> = emptyList(),
    val appliedByMe: Boolean = false,     // 내가 신청했는지
) {
    val remaining: Int get() = (capacity - current).coerceAtLeast(0)
    val percent: Int get() = if (capacity == 0) 0 else (current * 100 / capacity).coerceIn(0, 100)
}

enum class RecruitStatus { OPEN, CLOSED }  // 모집중 / 마감

/** 모집 신청자(84 아바타 스택). colorIndex는 아바타 배경색 변형용(0~2). */
data class RecruitApplicant(val initials: String, val colorIndex: Int = 0)

/** 신고 사유(82). 라벨은 DamoimStrings.reportReasonLabel로 매핑. */
enum class ReportReason { SPAM, ABUSE, SEXUAL, FRAUD, PRIVACY, ETC }

/** 댓글(14/36/84 상세). isReply면 들여쓰기, isAuthor면 '작성자' 뱃지, parentId는 답글 대상. */
data class Comment(
    val id: Long,
    val authorName: String,
    val authorInitials: String,
    val timeLabel: String,
    val content: String,
    val isReply: Boolean = false,
    val isAuthor: Boolean = false,
    val parentId: Long? = null,
)

/** 게시글 + 댓글을 함께 담는 상세 조회 결과. */
data class PostDetail(
    val post: BoardPost,
    val comments: List<Comment>,
)

// ── 작성(15/34/35/39/70) 제출용 초안 ──

/** 게시글 작성/수정 초안. 화면 상태를 모아 리포지토리에 제출한다(리포지토리가 이미지/문서를 S3에 업로드). */
data class PostDraft(
    val category: BoardCategory,
    val title: String,
    val content: String,
    val images: List<DraftImage> = emptyList(),
    val docs: List<DraftDocFile> = emptyList(),
    val links: List<DraftLink> = emptyList(),
    val poll: PollDraft? = null,
    val recruit: RecruitDraft? = null,
    val pinned: Boolean = false,          // 공지 필독 지정(53 시트)
)

/**
 * 작성 중 이미지 첨부. [bytes]가 있으면 제출 시 S3에 업로드(신규 선택/촬영),
 * [url]만 있으면 이미 서버에 있는 이미지(수정 프리필). [localKey]는 업로드 전 로컬 미리보기(ImageStore).
 */
data class DraftImage(
    val bytes: ByteArray? = null,
    val contentType: String? = null,
    val url: String? = null,
    val storageKey: String? = null,   // 기존(이미 업로드된) 이미지 — 수정 시 재참조
    val localKey: String? = null,
)

/** 작성 중 문서 첨부. [bytes]가 있으면 업로드, [url]만 있으면 기존 파일(수정 프리필). */
data class DraftDocFile(
    val name: String,
    val sizeLabel: String,
    val bytes: ByteArray? = null,
    val contentType: String? = null,
    val url: String? = null,
    val storageKey: String? = null,   // 기존(이미 업로드된) 문서 — 수정 시 재참조
)

/** 작성 중 링크 첨부. [url]=전체 URL(웹 이동), title/domain은 표시용. */
data class DraftLink(val url: String, val title: String, val domain: String)

/** 투표 초안(35). */
data class PollDraft(
    val options: List<String>,
    val anonymous: Boolean,
    val multiSelect: Boolean,
    val deadlineLabel: String,
    val deadlineEpochMillis: Long? = null,   // 피커(86)에서 고른 실제 마감 순간(UTC epoch ms)
)

/** 모집 초안(39). 마감 라벨·D-day는 날짜 피커(86) 선택값에서 계산해 전달. */
data class RecruitDraft(
    val capacity: Int,
    val deadlineLabel: String,
    val dday: String?,
    val firstCome: Boolean,
    val deadlineEpochMillis: Long? = null,   // 피커(86)에서 고른 실제 마감 순간(UTC epoch ms)
)
