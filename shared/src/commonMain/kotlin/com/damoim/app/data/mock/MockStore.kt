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
import com.damoim.app.domain.model.JoinApplicant
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

    /** 코드 가입 완료(04 확인) 후 데모 동아리 입장. 이미 세션이 있으면(생성 경로) 무시. */
    fun enterClub(role: ClubRole) {
        if (_session.value != null) return
        _session.value = ClubSession(MockData.myClub, role)
        seedDemoClub()
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
        _cohorts.value = listOf(Cohort(1, "1기", "1기", 1))   // 새 동아리는 1기부터 시작
        _notifications.value = listOf(
            AppNotification(nextId++, com.damoim.app.domain.model.NotificationType.NOTICE, "$name 동아리가 만들어졌어요. 가입 코드를 공유해 부원을 초대해보세요!", "방금 전", isUnread = true),
        )
        return club
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

    // ══════════ 기수(19/42 · 69 공개 범위) ══════════

    private val _cohorts = MutableStateFlow<List<Cohort>>(emptyList())

    fun cohortsFlow(): Flow<List<Cohort>> = _cohorts.asStateFlow()

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

    fun homeSummaryFlow(): Flow<HomeSummary?> =
        combine(_session, _posts, _pending, _notifications, _user) { session, posts, pending, notifications, user ->
            session ?: return@combine null
            val isDemo = session.club.id == MockData.myClub.id
            val schedules = if (isDemo) MockData.schedules else emptyList()
            val previews = posts
                .sortedWith(compareByDescending<BoardPost> { it.isPinned }.thenByDescending { it.createdAt })
                .take(3)
                .map { BoardPreview(it.category, it.title, it.commentCount) }
            val alert = when (session.role) {
                ClubRole.LEADER ->
                    if (pending.isNotEmpty()) HomeAlert("가입 신청 ${pending.size}건이 기다려요", "탭해서 승인/거절 처리", AlertKind.JOIN_REQUEST)
                    else null
                ClubRole.MEMBER ->
                    schedules.firstOrNull()?.let { HomeAlert("${it.title}가 3일 남았어요", "${it.date} ${it.subtitle}", AlertKind.SCHEDULE, badge = it.dday) }
            }
            HomeSummary(
                role = session.role,
                clubName = session.club.name,
                memberName = user.nickname.ifBlank { "회원" },
                stats = when (session.role) {
                    ClubRole.LEADER -> listOf(
                        HomeStat("${session.club.memberCount}", "회원"),
                        HomeStat("${pending.size}", "신청 대기"),
                        HomeStat("${schedules.size}", "이번 주"),
                    )
                    ClubRole.MEMBER -> listOf(
                        HomeStat("24기", "내 기수"),
                        HomeStat("${schedules.size}", "이번 주 일정"),
                        HomeStat("${posts.size}", "새 글"),
                    )
                },
                alert = alert,
                schedules = schedules,
                boardPreviews = previews,
                hasUnreadNotification = notifications.any { it.isUnread },
            )
        }

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
        _cohorts.value = MockData.cohorts
        orderCounter = 1_000_000L + seeded.size + 1
    }

    // ══════════ 작성 임시저장(15) ══════════

    private var savedDraft: PostDraft? = null

    fun saveDraft(draft: PostDraft) { savedDraft = draft }
    fun loadDraft(): PostDraft? = savedDraft
    fun clearDraft() { savedDraft = null }
}
