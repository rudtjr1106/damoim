package com.damoim.app.core.di

import com.damoim.app.data.repository.MockAuthRepository
import com.damoim.app.data.repository.MockClubRepository
import com.damoim.app.data.repository.MockNotificationRepository
import com.damoim.app.domain.repository.AuthRepository
import com.damoim.app.domain.repository.ClubRepository
import com.damoim.app.domain.repository.NotificationRepository
import com.damoim.app.domain.usecase.CreateClubUseCase
import com.damoim.app.domain.usecase.DecideApplicantUseCase
import com.damoim.app.domain.usecase.GetClubInfoUseCase
import com.damoim.app.domain.usecase.GetHomeSummaryUseCase
import com.damoim.app.domain.usecase.GetJoinApplicantsUseCase
import com.damoim.app.domain.usecase.GetNotificationsUseCase
import com.damoim.app.domain.usecase.LoginWithKakaoUseCase
import com.damoim.app.domain.usecase.RegenerateJoinCodeUseCase
import com.damoim.app.domain.usecase.SubmitJoinCodeUseCase
import com.damoim.app.domain.usecase.UpdateProfileUseCase

/**
 * 임시 수동 DI 컨테이너 (Service Locator). 서버·정식 DI(Koin) 도입 전까지 의존성을 조립한다.
 * Repository는 lazy 싱글턴(Mock), UseCase는 매 호출 생성(stateless).
 */
object AppGraph {

    private val authRepository: AuthRepository by lazy { MockAuthRepository() }
    private val clubRepository: ClubRepository by lazy { MockClubRepository() }
    private val notificationRepository: NotificationRepository by lazy { MockNotificationRepository() }

    // A. 인증·가입
    val loginWithKakaoUseCase get() = LoginWithKakaoUseCase(authRepository)
    val updateProfileUseCase get() = UpdateProfileUseCase(authRepository)
    val submitJoinCodeUseCase get() = SubmitJoinCodeUseCase(clubRepository)

    // B. 홈·동아리 관리
    val createClubUseCase get() = CreateClubUseCase(clubRepository)
    val getHomeSummaryUseCase get() = GetHomeSummaryUseCase(clubRepository)
    val getClubInfoUseCase get() = GetClubInfoUseCase(clubRepository)
    val regenerateJoinCodeUseCase get() = RegenerateJoinCodeUseCase(clubRepository)
    val getJoinApplicantsUseCase get() = GetJoinApplicantsUseCase(clubRepository)
    val decideApplicantUseCase get() = DecideApplicantUseCase(clubRepository)
    val getNotificationsUseCase get() = GetNotificationsUseCase(notificationRepository)
}
