package com.damoim.app.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 47 공통 당겨서 새로고침 래퍼. 세로 스크롤 Column을 감싸고, 위로 당기면 [onRefresh]를 호출한다.
 * 새로고침 == 해당 화면 DataTopic 무효화(RemoteBus.invalidate) → reactiveFlow 재조회. 재조회는
 * fire-and-forget(완료 신호 없음)이라 스피너는 짧은 타임아웃으로 내린다(데이터는 flow로 갱신됨).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullRefreshColumn(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,                 // 바깥 영역(weight/fillMaxSize 등)
    columnModifier: Modifier = Modifier,           // 스크롤 컬럼 추가(padding 등). verticalScroll은 내부에서 적용.
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            scope.launch {
                refreshing = true
                onRefresh()
                delay(600)
                refreshing = false
            }
        },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).then(columnModifier),
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}
