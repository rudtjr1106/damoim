package com.damoim.app.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.presentation.board.detail.PostDetailRoute
import com.damoim.app.presentation.board.home.BoardHomeRoute
import com.damoim.app.presentation.board.list.BoardListRoute
import com.damoim.app.presentation.board.search.SearchRoute
import com.damoim.app.presentation.board.write.PostWriteRoute
import com.damoim.app.presentation.clubsettings.ClubSettingsRoute
import com.damoim.app.presentation.component.DamoimToastHost
import com.damoim.app.presentation.component.MainTab
import com.damoim.app.presentation.home.HomeRoute
import com.damoim.app.presentation.joinmanage.JoinManageRoute
import com.damoim.app.presentation.member.cohort.CohortManageRoute
import com.damoim.app.presentation.member.detail.MemberDetailRoute
import com.damoim.app.presentation.member.list.MemberListRoute
import com.damoim.app.presentation.member.manage.MemberManageRoute
import com.damoim.app.presentation.notification.NotificationRoute
import com.damoim.app.presentation.profile.edit.ProfileEditRoute
import com.damoim.app.presentation.profile.myprofile.MyProfileRoute
import com.damoim.app.presentation.resource.archive.ArchiveRoute
import com.damoim.app.presentation.resource.detail.ResourceDetailRoute
import com.damoim.app.presentation.resource.upload.ResourceUploadRoute
import com.damoim.app.presentation.schedule.applicants.ApplicantsRoute
import com.damoim.app.presentation.schedule.detail.EventDetailRoute
import com.damoim.app.presentation.schedule.home.ScheduleHomeRoute
import com.damoim.app.presentation.schedule.myapplications.MyApplicationsRoute
import com.damoim.app.presentation.schedule.register.ScheduleRegisterRoute
import com.damoim.app.presentation.settings.admin.AdminRoute
import com.damoim.app.presentation.settings.blocked.BlockedRoute
import com.damoim.app.presentation.settings.home.SettingsHomeRoute
import com.damoim.app.presentation.settings.inquiry.InquiryRoute
import com.damoim.app.presentation.settings.notification.NotifSettingsRoute
import com.damoim.app.presentation.settings.plan.PlanRoute
import com.damoim.app.presentation.settings.result.PaymentResultScreen
import com.damoim.app.presentation.settings.subscription.SubscriptionRoute
import com.damoim.app.presentation.theme.DamoimStrings

/** 메인(로그인 이후) 플로우 목적지. */
private sealed interface MainDestination {
    // 탭 루트(하단 탭바 표시)
    data object Home : MainDestination                                  // 05/06
    data object BoardHome : MainDestination                            // 10 (게시판 탭)
    // 게시판 서브(푸시)
    data class BoardList(val category: BoardCategory) : MainDestination // 11/12/13
    data class PostDetail(val postId: Long) : MainDestination          // 14/36/79/84
    data class PostWrite(val category: BoardCategory, val editPostId: Long? = null) : MainDestination // 15/34/35/39/70 (+수정)
    data object Search : MainDestination                               // 85/40/76
    // D 자료실(푸시 — 하단 탭바는 '홈' 활성 유지)
    data object Archive : MainDestination                              // 67
    data class ResourceDetail(val resourceId: Long) : MainDestination  // 68
    data object ResourceUpload : MainDestination                       // 69
    // E 회원·기수 관리
    data object MemberManage : MainDestination                         // 16 (회원 탭 루트)
    data class MemberList(val cohortId: Long? = null) : MainDestination // 17/77
    data class MemberDetail(val memberId: Long) : MainDestination      // 18
    data object CohortManage : MainDestination                         // 19
    data object MyProfile : MainDestination                            // 20
    data object ProfileEdit : MainDestination                          // 45
    // F 일정·이벤트
    data object ScheduleHome : MainDestination                         // 21/22 (일정 탭 루트)
    data class ScheduleRegister(val editId: Long? = null) : MainDestination // 23 (+46/51 오버레이)
    data class EventDetail(val scheduleId: Long) : MainDestination     // 24 (+62/25/63 오버레이)
    data class Applicants(val scheduleId: Long) : MainDestination      // 47
    data object MyApplications : MainDestination                       // 48/75
    // G 설정·구독·권한
    data object SettingsHome : MainDestination                         // 26 (설정 탭 루트)
    data object Plan : MainDestination                                 // 27
    data object SubscriptionManage : MainDestination                   // 29
    data class PaymentResult(val success: Boolean) : MainDestination   // 49/50
    data object Admin : MainDestination                               // 30 (+64/다이얼로그)
    data object NotifSettings : MainDestination                        // 65
    data object Inquiry : MainDestination                             // 66
    data object Blocked : MainDestination                            // 83
    // B 서브
    data object ClubSettings : MainDestination
    data object JoinManage : MainDestination
    data object Notification : MainDestination
}

/**
 * 메인(홈) 플로우 호스트. 홈(05/06) + 게시판(C: 10·11/12/13·14/36·15) + B 서브화면.
 * 하단 탭 중 홈·게시판만 실제 화면이 있고 일정·회원·설정은 토스트로 안내한다.
 */
@Composable
fun MainNavHost(initialRole: ClubRole, onExitToAuth: () -> Unit = {}) {
    // 역할은 세션에서 관찰 — 동아리 전환(33)으로 세션이 바뀌면 자동 반영된다
    val ctx by AppGraph.observeMyContextUseCase().collectAsState(
        initial = com.damoim.app.domain.usecase.ObserveMyContextUseCase.MyContext(0, "", initialRole),
    )
    val role = ctx.role ?: initialRole

    val backStack: SnapshotStateList<MainDestination> =
        remember { mutableStateListOf<MainDestination>(MainDestination.Home) }
    var toast by remember { mutableStateOf<String?>(null) }

    fun navigate(d: MainDestination) = backStack.add(d)
    fun back() { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
    fun resetTo(d: MainDestination) { backStack.clear(); backStack.add(d) }
    fun onTab(tab: MainTab) = when (tab) {
        MainTab.HOME -> resetTo(MainDestination.Home)
        MainTab.BOARD -> resetTo(MainDestination.BoardHome)
        MainTab.MEMBERS -> resetTo(if (role == ClubRole.LEADER) MainDestination.MemberManage else MainDestination.MyProfile)
        MainTab.SCHEDULE -> resetTo(MainDestination.ScheduleHome)
        MainTab.SETTINGS -> resetTo(MainDestination.SettingsHome)
        else -> { toast = DamoimStrings.TOAST_COMING_SOON }
    }

    // 시스템 뒤로가기: 스택이 있으면 pop, 탭 루트(게시판/회원)에선 홈 탭으로 (홈에선 기본 동작=앱 나가기)
    val atTabRoot = backStack.last() == MainDestination.BoardHome || backStack.last() == MainDestination.MemberManage || backStack.last() == MainDestination.MyProfile || backStack.last() == MainDestination.ScheduleHome || backStack.last() == MainDestination.SettingsHome
    com.damoim.app.platform.PlatformBackHandler(
        enabled = backStack.size > 1 || atTabRoot,
    ) {
        if (backStack.size > 1) back() else resetTo(MainDestination.Home)
    }

    Box(Modifier.fillMaxSize()) {
        when (val current = backStack.last()) {
            MainDestination.Home -> HomeRoute(
                role = role,
                onNavigateJoinManage = { navigate(MainDestination.JoinManage) },
                onNavigateNotifications = { navigate(MainDestination.Notification) },
                onNavigateClubSettings = { navigate(MainDestination.ClubSettings) },
                onNavigateArchive = { navigate(MainDestination.Archive) },
                onComingSoon = { label ->
                    // 홈 퀵액션: 게시판→탭, 회원 관리(리더)→16, 내 프로필(회원)→20, 그 외 준비중
                    when (label) {
                        DamoimStrings.QA_BOARD -> resetTo(MainDestination.BoardHome)
                        DamoimStrings.QA_MEMBERS -> navigate(MainDestination.MemberManage)
                        DamoimStrings.QA_PROFILE -> navigate(MainDestination.MyProfile)
                        DamoimStrings.QA_SCHEDULE, DamoimStrings.HOME_SECTION_SCHEDULE -> resetTo(MainDestination.ScheduleHome)
                        else -> toast = DamoimStrings.TOAST_COMING_SOON
                    }
                },
                onOpenSchedule = { navigate(MainDestination.EventDetail(it)) },
                onTabSelect = { tab -> onTab(tab) },
            )

            MainDestination.BoardHome -> BoardHomeRoute(
                onOpenPost = { id -> navigate(MainDestination.PostDetail(id)) },
                onOpenCategory = { category -> navigate(MainDestination.BoardList(category)) },
                onSearch = { navigate(MainDestination.Search) },
                onWrite = { navigate(MainDestination.PostWrite(BoardCategory.FREE)) },
                onTabSelect = { tab -> onTab(tab) },
            )

            is MainDestination.BoardList -> BoardListRoute(
                category = current.category,
                onBack = { back() },
                onOpenPost = { id -> navigate(MainDestination.PostDetail(id)) },
            )

            MainDestination.Search -> SearchRoute(
                onBack = { back() },
                onOpenPost = { id -> navigate(MainDestination.PostDetail(id)) },
                onOpenSchedule = { id -> navigate(MainDestination.EventDetail(id)) },
                onComingSoon = { toast = DamoimStrings.TOAST_COMING_SOON },
            )

            is MainDestination.PostDetail -> PostDetailRoute(
                postId = current.postId,
                onBack = { back() },
                onEdit = { post -> navigate(MainDestination.PostWrite(post.category, editPostId = post.id)) },
                onToast = { toast = it },
            )

            is MainDestination.PostWrite -> PostWriteRoute(
                initialCategory = current.category,
                editPostId = current.editPostId,
                onCancel = { back() },
                onDone = { edited ->
                    back()
                    toast = if (edited) DamoimStrings.TOAST_POST_UPDATED else DamoimStrings.TOAST_POST_SUBMITTED
                },
                onToast = { toast = it },
            )

            MainDestination.Archive -> ArchiveRoute(
                onBack = { back() },
                onOpenResource = { id -> navigate(MainDestination.ResourceDetail(id)) },
                onUpload = { navigate(MainDestination.ResourceUpload) },
                onTabSelect = { tab -> onTab(tab) },
            )

            is MainDestination.ResourceDetail -> ResourceDetailRoute(
                resourceId = current.resourceId,
                onBack = { back() },
                onToast = { toast = it },
            )

            MainDestination.ResourceUpload -> ResourceUploadRoute(
                onCancel = { back() },
                onUploaded = {
                    back()
                    toast = DamoimStrings.TOAST_RESOURCE_UPLOADED
                },
                onToast = { toast = it },
            )

            MainDestination.MemberManage -> MemberManageRoute(
                onOpenList = { navigate(MainDestination.MemberList()) },
                onOpenJoinManage = { navigate(MainDestination.JoinManage) },
                onOpenCohorts = { navigate(MainDestination.CohortManage) },
                onOpenProfile = { navigate(MainDestination.MyProfile) },
                onTabSelect = { tab -> onTab(tab) },
            )

            is MainDestination.MemberList -> MemberListRoute(
                initialCohortId = current.cohortId,
                onBack = { back() },
                onOpenMember = { id -> navigate(MainDestination.MemberDetail(id)) },
                onShareCode = { navigate(MainDestination.ClubSettings) },
            )

            is MainDestination.MemberDetail -> MemberDetailRoute(
                memberId = current.memberId,
                onBack = { back() },
                onToast = { toast = it },
            )

            MainDestination.CohortManage -> CohortManageRoute(
                onBack = { back() },
                onToast = { toast = it },
            )

            MainDestination.MyProfile -> MyProfileRoute(
                onBack = { back() },
                onEditProfile = { navigate(MainDestination.ProfileEdit) },
                onExitToAuth = onExitToAuth,
                onSwitched = { resetTo(MainDestination.Home) },   // 동아리 전환 → 새 동아리 홈으로
                onOpenNotification = { navigate(MainDestination.NotifSettings) },
                onComingSoon = { toast = DamoimStrings.TOAST_COMING_SOON },
            )

            MainDestination.ProfileEdit -> ProfileEditRoute(
                onCancel = { back() },
                onSaved = {
                    back()
                    toast = DamoimStrings.TOAST_PROFILE_UPDATED
                },
            )

            MainDestination.ScheduleHome -> ScheduleHomeRoute(
                isLeader = role == ClubRole.LEADER,
                onOpenSchedule = { navigate(MainDestination.EventDetail(it)) },
                onRegister = { navigate(MainDestination.ScheduleRegister()) },
                onOpenMyApps = { navigate(MainDestination.MyApplications) },
                onTabSelect = { tab -> onTab(tab) },
                onToast = { toast = it },
            )

            is MainDestination.ScheduleRegister -> ScheduleRegisterRoute(
                editId = current.editId,
                onCancel = { back() },
                onDone = { edited ->
                    back()
                    toast = if (edited) DamoimStrings.TOAST_SCHEDULE_UPDATED else DamoimStrings.TOAST_SCHEDULE_CREATED
                },
            )

            is MainDestination.EventDetail -> EventDetailRoute(
                scheduleId = current.scheduleId,
                isLeader = role == ClubRole.LEADER,
                onBack = { back() },
                onEdit = { navigate(MainDestination.ScheduleRegister(editId = it)) },
                onApplicants = { navigate(MainDestination.Applicants(it)) },
                onToast = { toast = it },
            )

            is MainDestination.Applicants -> ApplicantsRoute(
                scheduleId = current.scheduleId,
                onBack = { back() },
                onToast = { toast = it },
            )

            MainDestination.MyApplications -> MyApplicationsRoute(
                onBack = { back() },
                onOpenEvent = { navigate(MainDestination.EventDetail(it)) },
                onToast = { toast = it },
            )

            MainDestination.SettingsHome -> SettingsHomeRoute(
                onOpenClubSettings = { navigate(MainDestination.ClubSettings) },
                onOpenAdmin = { navigate(MainDestination.Admin) },
                onOpenPlan = { navigate(MainDestination.Plan) },
                onOpenSubscription = { navigate(MainDestination.SubscriptionManage) },
                onOpenNotif = { navigate(MainDestination.NotifSettings) },
                onOpenInquiry = { navigate(MainDestination.Inquiry) },
                onOpenBlocked = { navigate(MainDestination.Blocked) },
                onTabSelect = { tab -> onTab(tab) },
            )

            MainDestination.Plan -> PlanRoute(
                onBack = { back() },
                onPaySuccess = { navigate(MainDestination.PaymentResult(success = true)) },
                onPayFail = { navigate(MainDestination.PaymentResult(success = false)) },
            )

            is MainDestination.PaymentResult -> PaymentResultScreen(
                success = current.success,
                onDone = {
                    if (current.success) { resetTo(MainDestination.SettingsHome); navigate(MainDestination.SubscriptionManage) }
                    else resetTo(MainDestination.SettingsHome)
                },
                onRetry = { back() },   // 결과 화면 pop → 27 구독플랜으로 복귀
            )

            MainDestination.SubscriptionManage -> SubscriptionRoute(
                onBack = { back() },
                onChangePlan = { navigate(MainDestination.Plan) },
                onToast = { toast = it },
            )

            MainDestination.Admin -> AdminRoute(
                onBack = { back() },
                onOpenMember = { id -> navigate(MainDestination.MemberDetail(id)) },
                onToast = { toast = it },
            )

            MainDestination.NotifSettings -> NotifSettingsRoute(
                isLeaderOrStaff = role == ClubRole.LEADER,
                onBack = { back() },
            )

            MainDestination.Inquiry -> InquiryRoute(
                onBack = { back() },
                onToast = { toast = it },
            )

            MainDestination.Blocked -> BlockedRoute(
                onBack = { back() },
                onToast = { toast = it },
            )

            MainDestination.ClubSettings -> ClubSettingsRoute(
                onBack = { back() },
                onToast = { toast = it },
            )

            MainDestination.JoinManage -> JoinManageRoute(
                onBack = { back() },
                onToast = { toast = it },
            )

            MainDestination.Notification -> NotificationRoute(
                onBack = { back() },
            )
        }

        DamoimToastHost(message = toast, onDismiss = { toast = null })
    }
}
