package com.damoim.app.domain.usecase

import com.damoim.app.domain.model.ClubRole
import com.damoim.app.domain.repository.AuthRepository
import com.damoim.app.domain.repository.ClubRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** 내 컨텍스트(사용자 id/이름 + 역할) 관찰 — 내 글 판정·운영진 기능 분기용. */
class ObserveMyContextUseCase(
    private val authRepository: AuthRepository,
    private val clubRepository: ClubRepository,
) {
    data class MyContext(val userId: Long, val name: String, val role: ClubRole?)

    operator fun invoke(): Flow<MyContext> =
        combine(authRepository.observeUser(), clubRepository.observeRole()) { user, role ->
            MyContext(user.id, user.nickname, role)
        }
}
