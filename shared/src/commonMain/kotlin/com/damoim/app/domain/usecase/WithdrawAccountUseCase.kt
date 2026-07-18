package com.damoim.app.domain.usecase

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.repository.AuthRepository

/** 51 회원 탈퇴 — 로그아웃/동아리 탈퇴와 구분. 서버 성공 시 세션이 정리된다. */
class WithdrawAccountUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): DataResult<Unit> = authRepository.withdraw()
}
