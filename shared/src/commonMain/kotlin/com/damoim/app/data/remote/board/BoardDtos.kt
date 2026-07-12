package com.damoim.app.data.remote.board

import com.damoim.app.data.remote.core.AttachmentTypes
import com.damoim.app.data.remote.core.RecruitMethods
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.BoardPost
import com.damoim.app.domain.model.Comment
import com.damoim.app.domain.model.Poll
import com.damoim.app.domain.model.PollDraft
import com.damoim.app.domain.model.PollOption
import com.damoim.app.domain.model.PostAttachment
import com.damoim.app.domain.model.PostDetail
import com.damoim.app.domain.model.PostDraft
import com.damoim.app.domain.model.RecruitDraft
import com.damoim.app.domain.model.RecruitInfo
import com.damoim.app.domain.model.RecruitStatus
import kotlinx.serialization.Serializable

/**
 * C 게시판 그룹 DTO. 서버 board/BoardDtos와 JSON 계약 1:1.
 * 파생값(preview·timeLabel·deadlineLabel·dday·percent 등)은 서버가 계산 → 그대로 사용/클라 게터가 재파생.
 */

// ── 요청 ──
@Serializable
data class AttachmentInputDto(
    val type: String,
    val imageLabel: String? = null,
    val fileName: String? = null,
    val fileSizeBytes: Long? = null,
    val linkTitle: String? = null,
    val linkDomain: String? = null,
)

@Serializable
data class PollInputDto(
    val options: List<String>,
    val anonymous: Boolean = false,
    val multiSelect: Boolean = false,
    val deadline: String? = null, // ISO-8601 Instant. draft에 실제 Instant가 없어 현재 null 전송(알려진 한계).
)

@Serializable
data class RecruitInputDto(
    val capacity: Int,
    val deadline: String? = null,
    val method: String? = null,
)

@Serializable
data class CreatePostRequestDto(
    val category: String,
    val title: String,
    val content: String,
    val pinned: Boolean = false,
    val attachments: List<AttachmentInputDto> = emptyList(),
    val poll: PollInputDto? = null,
    val recruit: RecruitInputDto? = null,
)

@Serializable
data class UpdatePostRequestDto(
    val category: String,
    val title: String,
    val content: String,
)

@Serializable
data class VotePollRequestDto(val optionIndex: Int)

@Serializable
data class AddCommentRequestDto(val content: String, val parentId: Long? = null)

@Serializable
data class DraftRequestDto(
    val category: String? = null,
    val title: String? = null,
    val content: String? = null,
    val pinned: Boolean = false,
    val attachments: List<AttachmentInputDto> = emptyList(),
    val poll: PollInputDto? = null,
    val recruit: RecruitInputDto? = null,
)

// ── 응답 ──
@Serializable
data class PostSummaryResponseDto(
    val id: Long,
    val category: String,
    val title: String,
    val preview: String = "",
    val authorName: String = "",
    val authorInitials: String = "",
    val authorGisu: String? = null,
    val timeLabel: String = "",
    val likeCount: Int = 0,
    val likedByMe: Boolean = false,
    val commentCount: Int = 0,
    val isPinned: Boolean = false,
    val isAuthorLeader: Boolean = false,
    val hasThumbnail: Boolean = false,
    val readRate: Int? = null,
)

@Serializable
data class AttachmentResponseDto(
    val type: String,
    val imageLabel: String? = null,
    val fileName: String? = null,
    val fileSize: String? = null,
    val linkTitle: String? = null,
    val linkDomain: String? = null,
)

@Serializable
data class PollOptionResponseDto(val index: Int, val label: String, val votes: Int = 0, val percent: Int = 0)

@Serializable
data class PollResponseDto(
    val anonymous: Boolean = false,
    val multiSelect: Boolean = false,
    val deadlineLabel: String? = null,
    val dday: String? = null,
    val totalVotes: Int = 0,
    val myVotes: List<Int> = emptyList(),
    val options: List<PollOptionResponseDto> = emptyList(),
)

@Serializable
data class RecruitResponseDto(
    val status: String,
    val capacity: Int = 0,
    val current: Int = 0,
    val remaining: Int = 0,
    val percent: Int = 0,
    val deadlineLabel: String? = null,
    val dday: String? = null,
    val method: String? = null,
    val appliedByMe: Boolean = false,
)

@Serializable
data class CommentResponseDto(
    val id: Long,
    val authorName: String = "",
    val authorInitials: String = "",
    val timeLabel: String = "",
    val content: String = "",
    val isReply: Boolean = false,
    val isAuthor: Boolean = false,
    val parentId: Long? = null,
)

@Serializable
data class PostDetailResponseDto(
    val id: Long,
    val category: String,
    val title: String,
    val content: String = "",
    val authorName: String = "",
    val authorInitials: String = "",
    val authorGisu: String? = null,
    val timeLabel: String = "",
    val dateLabel: String = "",
    val viewCount: Int = 0,
    val likeCount: Int = 0,
    val likedByMe: Boolean = false,
    val commentCount: Int = 0,
    val isPinned: Boolean = false,
    val isAuthorLeader: Boolean = false,
    val isMine: Boolean = false,
    val readRate: Int? = null,
    val attachments: List<AttachmentResponseDto> = emptyList(),
    val poll: PollResponseDto? = null,
    val recruit: RecruitResponseDto? = null,
    val comments: List<CommentResponseDto> = emptyList(),
)

@Serializable
data class LikeResponseDto(val liked: Boolean = false, val likeCount: Int = 0)

@Serializable
data class PinResponseDto(val isPinned: Boolean = false)

@Serializable
data class BoardHomeResponseDto(
    val pinned: List<PostSummaryResponseDto> = emptyList(),
    val feed: List<PostSummaryResponseDto> = emptyList(),
)

@Serializable
data class SearchResultResponseDto(
    val query: String = "",
    val posts: List<PostSummaryResponseDto> = emptyList(),
)

@Serializable
data class SearchSuggestionsResponseDto(
    val recent: List<String> = emptyList(),
    val recommended: List<String> = emptyList(),
)

@Serializable
data class DraftResponseDto(
    val category: String = "FREE",
    val title: String = "",
    val content: String = "",
    val pinned: Boolean = false,
    val attachments: List<AttachmentInputDto> = emptyList(),
    val poll: PollInputDto? = null,
    val recruit: RecruitInputDto? = null,
)

// ── 매퍼 ──
internal fun boardCategoryOf(s: String): BoardCategory =
    runCatching { BoardCategory.valueOf(s) }.getOrDefault(BoardCategory.FREE)

internal fun recruitStatusOf(s: String): RecruitStatus =
    runCatching { RecruitStatus.valueOf(s) }.getOrDefault(RecruitStatus.OPEN)

/** epoch ms → ISO-8601 Instant 문자열(서버 deadline 필드용). */
internal fun Long.toIsoInstant(): String = kotlin.time.Instant.fromEpochMilliseconds(this).toString()

/** ISO-8601 Instant → epoch ms(임시저장 복원용). 파싱 실패 시 null. */
internal fun parseInstantToMillis(iso: String?): Long? =
    iso?.let { runCatching { kotlin.time.Instant.parse(it).toEpochMilliseconds() }.getOrNull() }

internal fun formatSizeLabel(bytes: Long): String = when {
    bytes >= 1_048_576L -> "${bytes / 1_048_576L}MB"
    bytes >= 1024L -> "${bytes / 1024L}KB"
    else -> "${bytes}B"
}

internal fun parseSizeToBytes(label: String): Long {
    val t = label.trim().uppercase()
    val num = t.takeWhile { it.isDigit() || it == '.' }.toDoubleOrNull() ?: return 1L
    val bytes = when {
        t.endsWith("GB") -> num * 1_073_741_824.0
        t.endsWith("MB") -> num * 1_048_576.0
        t.endsWith("KB") -> num * 1024.0
        else -> num
    }
    return bytes.toLong().coerceAtLeast(1L)
}

/** 목록/홈 카드. [createdAt]은 서버 정렬 순서를 보존하기 위한 합성 내림차순 값. */
internal fun PostSummaryResponseDto.toDomain(orderKey: Long): BoardPost = BoardPost(
    id = id,
    category = boardCategoryOf(category),
    title = title,
    preview = preview,
    authorName = authorName,
    authorInitials = authorInitials,
    authorGisu = authorGisu,
    timeLabel = timeLabel,
    createdAt = orderKey,
    likeCount = likeCount,
    likedByMe = likedByMe,
    commentCount = commentCount,
    isPinned = isPinned,
    isAuthorLeader = isAuthorLeader,
    hasThumbnail = hasThumbnail,
    readRate = readRate,
)

internal fun List<PostSummaryResponseDto>.toDomainList(): List<BoardPost> =
    mapIndexed { index, dto -> dto.toDomain(orderKey = (size - index).toLong()) }

internal fun AttachmentResponseDto.toDomain(): PostAttachment = when (type) {
    AttachmentTypes.IMAGE -> PostAttachment.Image(label = imageLabel ?: "")
    AttachmentTypes.FILE_DOC -> PostAttachment.FileDoc(name = fileName ?: "", size = fileSize ?: "")
    else -> PostAttachment.Link(title = linkTitle ?: "", domain = linkDomain ?: "")
}

internal fun PollResponseDto.toDomain(): Poll = Poll(
    options = options.map { PollOption(label = it.label, votes = it.votes) },
    anonymous = anonymous,
    multiSelect = multiSelect,
    deadlineLabel = deadlineLabel ?: "",
    myVotes = myVotes.toSet(),
)

internal fun RecruitResponseDto.toDomain(): RecruitInfo = RecruitInfo(
    status = recruitStatusOf(status),
    dday = dday,
    current = current,
    capacity = capacity,
    deadlineLabel = deadlineLabel,
    method = method,
    applicants = emptyList(), // 서버가 아바타 스택 데이터를 내리지 않음(알려진 갭)
    appliedByMe = appliedByMe,
)

internal fun CommentResponseDto.toDomain(): Comment = Comment(
    id = id,
    authorName = authorName,
    authorInitials = authorInitials,
    timeLabel = timeLabel,
    content = content,
    isReply = isReply,
    isAuthor = isAuthor,
    parentId = parentId,
)

/** 상세 → PostDetail. authorId는 isMine + 세션 userId에서 파생(서버는 authorId 미전송). */
internal fun PostDetailResponseDto.toDomain(currentUserId: Long): PostDetail {
    val mappedAttachments = attachments.map { it.toDomain() }
    val post = BoardPost(
        id = id,
        category = boardCategoryOf(category),
        title = title,
        content = content,
        authorId = if (isMine) currentUserId else 0L,
        authorName = authorName,
        authorInitials = authorInitials,
        authorGisu = authorGisu,
        timeLabel = timeLabel,
        dateLabel = dateLabel,
        viewCount = viewCount,
        likeCount = likeCount,
        likedByMe = likedByMe,
        commentCount = commentCount,
        isPinned = isPinned,
        isAuthorLeader = isAuthorLeader,
        hasThumbnail = mappedAttachments.any { it is PostAttachment.Image },
        readRate = readRate,
        attachments = mappedAttachments,
        poll = poll?.toDomain(),
        recruit = recruit?.toDomain(),
    )
    return PostDetail(post = post, comments = comments.map { it.toDomain() })
}

internal fun BoardHomeResponseDto.toDomainHome(): com.damoim.app.domain.repository.BoardHomeData =
    com.damoim.app.domain.repository.BoardHomeData(
        pinned = pinned.toDomainList(),
        recent = feed.toDomainList(),
    )

internal fun DraftResponseDto.toDomain(): PostDraft = PostDraft(
    category = boardCategoryOf(category),
    title = title,
    content = content,
    pinned = pinned,
    photoLabels = attachments.filter { it.type == AttachmentTypes.IMAGE }.mapNotNull { it.imageLabel },
    docs = attachments.filter { it.type == AttachmentTypes.FILE_DOC }.map {
        PostAttachment.FileDoc(name = it.fileName ?: "", size = formatSizeLabel(it.fileSizeBytes ?: 0L))
    },
    link = attachments.firstOrNull { it.type == AttachmentTypes.LINK }
        ?.let { PostAttachment.Link(title = it.linkTitle ?: "", domain = it.linkDomain ?: "") },
    poll = poll?.let {
        PollDraft(
            options = it.options, anonymous = it.anonymous, multiSelect = it.multiSelect,
            deadlineLabel = "", deadlineEpochMillis = parseInstantToMillis(it.deadline),
        )
    },
    recruit = recruit?.let {
        RecruitDraft(
            capacity = it.capacity, deadlineLabel = "", dday = null,
            firstCome = it.method == RecruitMethods.FIRST_COME,
            deadlineEpochMillis = parseInstantToMillis(it.deadline),
        )
    },
)

/** PostDraft → 생성/저장 요청 바디. 첨부 순서: 이미지 → 문서 → 링크. */
internal fun PostDraft.toAttachmentInputs(): List<AttachmentInputDto> = buildList {
    photoLabels.forEach { add(AttachmentInputDto(type = AttachmentTypes.IMAGE, imageLabel = it)) }
    docs.forEach {
        add(AttachmentInputDto(type = AttachmentTypes.FILE_DOC, fileName = it.name, fileSizeBytes = parseSizeToBytes(it.size)))
    }
    link?.let { add(AttachmentInputDto(type = AttachmentTypes.LINK, linkTitle = it.title, linkDomain = it.domain)) }
}

internal fun PostDraft.toCreateRequest(): CreatePostRequestDto = CreatePostRequestDto(
    category = category.name,
    title = title,
    content = content,
    pinned = pinned,
    attachments = toAttachmentInputs(),
    poll = poll?.let {
        PollInputDto(
            options = it.options, anonymous = it.anonymous, multiSelect = it.multiSelect,
            deadline = it.deadlineEpochMillis?.toIsoInstant(),
        )
    },
    recruit = recruit?.let {
        RecruitInputDto(
            capacity = it.capacity,
            deadline = it.deadlineEpochMillis?.toIsoInstant(),
            method = if (it.firstCome) RecruitMethods.FIRST_COME else RecruitMethods.APPROVAL,
        )
    },
)

internal fun PostDraft.toUpdateRequest(): UpdatePostRequestDto =
    UpdatePostRequestDto(category = category.name, title = title, content = content)

internal fun PostDraft.toDraftRequest(): DraftRequestDto = DraftRequestDto(
    category = category.name,
    title = title,
    content = content,
    pinned = pinned,
    attachments = toAttachmentInputs(),
    poll = poll?.let {
        PollInputDto(
            options = it.options, anonymous = it.anonymous, multiSelect = it.multiSelect,
            deadline = it.deadlineEpochMillis?.toIsoInstant(),
        )
    },
    recruit = recruit?.let {
        RecruitInputDto(
            capacity = it.capacity,
            deadline = it.deadlineEpochMillis?.toIsoInstant(),
            method = if (it.firstCome) RecruitMethods.FIRST_COME else RecruitMethods.APPROVAL,
        )
    },
)
