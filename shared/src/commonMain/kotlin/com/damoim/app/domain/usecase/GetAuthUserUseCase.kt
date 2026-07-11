package com.damoim.app.domain.usecase

import com.damoim.app.domain.model.AuthUser
import com.damoim.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/** 현재 사용자 전체(닉네임·연락처·이메일) — 45 프로필 수정 프리필. */
class GetAuthUserUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<AuthUser> = authRepository.observeUser()
}
