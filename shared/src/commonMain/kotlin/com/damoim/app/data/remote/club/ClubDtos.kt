package com.damoim.app.data.remote.club

import com.damoim.app.domain.model.AlertKind
import com.damoim.app.domain.model.ApplicantsBoard
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.BoardPreview
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.model.ClubMembership
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.Cohort
import com.damoim.app.domain.model.HomeAlert
import com.damoim.app.domain.model.HomeStat
import com.damoim.app.domain.model.HomeSummary
import com.damoim.app.domain.model.JoinApplicant
import com.damoim.app.domain.model.JoinRequestResult
import com.damoim.app.domain.model.JoinStatus
import com.damoim.app.domain.model.Member
import com.damoim.app.domain.model.MemberDetail
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.domain.model.MemberStatus
import com.damoim.app.domain.model.ProcessedApplicant
import com.damoim.app.domain.model.UpcomingSchedule
import kotlinx.serialization.Serializable

/**
 * B(동아리/홈/알림) + E(회원/기수/멀티동아리) 그룹 DTO. 서버 club/·member/ DTO와 JSON 계약 1:1.
 * 파생 라벨(joinedLabel·timeAgo·dday 등)은 서버가 계산해 내려주므로 그대로 사용한다.
 */

// ── 요청 ──
@Serializable
data class CreateClubRequestDto(val name: String, val category: String, val intro: String = "")

@Serializable
data class JoinCodeRequestDto(val code: String)

@Serializable
data class DecideRequestDto(val approve: Boolean, val rejectionReason: String? = null)

@Serializable
data class SwitchClubRequestDto(val clubId: Long)

@Serializable
data class CohortCreateRequestDto(val short: String, val label: String = "")

@Serializable
data class CohortRenameRequestDto(val short: String, val label: String = "")

@Serializable
data class ChangeCohortRequestDto(val cohortId: Long)

@Serializable
data class ChangeRoleRequestDto(val role: String)

// ── 응답 ──
@Serializable
data class ClubResponseDto(
    val id: Long,
    val name: String,
    val category: String,
    val description: String = "",
    val joinCode: String? = null,
    val joinCodeActive: Boolean = false,
    val memberCount: Int = 0,
    val emblemColor: Long = 0xFF2F6DD3,
    val imageUrl: String? = null,
)

@Serializable
data class JoinCodeResponseDto(val joinCode: String? = null, val joinCodeActive: Boolean = false)

// ── 동아리 정보 수정(08) ──
@Serializable
data class ClubImageUploadRequestDto(val fileName: String? = null, val contentType: String? = null, val sizeBytes: Long)

@Serializable
data class ClubImageUploadResponseDto(val uploadUrl: String, val storageKey: String, val expiresInSeconds: Long = 0)

@Serializable
data class UpdateClubRequestDto(val name: String? = null, val intro: String? = null, val imageKey: String? = null)

/** 44 동아리별 프로필 수정 — 표시 이름 오버라이드(null/빈값=해제). */
@Serializable
data class UpdateClubProfileRequestDto(val displayName: String? = null)

@Serializable
data class CohortResponseDto(val id: Long, val label: String, val short: String, val memberCount: Int = 0)

@Serializable
data class ClubMembershipResponseDto(val club: ClubResponseDto, val role: String)

@Serializable
data class ClubSummaryDto(
    val id: Long,
    val name: String,
    val category: String,
    val emblemColor: Long = 0xFF2F6DD3,
    val imageUrl: String? = null,
)

@Serializable
data class JoinResultResponseDto(
    val club: ClubSummaryDto,
    val status: String,
    val rejectionReason: String? = null,
)

@Serializable
data class HomeSummaryResponseDto(
    val role: String,
    val clubName: String,
    val memberName: String,
    val stats: List<HomeStatDto> = emptyList(),
    val alert: HomeAlertDto? = null,
    val schedules: List<UpcomingScheduleDto> = emptyList(),
    val boardPreviews: List<BoardPreviewDto> = emptyList(),
    val hasUnreadNotification: Boolean = false,
)

@Serializable
data class HomeStatDto(val value: String, val label: String)

@Serializable
data class HomeAlertDto(val title: String, val subtitle: String, val kind: String, val badge: String? = null)

@Serializable
data class UpcomingScheduleDto(
    val id: Long,
    val dday: String,
    val date: String,
    val title: String,
    val subtitle: String,
    val primary: Boolean = false,
)

@Serializable
data class BoardPreviewDto(val id: Long = 0, val category: String, val title: String, val commentCount: Int = 0, val isPinned: Boolean = false)

@Serializable
data class ApplicantsBoardResponseDto(
    val pending: List<ApplicantResponseDto> = emptyList(),
    val processed: List<ProcessedApplicantResponseDto> = emptyList(),
)

@Serializable
data class ApplicantResponseDto(
    val id: Long,
    val name: String,
    val initial: String,
    val desiredGisu: String? = null,
    val appliedDate: String,
    val timeAgo: String,
    val message: String? = null,
    val imageUrl: String? = null,
)

@Serializable
data class ProcessedApplicantResponseDto(
    val applicant: ApplicantResponseDto,
    val approved: Boolean,
    val decidedLabel: String,
)

@Serializable
data class MemberResponseDto(
    val id: Long,
    val name: String,
    val initials: String,
    val cohortId: Long = 0,
    val role: String,
    val status: String,
    val email: String = "",
    val joinedLabel: String = "",
    val isMe: Boolean = false,
    val profileImageUrl: String? = null,
    // /me 응답에만 채워진다 — 요청자 본인의 세분 권한(운영진 기능 게이팅용).
    val permissions: List<String> = emptyList(),
)

@Serializable
data class MemberDetailResponseDto(
    val member: MemberResponseDto,
    val cohortLabel: String = "",
    val postCount: Int = 0,
    val eventCount: Int = 0,
    val lastActiveLabel: String = "",
)

// ── 매퍼 ──
internal fun clubRole(s: String): ClubRole = if (s == "LEADER") ClubRole.LEADER else ClubRole.MEMBER

internal fun memberRole(s: String): MemberRole =
    runCatching { MemberRole.valueOf(s) }.getOrDefault(MemberRole.MEMBER)

internal fun memberStatus(s: String): MemberStatus =
    runCatching { MemberStatus.valueOf(s) }.getOrDefault(MemberStatus.ACTIVE)

internal fun boardCategory(s: String): BoardCategory =
    runCatching { BoardCategory.valueOf(s) }.getOrDefault(BoardCategory.FREE)

internal fun joinStatus(s: String): JoinStatus =
    runCatching { JoinStatus.valueOf(s) }.getOrDefault(JoinStatus.PENDING)

internal fun alertKind(s: String): AlertKind =
    runCatching { AlertKind.valueOf(s) }.getOrDefault(AlertKind.JOIN_REQUEST)

internal fun ClubResponseDto.toDomain(): Club = Club(
    id = id,
    name = name,
    category = category,
    description = description,
    memberCount = memberCount,
    joinCode = joinCode ?: "",
    emblemColor = emblemColor,
    imageUrl = imageUrl,
)

internal fun ClubSummaryDto.toDomain(): Club = Club(
    id = id,
    name = name,
    category = category,
    description = "",
    memberCount = 0,
    joinCode = "",
    emblemColor = emblemColor,
    imageUrl = imageUrl,
)

internal fun CohortResponseDto.toDomain(): Cohort = Cohort(id = id, label = label, short = short, memberCount = memberCount)

internal fun ClubMembershipResponseDto.toDomain(): ClubMembership =
    ClubMembership(club = club.toDomain(), role = clubRole(role))

internal fun JoinResultResponseDto.toDomain(): JoinRequestResult = JoinRequestResult(
    club = club.toDomain(),
    status = joinStatus(status),
    rejectionReason = rejectionReason,
)

internal fun HomeSummaryResponseDto.toDomain(): HomeSummary = HomeSummary(
    role = clubRole(role),
    clubName = clubName,
    memberName = memberName,
    stats = stats.map { HomeStat(it.value, it.label) },
    alert = alert?.let { HomeAlert(it.title, it.subtitle, alertKind(it.kind), it.badge) },
    schedules = schedules.map {
        UpcomingSchedule(it.id, it.dday, it.date, it.title, it.subtitle, it.primary)
    },
    boardPreviews = boardPreviews.map { BoardPreview(it.id, boardCategory(it.category), it.title, it.commentCount, it.isPinned) },
    hasUnreadNotification = hasUnreadNotification,
)

internal fun ApplicantResponseDto.toDomain(): JoinApplicant = JoinApplicant(
    id = id,
    name = name,
    initial = initial,
    desiredGisu = desiredGisu ?: "",
    appliedDate = appliedDate,
    timeAgo = timeAgo,
    message = message,
    imageUrl = imageUrl,
)

internal fun ProcessedApplicantResponseDto.toDomain(): ProcessedApplicant = ProcessedApplicant(
    applicant = applicant.toDomain(),
    approved = approved,
    decidedLabel = decidedLabel,
)

internal fun ApplicantsBoardResponseDto.toDomain(): ApplicantsBoard = ApplicantsBoard(
    pending = pending.map { it.toDomain() },
    processed = processed.map { it.toDomain() },
)

internal fun MemberResponseDto.toDomain(): Member = Member(
    id = id,
    name = name,
    initials = initials,
    cohortId = cohortId,
    role = memberRole(role),
    status = memberStatus(status),
    joinedLabel = joinedLabel,
    isMe = isMe,
    profileImageUrl = profileImageUrl,
)

internal fun MemberDetailResponseDto.toDomain(): MemberDetail = MemberDetail(
    member = member.toDomain(),
    cohortLabel = cohortLabel,
    postCount = postCount,
    eventCount = eventCount,
    lastActiveLabel = lastActiveLabel,
)
