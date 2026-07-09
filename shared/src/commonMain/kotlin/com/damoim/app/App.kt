package com.damoim.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.damoim.app.presentation.RootNavHost
import com.damoim.app.presentation.theme.DamoimTheme

/**
 * 앱 진입 컴포저블. 인증(온보딩) ↔ 메인(홈) 플로우를 [RootNavHost]가 전환한다.
 */
@Composable
@Preview
fun App() {
    DamoimTheme {
        RootNavHost()
    }
}
