package com.damoim.app.data.mock

import com.damoim.app.domain.model.AppNotification
import com.damoim.app.domain.model.AuthUser
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.BoardPost
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.model.Cohort
import com.damoim.app.domain.model.Comment
import com.damoim.app.domain.model.HomeAlert
import com.damoim.app.domain.model.AlertKind
import com.damoim.app.domain.model.HomeStat
import com.damoim.app.domain.model.HomeSummary
import com.damoim.app.domain.model.BoardPreview
import com.damoim.app.domain.model.UpcomingSchedule
import com.damoim.app.domain.model.JoinApplicant
import com.damoim.app.domain.model.Member
import com.damoim.app.domain.model.MemberDetail
import com.damoim.app.domain.model.MemberRole
import com.damoim.app.domain.model.MemberStatus
import com.damoim.app.domain.model.Poll
import com.damoim.app.domain.model.PollOption
import com.damoim.app.domain.model.PostAttachment
import com.damoim.app.domain.model.PostDetail
import com.damoim.app.domain.model.PostDraft
import com.damoim.app.domain.model.RecruitApplicant
import com.damoim.app.domain.model.RecruitInfo
import com.damoim.app.domain.model.RecruitStatus
import com.damoim.app.domain.model.ResourceDraft
import com.damoim.app.domain.model.ResourceFile
import com.damoim.app.domain.model.ResourceFolder
import com.damoim.app.domain.model.ApplicantStatus
import com.damoim.app.domain.model.ApplicationStatus
import com.damoim.app.domain.model.EventApplicant
import com.damoim.app.domain.model.EventInfo
import com.damoim.app.domain.model.EventStatus
import com.damoim.app.domain.model.MyApplication
import com.damoim.app.domain.model.QuestionAnswer
import com.damoim.app.domain.model.Schedule
import com.damoim.app.domain.model.ScheduleAccent
import com.damoim.app.domain.model.ScheduleDraft
import com.damoim.app.domain.model.ScheduleType
import com.damoim.app.domain.repository.BoardHomeData
import com.damoim.app.domain.repository.SearchFileHit
import com.damoim.app.domain.repository.SearchResults
import com.damoim.app.domain.repository.SearchScheduleHit
import com.damoim.app.domain.repository.SearchSuggestions
import com.damoim.app.domain.repository.StorageUsage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * 앱 전역 인메모리 상태 저장소(단일 소스). 서버·DB 도입 전까지 모든 상호작용
 * (로그인/프로필, 동아리 생성/가입, 게시글 CRUD, 댓글, 좋아요, 투표, 모집 신청,
 * 가입 승인, 알림 읽음, 코드 재발급, 최근 검색어)이 여기서 실제로 상태 변경된다.
 *
 * Mock 레포지토리들은 이 스토어에 위임만 하므로, 서버가 붙으면 레포지토리 구현만
 * Ktor 기반으로 교체하면 된다(화면·ViewModel 무변경).
 */
object MockStore {

    // ══════════ 인증/프로필 ══════════

    private val _user = MutableStateFlow(
        AuthUser(id = 1001, nickname = "", email = "seoyeon@kakao.com", profileImageUrl = null, needsProfileSetup = true),
    )
    val user: StateFlow<AuthUser> = _user.asStateFlow()

    /** 카카오 로그인(모의). 프로필 미설정이면 needsProfileSetup=true 유지. */
    fun login(): AuthUser = _user.value

    /** 실제 소셜(카카오) 로그인 결과 반영 — 닉네임/이메일/사진을 프리필하고 프로필 설정 단계로. */
    fun loginWithSocial(social: com.damoim.app.core.social.SocialUser): AuthUser {
        _user.update {
            it.copy(
                id = if (social.id != 0L) social.id else it.id,
                nickname = social.nickname.ifBlank { it.nickname },
                email = social.email ?: it.email,
                profileImageUrl = social.profileImageUrl ?: it.profileImageUrl,
            )
        }
        return _user.value
    }

    fun updateProfile(nickname: String, contact: String, profileImageUrl: String?): AuthUser {
        _user.update { it.copy(nickname = nickname, contact = contact, profileImageUrl = profileImageUrl ?: it.profileImageUrl, needsProfileSetup = false) }
        return _user.value
    }

    private val myName: String get() = _user.value.nickname.ifBlank { "나" }
    private fun initialsOf(name: String) = if (name.length >= 3) name.takeLast(2) else name

    // ══════════ 동아리 세션 ══════════

    data class ClubSession(val club: Club, val role: ClubRole)

    private val _session = MutableStateFlow<ClubSession?>(null)
    val session: StateFlow<ClubSession?> = _session.asStateFlow()

    val role: ClubRole? get() = _session.value?.role

    /** 33 동아리 전환 시트용 — 내가 속한 동아리들(현재 + 예시). */
    private val _joinedClubs = MutableStateFlow<List<com.damoim.app.domain.model.ClubMembership>>(emptyList())
    fun joinedClubsFlow(): Flow<List<com.damoim.app.domain.model.ClubMembership>> = _joinedClubs.asStateFlow()

    /**
     * 동아리별 가변 상태 스냅샷. 전환 시 현재 동아리를 이 번들로 보관했다가 되돌린다
     * → 전역 flow 구조를 바꾸지 않고 value만 통째로 교체해 전 화면이 자동 갱신된다.
     * (서버 도입 시 = activeClubId 기준 데이터 재조회로 교체)
     */
    private data class ClubBundle(
        val session: ClubSession,
        val posts: List<BoardPost>,
        val comments: Map<Long, List<Comment>>,
        val pending: List<JoinApplicant>,
        val processed: List<com.damoim.app.domain.model.ProcessedApplicant>,
        val notifications: List<AppNotification>,
        val resources: List<ResourceFile>,
        val cohorts: List<Cohort>,
        val members: List<Member>,
        val draft: PostDraft?,
        val schedules: List<Schedule>,
        val myApplications: List<MyApplication>,
        val scheduleDraft: ScheduleDraft?,
    )

    private val bundles = mutableMapOf<Long, ClubBundle>()

    private fun snapshotCurrent(): ClubBundle? {
        val s = _session.value ?: return null
        return ClubBundle(s, _posts.value, _comments.value, _pending.value, _processed.value, _notifications.value, _resources.value, _cohorts.value, _members.value, savedDraft, _schedules.value, _myApplications.value, scheduleDraft)
    }

    private fun applyBundle(b: ClubBundle) {
        _session.value = b.session
        _posts.value = b.posts
        _comments.value = b.comments
        _pending.value = b.pending
        _processed.value = b.processed
        _notifications.value = b.notifications
        _resources.value = b.resources
        _cohorts.value = b.cohorts
        _members.value = b.members
        savedDraft = b.draft   // 임시저장 글은 동아리별로 격리 — 전환 시 다른 동아리로 새지 않는다
        _schedules.value = b.schedules
        _myApplications.value = b.myApplications
        scheduleDraft = b.scheduleDraft
    }

    /** 코드 가입 완료(04 확인) 후 데모 동아리 입장. 이미 세션이 있으면(생성 경로) 무시. */
    fun enterClub(role: ClubRole) {
        if (_session.value != null) return
        _session.value = ClubSession(MockData.myClub, role)
        seedDemoClub()
    }

    /** 33 동아리 전환 — 현재 동아리를 번들로 보관하고 대상 동아리 데이터로 갈아탄다. */
    fun switchClub(clubId: Long) {
        val cur = _session.value ?: return
        if (cur.club.id == clubId) return
        snapshotCurrent()?.let { bundles[cur.club.id] = it }
        val target = bundles[clubId] ?: return
        applyBundle(target)
    }

    /** 60 탈퇴 / 33 새 참여·생성 / 로그아웃 — 세션과 모든 동아리 데이터를 비운다(→ Auth로 복귀). */
    fun leaveClub() {
        _session.value = null
        bundles.clear()
        _joinedClubs.value = emptyList()
        _posts.value = emptyList()
        _comments.value = emptyMap()
        _pending.value = emptyList()
        _processed.value = emptyList()
        _notifications.value = emptyList()
        _resources.value = emptyList()
        _cohorts.value = emptyList()
        _members.value = emptyList()
        savedDraft = null
        _schedules.value = emptyList()
        _myApplications.value = emptyList()
        scheduleDraft = null
    }

    /** 동아리 생성(07). 새 동아리는 게시판/신청자/알림이 빈 상태에서 시작한다. */
    fun createClub(name: String, intro: String, category: String): Club {
        val club = Club(
            id = nextId++, name = name, category = category, description = intro,
            memberCount = 1, joinCode = randomCode(),
        )
        _session.value = ClubSession(club, ClubRole.LEADER)
        _posts.value = emptyList()
        _comments.value = emptyMap()
        _pending.value = emptyList()
        _processed.value = emptyList()
        _resources.value = emptyList()
        _schedules.value = emptyList()
        _myApplications.value = emptyList()
        scheduleDraft = null
        _cohorts.value = listOf(Cohort(1, "1기", "1기", 1))   // 새 동아리는 1기부터 시작
        _members.value = listOf(selfMember(cohortId = 1L, role = MemberRole.LEADER))   // 나 혼자 리더
        _notifications.value = listOf(
            AppNotification(nextId++, com.damoim.app.domain.model.NotificationType.NOTICE, "$name 동아리가 만들어졌어요. 가입 코드를 공유해 부원을 초대해보세요!", "방금 전", isUnread = true),
        )
        seedSecondaryClubs(ClubSession(club, ClubRole.LEADER))
        return club
    }

    /** 본인(isMe) 명부 행. 이름·이메일은 membersFlow에서 항상 현재 프로필로 덮인다. */
    private fun selfMember(cohortId: Long, role: MemberRole): Member {
        val meName = myName
        return Member(501, meName, initialsOf(meName), cohortId, role, email = _user.value.email.orEmpty(), joinedLabel = "2024.09.15", isMe = true)
    }

    /** 33 전환용 부가 동아리 시드 — 현재 동아리 + 예시(한강 러너스). */
    private fun seedSecondaryClubs(current: ClubSession) {
        bundles.clear()
        _joinedClubs.value = listOf(
            com.damoim.app.domain.model.ClubMembership(current.club, current.role),
            com.damoim.app.domain.model.ClubMembership(MockData.runnersClub, ClubRole.MEMBER),
        )
        // 한강 러너스: 나는 일반 회원, 게시판·자료실은 빈 상태(신규 동아리처럼)에서 시작
        val runnerMe = Member(501, myName, initialsOf(myName), 31, MemberRole.MEMBER, email = _user.value.email.orEmpty(), joinedLabel = "2024.06.01", isMe = true)
        bundles[MockData.runnersClub.id] = ClubBundle(
            session = ClubSession(MockData.runnersClub, ClubRole.MEMBER),
            posts = emptyList(), comments = emptyMap(),
            pending = emptyList(), processed = emptyList(),
            notifications = emptyList(), resources = emptyList(),
            cohorts = MockData.runnersCohorts,
            members = listOf(runnerMe) + MockData.runnersMembersExceptMe(),
            draft = null,
            schedules = emptyList(), myApplications = emptyList(), scheduleDraft = null,
        )
    }

    fun regenerateJoinCode(): String {
        val code = randomCode()
        _session.update { it?.copy(club = it.club.copy(joinCode = code)) }
        return code
    }

    fun disableJoinCode() {
        _session.update { it?.copy(club = it.club.copy(joinCode = "")) }
    }

    private fun randomCode() = (1..6).map { "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".random() }.joinToString("")

    // ══════════ 가입 신청자(09) ══════════

    private val _pending = MutableStateFlow<List<JoinApplicant>>(emptyList())
    private val _processed = MutableStateFlow<List<com.damoim.app.domain.model.ProcessedApplicant>>(emptyList())

    fun applicantsFlow(): Flow<com.damoim.app.domain.model.ApplicantsBoard> =
        combine(_pending, _processed) { p, d -> com.damoim.app.domain.model.ApplicantsBoard(p, d) }

    fun decideApplicant(applicantId: Long, approve: Boolean) {
        val target = _pending.value.firstOrNull { it.id == applicantId } ?: return
        _pending.update { list -> list.filterNot { it.id == applicantId } }
        _processed.update { list ->
            listOf(com.damoim.app.domain.model.ProcessedApplicant(target, approved = approve, decidedLabel = "방금 전")) + list
        }
        if (approve) {
            _session.update { it?.copy(club = it.club.copy(memberCount = it.club.memberCount + 1)) }
            // 승인된 신청자를 명부(17)에 추가 — 모집중 기수(가장 최근=최상단)에 배정하고 그 기수 +1
            val targetCohort = _cohorts.value.firstOrNull()?.id ?: 25L
            _members.update { it + Member(nextId++, target.name, target.initial, targetCohort, MemberRole.MEMBER, email = "${target.initial}@kakao.com", joinedLabel = "방금 가입") }
            _cohorts.update { list -> list.map { if (it.id == targetCohort) it.copy(memberCount = it.memberCount + 1) else it } }
            _notifications.update { list ->
                listOf(
                    AppNotification(nextId++, com.damoim.app.domain.model.NotificationType.JOIN_APPROVED, "${target.name}님이 동아리에 합류했어요 🎉", "방금 전", isUnread = true),
                ) + list
            }
        }
    }

    // ══════════ 알림(37) ══════════

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // ══════════ 기수(19/42/44 · 69 공개 범위) ══════════

    private val _cohorts = MutableStateFlow<List<Cohort>>(emptyList())

    fun cohortsFlow(): Flow<List<Cohort>> = _cohorts.asStateFlow()

    /** 44 새 기수 추가. 새 기수는 0명에서 시작한다. 생성된 Cohort를 반환. */
    fun addCohort(shortLabel: String, displayName: String): Cohort {
        val cohort = Cohort(nextId++, displayName, shortLabel, memberCount = 0)
        _cohorts.update { listOf(cohort) + it }
        return cohort
    }

    /** 기수 이름 변경(19 연필). */
    fun renameCohort(cohortId: Long, shortLabel: String, displayName: String) =
        _cohorts.update { list -> list.map { if (it.id == cohortId) it.copy(label = displayName, short = shortLabel) else it } }

    // ══════════ 회원 명부(16/17/18/42/43) ══════════

    private val _members = MutableStateFlow<List<Member>>(emptyList())

    /** 명부. 본인(isMe) 행의 이름·이메일·이니셜은 항상 현재 프로필(_user)에서 덮어써 프로필 수정이 즉시 반영된다. */
    fun membersFlow(): Flow<List<Member>> = combine(_members, _user) { members, user ->
        members.map { m ->
            if (!m.isMe) m
            else {
                val name = user.nickname.ifBlank { m.name }
                m.copy(name = name, initials = initialsOf(name), email = user.email ?: m.email)
            }
        }
    }

    fun memberDetailFlow(memberId: Long): Flow<MemberDetail?> =
        combine(membersFlow(), _cohorts) { members, cohorts ->
            members.firstOrNull { it.id == memberId }?.let { m ->
                MemberDetail(
                    member = m,
                    cohortLabel = cohorts.firstOrNull { it.id == m.cohortId }?.label ?: "",
                    postCount = if (m.isMe) 14 else (m.id % 20).toInt(),
                    eventCount = if (m.isMe) 8 else (m.id % 9).toInt(),
                    lastActiveLabel = if (m.status == MemberStatus.DORMANT) "3주 전" else "1시간 전",
                )
            }
        }

    /** 자기 자신의 명부 역할(20 내 프로필 뱃지). */
    fun myMemberFlow(): Flow<Member?> = membersFlow().map { list -> list.firstOrNull { it.isMe } }

    /** 42 기수 변경 — 옛 기수 -1, 새 기수 +1로 카운트를 함께 옮긴다. */
    fun changeMemberCohort(memberId: Long, cohortId: Long) {
        val member = _members.value.firstOrNull { it.id == memberId } ?: return
        val old = member.cohortId
        if (old == cohortId) return
        _members.update { list -> list.map { if (it.id == memberId) it.copy(cohortId = cohortId) else it } }
        _cohorts.update { list ->
            list.map {
                when (it.id) {
                    old -> it.copy(memberCount = (it.memberCount - 1).coerceAtLeast(0))
                    cohortId -> it.copy(memberCount = it.memberCount + 1)
                    else -> it
                }
            }
        }
    }

    /** 18 역할 변경. */
    fun changeMemberRole(memberId: Long, role: MemberRole) =
        _members.update { list -> list.map { if (it.id == memberId) it.copy(role = role) else it } }

    /** 43 내보내기 — 명부에서 제거하고 기수·동아리 회원 수를 함께 감소시킨다. */
    fun removeMember(memberId: Long) {
        val member = _members.value.firstOrNull { it.id == memberId } ?: return
        _members.update { list -> list.filterNot { it.id == memberId } }
        _cohorts.update { list -> list.map { if (it.id == member.cohortId) it.copy(memberCount = (it.memberCount - 1).coerceAtLeast(0)) else it } }
        _session.update { it?.copy(club = it.club.copy(memberCount = (it.club.memberCount - 1).coerceAtLeast(0))) }
    }

    fun markAllNotificationsRead() =
        _notifications.update { list -> list.map { it.copy(isUnread = false) } }

    // ══════════ 게시판 ══════════

    private val _posts = MutableStateFlow<List<BoardPost>>(emptyList())
    private val _comments = MutableStateFlow<Map<Long, List<Comment>>>(emptyMap())
    private var nextId = 5000L
    private var orderCounter = 1_000_000L  // createdAt 단조 증가 값

    fun boardHomeFlow(): Flow<BoardHomeData> = _posts.map { posts ->
        BoardHomeData(
            pinned = posts.filter { it.isPinned }.sortedByDescending { it.createdAt },
            recent = posts.filterNot { it.isPinned }.sortedByDescending { it.createdAt },
        )
    }

    fun postsFlow(category: BoardCategory?): Flow<List<BoardPost>> = _posts.map { posts ->
        (if (category == null) posts else posts.filter { it.category == category })
            .sortedByDescending { it.createdAt }
    }

    fun postDetailFlow(postId: Long): Flow<PostDetail?> =
        combine(_posts, _comments) { posts, comments ->
            posts.firstOrNull { it.id == postId }?.let { PostDetail(it, comments[postId].orEmpty()) }
        }

    fun createPost(draft: PostDraft): Long {
        val me = _user.value
        val id = nextId++
        _posts.update { it + buildPost(id, draft, me) }
        return id
    }

    fun updatePost(postId: Long, draft: PostDraft) {
        _posts.update { list ->
            list.map { old ->
                if (old.id != postId) old else buildPost(postId, draft, _user.value).copy(
                    // 카운터·시간은 원본 유지 (내용만 수정)
                    createdAt = old.createdAt, timeLabel = old.timeLabel, dateLabel = old.dateLabel,
                    viewCount = old.viewCount, likeCount = old.likeCount, likedByMe = old.likedByMe,
                    commentCount = old.commentCount, readRate = old.readRate,
                    authorId = old.authorId, authorName = old.authorName,
                    authorInitials = old.authorInitials, authorGisu = old.authorGisu,
                    isAuthorLeader = old.isAuthorLeader,
                    // 수정으로 투표를 갈아끼우면 기존 내 투표는 초기화되므로 그대로 반영
                    recruit = draft.recruit?.let { d -> old.recruit?.copy(capacity = d.capacity, deadlineLabel = d.deadlineLabel, dday = d.dday, method = if (d.firstCome) "선착순" else "승인제") ?: buildRecruit(d) },
                )
            }
        }
    }

    private fun buildPost(id: Long, draft: PostDraft, me: AuthUser): BoardPost = BoardPost(
        id = id,
        category = draft.category,
        title = draft.title,
        content = draft.content,
        preview = draft.content.lineSequence().firstOrNull().orEmpty(),
        authorId = me.id,
        authorName = myName,
        authorInitials = initialsOf(myName),
        timeLabel = "방금 전",
        dateLabel = "방금 전",
        createdAt = orderCounter++,
        isPinned = draft.pinned,
        isAuthorLeader = role == ClubRole.LEADER,
        hasThumbnail = draft.photoLabels.isNotEmpty(),
        attachments = buildList {
            draft.photoLabels.forEach { add(PostAttachment.Image(it)) }
            addAll(draft.docs)
            draft.link?.let { add(it) }
        },
        poll = draft.poll?.let { d ->
            Poll(
                options = d.options.filter { it.isNotBlank() }.map { PollOption(it, 0) },
                anonymous = d.anonymous, multiSelect = d.multiSelect, deadlineLabel = d.deadlineLabel,
            )
        },
        recruit = draft.recruit?.let { buildRecruit(it) },
    )

    private fun buildRecruit(d: com.damoim.app.domain.model.RecruitDraft) = RecruitInfo(
        status = RecruitStatus.OPEN, dday = d.dday, current = 0, capacity = d.capacity,
        deadlineLabel = d.deadlineLabel, method = if (d.firstCome) "선착순" else "승인제",
    )

    fun deletePost(postId: Long) {
        _posts.update { list -> list.filterNot { it.id == postId } }
        _comments.update { it - postId }
    }

    /** 상단 고정 토글. 새 고정 상태를 반환. */
    fun togglePin(postId: Long): Boolean {
        var pinned = false
        _posts.update { list ->
            list.map { if (it.id == postId) it.copy(isPinned = !it.isPinned).also { p -> pinned = p.isPinned } else it }
        }
        return pinned
    }

    fun toggleLike(postId: Long) = _posts.update { list ->
        list.map {
            if (it.id != postId) it
            else it.copy(likedByMe = !it.likedByMe, likeCount = it.likeCount + if (it.likedByMe) -1 else 1)
        }
    }

    fun votePoll(postId: Long, optionIndex: Int) = _posts.update { list ->
        list.map { post ->
            val poll = post.poll
            if (post.id != postId || poll == null || optionIndex !in poll.options.indices) return@map post
            val options = poll.options.toMutableList()
            val newVotes: Set<Int>
            if (poll.multiSelect) {
                if (optionIndex in poll.myVotes) {
                    options[optionIndex] = options[optionIndex].copy(votes = (options[optionIndex].votes - 1).coerceAtLeast(0))
                    newVotes = poll.myVotes - optionIndex
                } else {
                    options[optionIndex] = options[optionIndex].copy(votes = options[optionIndex].votes + 1)
                    newVotes = poll.myVotes + optionIndex
                }
            } else {
                if (optionIndex in poll.myVotes) return@map post
                poll.myVotes.firstOrNull()?.let { old ->
                    options[old] = options[old].copy(votes = (options[old].votes - 1).coerceAtLeast(0))
                }
                options[optionIndex] = options[optionIndex].copy(votes = options[optionIndex].votes + 1)
                newVotes = setOf(optionIndex)
            }
            post.copy(poll = poll.copy(options = options, myVotes = newVotes))
        }
    }

    /** 다시 투표 — 내 표를 회수하고 선택 초기화. */
    fun clearPollVote(postId: Long) = _posts.update { list ->
        list.map { post ->
            val poll = post.poll
            if (post.id != postId || poll == null) return@map post
            val options = poll.options.toMutableList()
            poll.myVotes.forEach { i -> options[i] = options[i].copy(votes = (options[i].votes - 1).coerceAtLeast(0)) }
            post.copy(poll = poll.copy(options = options, myVotes = emptySet()))
        }
    }

    /** 모집 신청. 성공 시 true, 이미 신청/정원 초과면 false. 정원이 차면 자동 마감. */
    fun applyRecruit(postId: Long): Boolean {
        var applied = false
        _posts.update { list ->
            list.map { post ->
                val r = post.recruit
                if (post.id != postId || r == null || r.appliedByMe || r.current >= r.capacity || r.status == RecruitStatus.CLOSED) return@map post
                applied = true
                val current = r.current + 1
                post.copy(
                    recruit = r.copy(
                        current = current,
                        appliedByMe = true,
                        status = if (current >= r.capacity) RecruitStatus.CLOSED else r.status,
                        applicants = r.applicants + RecruitApplicant(initialsOf(myName), r.applicants.size % 3),
                    ),
                )
            }
        }
        return applied
    }

    /** 댓글 추가. parentId가 있으면 해당 댓글(과 그 답글들) 뒤에 답글로 삽입. */
    fun addComment(postId: Long, content: String, parentId: Long?) {
        val me = _user.value
        val post = _posts.value.firstOrNull { it.id == postId } ?: return
        val comment = Comment(
            id = nextId++, authorName = myName, authorInitials = initialsOf(myName),
            timeLabel = "방금 전", content = content,
            isReply = parentId != null, isAuthor = post.authorId == me.id, parentId = parentId,
        )
        _comments.update { map ->
            val list = map[postId].orEmpty().toMutableList()
            if (parentId == null) {
                list.add(comment)
            } else {
                // 부모 + 기존 답글 블록의 끝에 삽입
                var insertAt = list.indexOfFirst { it.id == parentId }
                if (insertAt < 0) { list.add(comment) } else {
                    insertAt++
                    while (insertAt < list.size && list[insertAt].parentId == parentId) insertAt++
                    list.add(insertAt, comment)
                }
            }
            map + (postId to list)
        }
        _posts.update { list -> list.map { if (it.id == postId) it.copy(commentCount = it.commentCount + 1) else it } }
    }

    // ══════════ 자료실(67/68/69) ══════════

    private val _resources = MutableStateFlow<List<ResourceFile>>(emptyList())

    fun resourcesFlow(folder: ResourceFolder?): Flow<List<ResourceFile>> = _resources.map { list ->
        (if (folder == null) list else list.filter { it.folder == folder })
            .sortedByDescending { it.createdAt }
    }

    fun resourceDetailFlow(resourceId: Long): Flow<ResourceFile?> =
        _resources.map { list -> list.firstOrNull { it.id == resourceId } }

    fun storageFlow(): Flow<StorageUsage> = _resources.map { list ->
        val used = list.sumOf { it.sizeBytes }
        StorageUsage(
            usedBytes = used,
            totalBytes = MockResourceData.QUOTA_BYTES,
            usedLabel = MockResourceData.formatStorageLabel(used),
            totalLabel = MockResourceData.QUOTA_LABEL,
            count = list.size,
        )
    }

    /** 69 업로드. 업로더는 현재 사용자, 목록 최상단에 노출된다. */
    fun uploadResource(draft: ResourceDraft): Long {
        val id = nextId++
        val ext = draft.fileName.substringAfterLast('.', "").uppercase().take(4).ifBlank { "DOC" }
        _resources.update { list ->
            list + ResourceFile(
                id = id,
                title = draft.title,
                fileName = draft.fileName,
                ext = ext,
                description = draft.description,
                folder = draft.folder,
                sizeLabel = draft.sizeLabel,
                sizeBytes = MockResourceData.parseSizeToBytes(draft.sizeLabel),
                uploaderId = _user.value.id,
                uploaderName = myName,
                uploaderIsLeader = role == ClubRole.LEADER,
                uploadedLabel = "방금 전",
                visibility = draft.visibility,
                cohortIds = draft.cohortIds,
                createdAt = orderCounter++,
            )
        }
        return id
    }

    fun deleteResource(resourceId: Long) =
        _resources.update { list -> list.filterNot { it.id == resourceId } }

    fun incrementDownload(resourceId: Long) = _resources.update { list ->
        list.map { if (it.id == resourceId) it.copy(downloadCount = it.downloadCount + 1) else it }
    }

    // ══════════ 검색(85/40/76) ══════════

    private val _recentSearches = MutableStateFlow(listOf("MT", "신입 부원 모집", "회칙", "OT 일정"))

    fun searchSuggestionsFlow(): Flow<SearchSuggestions> = _recentSearches.map {
        SearchSuggestions(recent = it, recommended = MockData.recommendedKeywords)
    }

    fun removeRecentSearch(query: String) = _recentSearches.update { it - query }
    fun clearRecentSearches() = _recentSearches.update { emptyList() }

    fun search(query: String): SearchResults {
        _recentSearches.update { (listOf(query) + (it - query)).take(8) }
        val posts = _posts.value
            .filter { it.title.contains(query, true) || it.content.contains(query, true) || it.authorName.contains(query, true) }
            .sortedByDescending { it.createdAt }
        val schedules = demoScheduleHits().filter { it.title.contains(query, true) }
        val files = _posts.value
            .flatMap { p -> p.attachments.filterIsInstance<PostAttachment.FileDoc>() }
            .filter { it.name.contains(query, true) }
            .map { SearchFileHit(it.name, "게시글 첨부 · ${it.size}") }
        return SearchResults(query, posts, schedules, files)
    }

    private fun demoScheduleHits(): List<SearchScheduleHit> =
        if (_session.value?.club?.id == MockData.myClub.id) {
            listOf(
                SearchScheduleHit("6월", "14", "신입 환영 MT", "1박 2일 · 가평"),
                SearchScheduleHit("6월", "7", "정기 월례회의", "오전 10:00 · 동아리방"),
            )
        } else emptyList()

    // ══════════ 홈 요약(05/06) ══════════

    private data class HomeBase(
        val session: ClubSession?, val posts: List<BoardPost>, val pending: List<JoinApplicant>,
        val notifications: List<AppNotification>, val user: AuthUser,
    )

    fun homeSummaryFlow(): Flow<HomeSummary?> {
        // combine은 5개까지라 base 5종을 먼저 묶고, 회원 기수(내 기수 파생)를 위해 _members/_cohorts를 더한다
        val base = combine(_session, _posts, _pending, _notifications, _user) { s, p, pend, n, u -> HomeBase(s, p, pend, n, u) }
        return combine(base, _members, _cohorts, _schedules) { b, members, cohorts, allSchedules ->
            val session = b.session ?: return@combine null
            val posts = b.posts; val pending = b.pending; val notifications = b.notifications; val user = b.user
            val myCohortShort = cohorts.firstOrNull { it.id == members.firstOrNull { m -> m.isMe }?.cohortId }?.short ?: "내 기수"
            val nowDay = today()
            val schedules = upcomingSchedules(allSchedules, nowDay)   // 홈 캐러셀 = 실제 일정 스토어에서 파생
            val thisWeekCount = allSchedules.count { it.date >= nowDay && it.date.toEpochDays() - nowDay.toEpochDays() in 0..6 }
            val previews = posts
                .sortedWith(compareByDescending<BoardPost> { it.isPinned }.thenByDescending { it.createdAt })
                .take(3)
                .map { BoardPreview(it.category, it.title, it.commentCount) }
            val alert = when (session.role) {
                ClubRole.LEADER ->
                    if (pending.isNotEmpty()) HomeAlert("가입 신청 ${pending.size}건이 기다려요", "탭해서 승인/거절 처리", AlertKind.JOIN_REQUEST)
                    else null
                ClubRole.MEMBER ->
                    schedules.firstOrNull()?.let { HomeAlert("${it.title} 일정이 다가와요", "${it.date} ${it.subtitle}", AlertKind.SCHEDULE, badge = it.dday) }
            }
            HomeSummary(
                role = session.role,
                clubName = session.club.name,
                memberName = user.nickname.ifBlank { "회원" },
                stats = when (session.role) {
                    ClubRole.LEADER -> listOf(
                        HomeStat("${session.club.memberCount}", "회원"),
                        HomeStat("${pending.size}", "신청 대기"),
                        HomeStat("$thisWeekCount", "이번 주"),
                    )
                    ClubRole.MEMBER -> listOf(
                        HomeStat(myCohortShort, "내 기수"),
                        HomeStat("$thisWeekCount", "이번 주 일정"),
                        HomeStat("${posts.size}", "새 글"),
                    )
                },
                alert = alert,
                schedules = schedules,
                boardPreviews = previews,
                hasUnreadNotification = notifications.any { it.isUnread },
            )
        }
    }

    // ══════════ 일정/이벤트(F 그룹 · 21~25 · 46~48 · 61~63) ══════════

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    private val _myApplications = MutableStateFlow<List<MyApplication>>(emptyList())
    private var scheduleDraft: ScheduleDraft? = null

    private fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

    /** 일정 목록(날짜·정렬 보조 순). 21 캘린더·22 목록 공용. */
    fun schedulesFlow(): Flow<List<Schedule>> = _schedules.map { list ->
        list.sortedWith(compareBy({ it.date }, { it.createdAt }))
    }

    fun scheduleDetailFlow(scheduleId: Long): Flow<Schedule?> =
        _schedules.map { list -> list.firstOrNull { it.id == scheduleId } }

    fun myApplicationsFlow(): Flow<List<MyApplication>> = _myApplications.asStateFlow()

    private fun timeLabel(hour24: Int, minute: Int): String {
        val ampm = if (hour24 < 12) "오전" else "오후"
        val h12 = (hour24 % 12).let { if (it == 0) 12 else it }
        return "$ampm $h12:${minute.toString().padStart(2, '0')}"
    }

    private fun ddayOf(target: LocalDate): String {
        val diff = target.toEpochDays() - today().toEpochDays()
        return when {
            diff > 0 -> "D-$diff"
            diff == 0L -> "D-DAY"
            else -> "종료"
        }
    }

    /** 홈 캐러셀(05/06)용 — 다가오는 일정 상위 3개를 [UpcomingSchedule]로 파생. 첫 카드=진한 네이비. */
    private fun upcomingSchedules(list: List<Schedule>, today: LocalDate): List<UpcomingSchedule> =
        list.filter { it.date >= today }.sortedBy { it.date.toEpochDays() }.take(3).mapIndexed { i, s ->
            UpcomingSchedule(
                id = s.id,
                dday = ddayOf(s.date),
                date = "${s.date.monthNumber}.${s.date.dayOfMonth.toString().padStart(2, '0')} ${MockScheduleData.weekday(s.date)}",
                title = s.title,
                subtitle = s.event?.meta?.takeIf { it.isNotBlank() } ?: buildString { append(s.timeLabel); if (s.location.isNotBlank()) append(" · ${s.location}") },
                primary = i == 0,
            )
        }

    /** 23 등록. 생성된 일정 id 반환. */
    fun createSchedule(draft: ScheduleDraft): Long {
        val id = nextId++
        _schedules.update { it + buildSchedule(id, draft, existing = null) }
        scheduleDraft = null
        return id
    }

    /** 23 수정(62 '이벤트 수정' 경유). */
    fun updateSchedule(draft: ScheduleDraft): Long {
        val id = draft.editId ?: return createSchedule(draft)
        _schedules.update { list -> list.map { if (it.id == id) buildSchedule(id, draft, existing = it) else it } }
        scheduleDraft = null
        return id
    }

    private fun buildSchedule(id: Long, draft: ScheduleDraft, existing: Schedule?): Schedule {
        val start = draft.startDate ?: today()
        val event = if (draft.isEvent) {
            val deadline = draft.deadlineDate ?: start
            EventInfo(
                capacity = draft.capacity.toIntOrNull() ?: 0,
                appliedCount = existing?.event?.appliedCount ?: 0,
                deadlineDate = deadline,
                deadlineLabel = "${MockScheduleData.shortDate(deadline)} ${timeLabel(draft.deadlineHour, draft.deadlineMinute)}",
                status = existing?.event?.status ?: EventStatus.OPEN,
                dday = ddayOf(start),
                meta = draft.location.ifBlank { "이벤트" },
                form = draft.form,
                applicants = existing?.event?.applicants ?: emptyList(),
                appliedByMe = existing?.event?.appliedByMe ?: false,
            )
        } else null
        return Schedule(
            id = id,
            type = if (draft.isEvent) ScheduleType.EVENT else ScheduleType.SCHEDULE,
            title = draft.title,
            date = start,
            timeLabel = timeLabel(draft.startHour, draft.startMinute),
            startHour = draft.startHour,
            startMinute = draft.startMinute,
            endLabel = if (draft.hasEnd && draft.endDate != null) "~${MockScheduleData.shortDate(draft.endDate)}" else null,
            endDate = if (draft.hasEnd) draft.endDate else null,
            endHour = draft.endHour,
            endMinute = draft.endMinute,
            location = draft.location,
            memo = draft.memo,
            accent = existing?.accent ?: ScheduleAccent.PRIMARY,
            addedToMyCalendar = existing?.addedToMyCalendar ?: false,
            hostName = myName,
            createdAt = existing?.createdAt ?: orderCounter++,
            event = event,
        )
    }

    /** 63 일정 삭제 / 62 이벤트 취소. 관련 내 신청도 제거. */
    fun deleteSchedule(scheduleId: Long) {
        _schedules.update { list -> list.filterNot { it.id == scheduleId } }
        _myApplications.update { list -> list.filterNot { it.eventId == scheduleId } }
    }

    /** 21/22 '내 일정에 추가' 토글 — 새 상태 반환. */
    fun toggleScheduleCalendar(scheduleId: Long): Boolean {
        var added = false
        _schedules.update { list ->
            list.map { if (it.id == scheduleId) it.copy(addedToMyCalendar = !it.addedToMyCalendar).also { s -> added = s.addedToMyCalendar } else it }
        }
        return added
    }

    /** 47/62 신청 조기 마감. */
    fun closeEventEarly(scheduleId: Long) {
        _schedules.update { list ->
            list.map { s -> if (s.id != scheduleId || s.event == null) s else s.copy(event = s.event.copy(status = EventStatus.CLOSED)) }
        }
    }

    /** 25 참여 신청 — 성공 시 true. 이미 신청/정원초과/마감이면 false. 정원이 차면 자동 마감. */
    fun applyToEvent(scheduleId: Long, answers: List<QuestionAnswer>): Boolean {
        var applied = false
        var title = ""
        var dateLabel = ""
        _schedules.update { list ->
            list.map { s ->
                val e = s.event
                if (s.id != scheduleId || e == null || e.appliedByMe || e.status != EventStatus.OPEN || e.appliedCount >= e.capacity) return@map s
                applied = true; title = s.title; dateLabel = MockScheduleData.midDate(s.date)
                val count = e.appliedCount + 1
                s.copy(
                    event = e.copy(
                        appliedCount = count,
                        appliedByMe = true,
                        status = if (count >= e.capacity) EventStatus.CLOSED else e.status,
                        applicants = e.applicants + EventApplicant(nextId++, myName, initialsOf(myName), e.applicants.size % 4, ApplicantStatus.APPLIED, "방금 전", answers),
                    ),
                )
            }
        }
        if (applied) {
            _myApplications.update { listOf(MyApplication(scheduleId, title, dateLabel, ApplicationStatus.APPLIED, answers)) + it.filterNot { a -> a.eventId == scheduleId } }
        }
        return applied
    }

    /** 48 응답 수정 — 재신청 없이 답변만 교체. */
    fun updateMyApplication(scheduleId: Long, answers: List<QuestionAnswer>) {
        _myApplications.update { list -> list.map { if (it.eventId == scheduleId) it.copy(answers = answers) else it } }
        _schedules.update { list ->
            list.map { s ->
                val e = s.event ?: return@map s
                if (s.id != scheduleId) s else s.copy(event = e.copy(applicants = e.applicants.map { if (it.name == myName) it.copy(answers = answers) else it }))
            }
        }
    }

    /** 48 신청 취소 — 내 신청 제거, 정원 -1, 내 신청자 행 취소 표시. */
    fun cancelMyApplication(eventId: Long) {
        _myApplications.update { list -> list.filterNot { it.eventId == eventId } }
        _schedules.update { list ->
            list.map { s ->
                val e = s.event
                if (s.id != eventId || e == null) s
                else {
                    val count = (e.appliedCount - 1).coerceAtLeast(0)
                    s.copy(
                        event = e.copy(
                            appliedCount = count,
                            appliedByMe = false,
                            status = if (e.status == EventStatus.CLOSED && count < e.capacity) EventStatus.OPEN else e.status,
                            applicants = e.applicants.map { if (it.name == myName) it.copy(status = ApplicantStatus.CANCELED) else it },
                        ),
                    )
                }
            }
        }
    }

    /** G5 '공지로 알리기' — 이벤트를 게시판 공지 글(필독)로 등록. */
    fun announceEvent(scheduleId: Long) {
        val s = _schedules.value.firstOrNull { it.id == scheduleId } ?: return
        createPost(
            PostDraft(
                category = BoardCategory.NOTICE,
                title = "[이벤트] ${s.title}",
                content = "${MockScheduleData.longDate(s.date)} ${s.timeLabel} · ${s.location}\n\n${s.memo}",
                pinned = true,
            ),
        )
    }

    // 진행 중 등록 초안(23 ↔ 46 양식편집 공유)
    fun currentScheduleDraft(): ScheduleDraft? = scheduleDraft
    fun saveScheduleDraft(draft: ScheduleDraft) { scheduleDraft = draft }
    fun clearScheduleDraft() { scheduleDraft = null }

    // ══════════ 시드(데모 동아리) ══════════

    private fun seedDemoClub() {
        val seeded = MockBoardData.seedPosts()
        val comments = MockBoardData.seedComments()
        // 댓글 수는 실제 댓글 목록과 항상 일치시킨다
        _posts.value = seeded.map { it.copy(commentCount = comments[it.id].orEmpty().size) }
        _comments.value = comments
        _pending.value = MockData.applicants
        _processed.value = MockData.processedApplicants
        _notifications.value = MockData.notifications
        _resources.value = MockResourceData.seedResources()
        val seedToday = today()
        _schedules.value = MockScheduleData.seedSchedules(seedToday)
        _myApplications.value = MockScheduleData.seedMyApplications(seedToday)
        _cohorts.value = MockData.cohorts
        // 본인은 세션 역할 기반: LEADER면 리더 슬롯을 차지(김민준 생략), MEMBER면 24기 일반 + 김민준 리더 포함
        val meLeader = _session.value?.role == ClubRole.LEADER
        val me = selfMember(cohortId = if (meLeader) 23L else 24L, role = if (meLeader) MemberRole.LEADER else MemberRole.MEMBER)
        _members.value = listOf(me) + MockData.demoMembersExceptMe(includeLeader = !meLeader)
        _session.value?.let { seedSecondaryClubs(it) }
        orderCounter = 1_000_000L + seeded.size + 1
    }

    // ══════════ 작성 임시저장(15) ══════════

    private var savedDraft: PostDraft? = null

    fun saveDraft(draft: PostDraft) { savedDraft = draft }
    fun loadDraft(): PostDraft? = savedDraft
    fun clearDraft() { savedDraft = null }
}
