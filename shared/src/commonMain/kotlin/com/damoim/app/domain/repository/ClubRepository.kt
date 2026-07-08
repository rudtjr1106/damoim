package com.damoim.app.domain.repository

import com.damoim.app.core.result.DataResult
import com.damoim.app.domain.model.JoinRequestResult

/**
 * 동아리 가입 레포지토리. 구현체는 data 계층에 있으며 현재는 Mock.
 */
interface ClubRepository {

    /**
     * 가입 코드 제출. 유효한 코드면 신청이 접수되고 [JoinRequestResult]를 반환한다.
     * 코드가 없거나 잘못되면 실패(DataError.code = "INVALID_CODE").
     */
    suspend fun submitJoinCode(code: String): DataResult<JoinRequestResult>
}
