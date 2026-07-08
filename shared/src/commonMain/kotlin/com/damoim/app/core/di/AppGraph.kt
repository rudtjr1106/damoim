package com.damoim.app.core.di

import com.damoim.app.data.repository.MockAuthRepository
import com.damoim.app.data.repository.MockClubRepository
import com.damoim.app.domain.repository.AuthRepository
import com.damoim.app.domain.repository.ClubRepository
import com.damoim.app.domain.usecase.LoginWithKakaoUseCase
import com.damoim.app.domain.usecase.SubmitJoinCodeUseCase
import com.damoim.app.domain.usecase.UpdateProfileUseCase

/**
 * 임시 수동 DI 컨테이너 (Service Locator).
 *
 * 서버와 정식 DI(Koin 등)가 붙기 전까지 의존성을 한 곳에서 조립한다.
 * - Repository는 lazy 싱글턴 (지금은 Mock 구현)
 * - UseCase는 매 호출마다 생성 (stateless라 무해)
 *
 * ViewModel은 이 UseCase들을 생성자 주입으로 받으므로, 나중에 Koin으로 옮길 때
 * ViewModel/UseCase/Repository 코드는 그대로 두고 이 파일만 module 정의로 대체하면 된다.
 */
object AppGraph {

    private val authRepository: AuthRepository by lazy { MockAuthRepository() }
    private val clubRepository: ClubRepository by lazy { MockClubRepository() }

    val loginWithKakaoUseCase: LoginWithKakaoUseCase
        get() = LoginWithKakaoUseCase(authRepository)

    val updateProfileUseCase: UpdateProfileUseCase
        get() = UpdateProfileUseCase(authRepository)

    val submitJoinCodeUseCase: SubmitJoinCodeUseCase
        get() = SubmitJoinCodeUseCase(clubRepository)
}
