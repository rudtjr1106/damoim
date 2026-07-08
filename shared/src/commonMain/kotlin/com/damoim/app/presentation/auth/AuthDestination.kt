package com.damoim.app.presentation.auth

import com.damoim.app.domain.model.Club

/**
 * A(인증·가입) 플로우의 화면 목적지.
 *
 * 정식 네비게이션(Navigation-Compose 등) 도입 전까지 [AuthNavHost]가 이 sealed 타입을
 * 백스택으로 관리한다. 데이터가 필요한 화면(완료/거절)은 파라미터를 직접 담는다.
 */
sealed interface AuthDestination {
    data object Login : AuthDestination              // 01 (로그인 수행 포함)
    data object ProfileSetup : AuthDestination       // 31
    data object Start : AuthDestination              // 32
    data object JoinCode : AuthDestination           // 03
    data class JoinComplete(val club: Club) : AuthDestination      // 04
    data class JoinRejected(val club: Club, val reason: String) : AuthDestination  // 38
}
