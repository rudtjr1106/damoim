package com.damoim.app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.ClubRole
import com.damoim.app.presentation.auth.AuthNavHost

/** 앱 루트 플로우: 인증(온보딩) ↔ 메인(홈). */
private sealed interface AppFlow {
    data object Auth : AppFlow
    data class Main(val role: ClubRole) : AppFlow
}

/**
 * 최상위 네비게이션. 온보딩 완료(동아리 생성/가입)로 메인 플로우에 진입하며,
 * 이때 스토어 세션이 시작되어 홈/게시판 데이터가 준비된다.
 * (세션/토큰이 붙으면 초기 진입 플로우를 로그인 상태로 결정하도록 확장)
 */
@Composable
fun RootNavHost() {
    var flow by remember { mutableStateOf<AppFlow>(AppFlow.Auth) }

    when (val current = flow) {
        AppFlow.Auth -> AuthNavHost(
            onEnterClub = { role ->
                AppGraph.enterClubUseCase(role)   // 코드 가입 경로 세션 시작(생성 경로는 이미 세션 있음)
                flow = AppFlow.Main(role)
            },
        )
        is AppFlow.Main -> MainNavHost(role = current.role)
    }
}
