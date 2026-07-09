package com.damoim.app.presentation.auth

import com.damoim.app.domain.model.Club

/**
 * A(인증·가입) 플로우의 화면 목적지. [AuthNavHost]가 백스택으로 관리한다.
 */
sealed interface AuthDestination {
    data object Login : AuthDestination              // 01 (로그인 수행 포함)
    data object ProfileSetup : AuthDestination       // 31
    data object Start : AuthDestination              // 32
    data object ClubCreate : AuthDestination         // 07 (동아리 생성)
    data object JoinCode : AuthDestination           // 03
    data class JoinComplete(val club: Club) : AuthDestination      // 04
    data class JoinRejected(val club: Club, val reason: String) : AuthDestination  // 38
}
