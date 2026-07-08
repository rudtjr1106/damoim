package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.AuthUser
import com.damoim.app.domain.repository.AuthRepository

/** 카카오 로그인 (화면 02 → 동의하고 계속하기). */
class LoginWithKakaoUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): DataResult<AuthUser> = authRepository.loginWithKakao()
}
