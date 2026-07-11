package com.damoim.app.core.di

import com.damoim.app.data.repository.MockAuthRepository
import com.damoim.app.data.repository.MockBoardRepository
import com.damoim.app.data.repository.MockClubRepository
import com.damoim.app.data.repository.MockNotificationRepository
import com.damoim.app.data.repository.MockResourceRepository
import com.damoim.app.data.repository.MockScheduleRepository
import com.damoim.app.data.repository.MockSettingsRepository
import com.damoim.app.domain.repository.AuthRepository
import com.damoim.app.domain.repository.BoardRepository
import com.damoim.app.domain.repository.ClubRepository
import com.damoim.app.domain.repository.NotificationRepository
import com.damoim.app.domain.repository.ResourceRepository
import com.damoim.app.domain.repository.ScheduleRepository
import com.damoim.app.domain.repository.SettingsRepository
import com.damoim.app.domain.usecase.AdminPermissionUseCase
import com.damoim.app.domain.usecase.BlockedUserUseCase
import com.damoim.app.domain.usecase.ClubSessionUseCase
import com.damoim.app.domain.usecase.NotifSettingsUseCase
import com.damoim.app.domain.usecase.SubscriptionUseCase
import com.damoim.app.domain.usecase.EventApplicationUseCase
import com.damoim.app.domain.usecase.GetMyApplicationsUseCase
import com.damoim.app.domain.usecase.GetScheduleDetailUseCase
import com.damoim.app.domain.usecase.GetSchedulesUseCase
import com.damoim.app.domain.usecase.ScheduleActionUseCase
import com.damoim.app.domain.usecase.SubmitScheduleUseCase
import com.damoim.app.domain.usecase.CohortActionUseCase
import com.damoim.app.domain.usecase.CreateClubUseCase
import com.damoim.app.domain.usecase.DecideApplicantUseCase
import com.damoim.app.domain.usecase.GetJoinedClubsUseCase
import com.damoim.app.domain.usecase.GetMemberDetailUseCase
import com.damoim.app.domain.usecase.GetMembersUseCase
import com.damoim.app.domain.usecase.GetMyMemberUseCase
import com.damoim.app.domain.usecase.MemberActionUseCase
import com.damoim.app.domain.usecase.DisableJoinCodeUseCase
import com.damoim.app.domain.usecase.EnterClubUseCase
import com.damoim.app.domain.usecase.GetAuthUserUseCase
import com.damoim.app.domain.usecase.GetBoardHomeUseCase
import com.damoim.app.domain.usecase.GetBoardPostsUseCase
import com.damoim.app.domain.usecase.GetClubInfoUseCase
import com.damoim.app.domain.usecase.GetCohortsUseCase
import com.damoim.app.domain.usecase.GetHomeSummaryUseCase
import com.damoim.app.domain.usecase.GetJoinApplicantsUseCase
import com.damoim.app.domain.usecase.GetNotificationsUseCase
import com.damoim.app.domain.usecase.GetPostDetailUseCase
import com.damoim.app.domain.usecase.GetResourceDetailUseCase
import com.damoim.app.domain.usecase.GetResourcesUseCase
import com.damoim.app.domain.usecase.GetSearchSuggestionsUseCase
import com.damoim.app.domain.usecase.GetStorageUsageUseCase
import com.damoim.app.domain.usecase.LoginWithKakaoUseCase
import com.damoim.app.domain.usecase.ManageRecentSearchUseCase
import com.damoim.app.domain.usecase.MarkNotificationsReadUseCase
import com.damoim.app.domain.usecase.ObserveMyContextUseCase
import com.damoim.app.domain.usecase.PostActionUseCase
import com.damoim.app.domain.usecase.RegenerateJoinCodeUseCase
import com.damoim.app.domain.usecase.ResourceActionUseCase
import com.damoim.app.domain.usecase.SearchBoardUseCase
import com.damoim.app.domain.usecase.SubmitJoinCodeUseCase
import com.damoim.app.domain.usecase.SubmitPostUseCase
import com.damoim.app.domain.usecase.UpdateProfileUseCase
import com.damoim.app.domain.usecase.UploadResourceUseCase

/**
 * 임시 수동 DI 컨테이너 (Service Locator). 서버·정식 DI(Koin) 도입 전까지 의존성을 조립한다.
 * Repository는 lazy 싱글턴(Mock — MockStore 위임), UseCase는 매 호출 생성(stateless).
 */
object AppGraph {

    private val authRepository: AuthRepository by lazy { MockAuthRepository() }
    private val clubRepository: ClubRepository by lazy { MockClubRepository() }
    private val notificationRepository: NotificationRepository by lazy { MockNotificationRepository() }
    private val boardRepository: BoardRepository by lazy { MockBoardRepository() }
    private val resourceRepository: ResourceRepository by lazy { MockResourceRepository() }
    private val scheduleRepository: ScheduleRepository by lazy { MockScheduleRepository() }
    private val settingsRepository: SettingsRepository by lazy { MockSettingsRepository() }

    // A. 인증·가입
    val loginWithKakaoUseCase get() = LoginWithKakaoUseCase(authRepository)
    val updateProfileUseCase get() = UpdateProfileUseCase(authRepository)
    val getAuthUserUseCase get() = GetAuthUserUseCase(authRepository)
    val submitJoinCodeUseCase get() = SubmitJoinCodeUseCase(clubRepository)
    val enterClubUseCase get() = EnterClubUseCase(clubRepository)

    // B. 홈·동아리 관리
    val createClubUseCase get() = CreateClubUseCase(clubRepository)
    val getHomeSummaryUseCase get() = GetHomeSummaryUseCase(clubRepository)
    val getClubInfoUseCase get() = GetClubInfoUseCase(clubRepository)
    val getCohortsUseCase get() = GetCohortsUseCase(clubRepository)
    val regenerateJoinCodeUseCase get() = RegenerateJoinCodeUseCase(clubRepository)
    val disableJoinCodeUseCase get() = DisableJoinCodeUseCase(clubRepository)
    val getJoinApplicantsUseCase get() = GetJoinApplicantsUseCase(clubRepository)
    val decideApplicantUseCase get() = DecideApplicantUseCase(clubRepository)
    val getNotificationsUseCase get() = GetNotificationsUseCase(notificationRepository)
    val markNotificationsReadUseCase get() = MarkNotificationsReadUseCase(notificationRepository)

    // E. 회원·기수 관리
    val getMembersUseCase get() = GetMembersUseCase(clubRepository)
    val getMemberDetailUseCase get() = GetMemberDetailUseCase(clubRepository)
    val getMyMemberUseCase get() = GetMyMemberUseCase(clubRepository)
    val getJoinedClubsUseCase get() = GetJoinedClubsUseCase(clubRepository)
    val memberActionUseCase get() = MemberActionUseCase(clubRepository)
    val cohortActionUseCase get() = CohortActionUseCase(clubRepository)
    val clubSessionUseCase get() = ClubSessionUseCase(clubRepository)

    // C. 게시판
    val getBoardHomeUseCase get() = GetBoardHomeUseCase(boardRepository)
    val getBoardPostsUseCase get() = GetBoardPostsUseCase(boardRepository)
    val getPostDetailUseCase get() = GetPostDetailUseCase(boardRepository)
    val postActionUseCase get() = PostActionUseCase(boardRepository)
    val submitPostUseCase get() = SubmitPostUseCase(boardRepository)
    val searchBoardUseCase get() = SearchBoardUseCase(boardRepository)
    val getSearchSuggestionsUseCase get() = GetSearchSuggestionsUseCase(boardRepository)
    val manageRecentSearchUseCase get() = ManageRecentSearchUseCase(boardRepository)

    // D. 자료실
    val getResourcesUseCase get() = GetResourcesUseCase(resourceRepository)
    val getResourceDetailUseCase get() = GetResourceDetailUseCase(resourceRepository)
    val getStorageUsageUseCase get() = GetStorageUsageUseCase(resourceRepository)
    val uploadResourceUseCase get() = UploadResourceUseCase(resourceRepository)
    val resourceActionUseCase get() = ResourceActionUseCase(resourceRepository)

    // F. 일정·이벤트
    val getSchedulesUseCase get() = GetSchedulesUseCase(scheduleRepository)
    val getScheduleDetailUseCase get() = GetScheduleDetailUseCase(scheduleRepository)
    val getMyApplicationsUseCase get() = GetMyApplicationsUseCase(scheduleRepository)
    val submitScheduleUseCase get() = SubmitScheduleUseCase(scheduleRepository)
    val scheduleActionUseCase get() = ScheduleActionUseCase(scheduleRepository)
    val eventApplicationUseCase get() = EventApplicationUseCase(scheduleRepository)

    // G. 설정·구독·권한
    val subscriptionUseCase get() = SubscriptionUseCase(settingsRepository)
    val adminPermissionUseCase get() = AdminPermissionUseCase(settingsRepository)
    val blockedUserUseCase get() = BlockedUserUseCase(settingsRepository)
    val notifSettingsUseCase get() = NotifSettingsUseCase(settingsRepository)

    // 공통 컨텍스트
    val observeMyContextUseCase get() = ObserveMyContextUseCase(authRepository, clubRepository)
}
