package com.damoim.app.core.di

import com.damoim.app.data.repository.MockAuthRepository
import com.damoim.app.data.repository.MockBoardRepository
import com.damoim.app.data.repository.MockClubRepository
import com.damoim.app.data.repository.MockNotificationRepository
import com.damoim.app.data.repository.MockResourceRepository
import com.damoim.app.domain.repository.AuthRepository
import com.damoim.app.domain.repository.BoardRepository
import com.damoim.app.domain.repository.ClubRepository
import com.damoim.app.domain.repository.NotificationRepository
import com.damoim.app.domain.repository.ResourceRepository
import com.damoim.app.domain.usecase.CreateClubUseCase
import com.damoim.app.domain.usecase.DecideApplicantUseCase
import com.damoim.app.domain.usecase.DisableJoinCodeUseCase
import com.damoim.app.domain.usecase.EnterClubUseCase
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

    // A. 인증·가입
    val loginWithKakaoUseCase get() = LoginWithKakaoUseCase(authRepository)
    val updateProfileUseCase get() = UpdateProfileUseCase(authRepository)
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

    // 공통 컨텍스트
    val observeMyContextUseCase get() = ObserveMyContextUseCase(authRepository, clubRepository)
}
