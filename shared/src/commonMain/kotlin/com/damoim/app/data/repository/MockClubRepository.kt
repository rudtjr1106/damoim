package com.damoim.app.data.repository

import com.damoim.app.core.result.DataError
import com.damoim.app.core.result.DataResult
import com.damoim.app.data.mock.MockData
import com.damoim.app.domain.model.JoinRequestResult
import com.damoim.app.domain.repository.ClubRepository
import kotlinx.coroutines.delay

/**
 * [ClubRepository]의 Mock 구현. 코드에 따라 접수/거절/무효를 돌려준다(MockData 참고).
 */
class MockClubRepository : ClubRepository {

    override suspend fun submitJoinCode(code: String): DataResult<JoinRequestResult> {
        delay(NETWORK_DELAY_MS)
        val result = MockData.joinResultForCode(code)
            ?: return DataResult.Failure(
                DataError(code = "INVALID_CODE", message = "존재하지 않거나 만료된 코드예요"),
            )
        return DataResult.Success(result)
    }

    private companion object {
        const val NETWORK_DELAY_MS = 700L
    }
}
