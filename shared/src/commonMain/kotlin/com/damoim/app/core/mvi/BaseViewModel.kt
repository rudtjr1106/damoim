package com.damoim.app.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 화면 상태 마커. 각 화면은 자신의 immutable state를 정의한다. */
interface UiState

/** 일회성 효과(네비게이션·토스트 등). state와 달리 소비되면 사라진다. */
interface UiSideEffect

/**
 * MVI-lite 베이스 ViewModel. (UMC-Product `BaseViewModel` 패턴의 CMP 이식)
 *
 * - [uiState]: 화면이 구독하는 단일 상태 StateFlow
 * - [sideEffect]: 네비게이션·토스트 같은 일회성 이벤트 Flow
 * - [setState] / [sendEffect]: 하위 ViewModel용 protected 헬퍼
 * - [handleResult]: [DataResult] 성공/실패 공통 분기 (+ 로딩 종료)
 */
abstract class BaseViewModel<S : UiState, E : UiSideEffect>(
    initialState: S,
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _sideEffect = Channel<E>(Channel.BUFFERED)
    val sideEffect: Flow<E> = _sideEffect.receiveAsFlow()

    protected val currentState: S get() = _uiState.value

    protected fun setState(reducer: S.() -> S) {
        _uiState.update { it.reducer() }
    }

    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _sideEffect.send(effect) }
    }

    /** DataResult 공통 처리기. 성공/실패 콜백으로 분기한다. */
    protected fun <T> handleResult(
        result: DataResult<T>,
        onSuccess: (T) -> Unit,
        onFailure: (DataError) -> Unit = {},
    ) {
        when (result) {
            is DataResult.Success -> onSuccess(result.data)
            is DataResult.Failure -> onFailure(result.error)
        }
    }
}
