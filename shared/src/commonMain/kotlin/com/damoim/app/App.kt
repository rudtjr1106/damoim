package com.damoim.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.damoim.app.presentation.auth.AuthNavHost
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 앱 진입 컴포저블. 현재는 A(인증·가입) 플로우만 연결되어 있다.
 * 이후 로그인 세션 유무에 따라 A → 홈(B) 등으로 분기하는 루트 그래프로 확장한다.
 */
@Composable
@Preview
fun App() {
    DamoimTheme {
        AuthNavHost()
    }
}
