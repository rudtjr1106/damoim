package com.damoim.app.data.mock

import com.damoim.app.domain.model.AuthUser
import com.damoim.app.domain.model.Club
import com.damoim.app.domain.model.JoinRequestResult
import com.damoim.app.domain.model.JoinStatus

/**
 * 서버 연동 전 임시 목 데이터 저장소.
 *
 * 한 곳에 모아 두어 실제 API/DataSource로 교체할 때 이 파일만 삭제·대체하면 되도록 한다.
 * 가입 코드별로 결과를 다르게 돌려주어 화면 04(완료)/38(거절) 플로우를 모두 시연할 수 있다.
 */
object MockData {

    val kakaoUser = AuthUser(
        id = 1001L,
        nickname = "서연",
        email = "seoyeon@kakao.com",
        profileImageUrl = null,
        needsProfileSetup = true,
    )

    private val designClub = Club(
        id = 10L,
        name = "UMC 앱디자인 동아리",
        category = "IT · 개발",
        description = "매 기수 실제 앱을 기획부터 출시까지 함께 만드는 대학생 연합 동아리",
        memberCount = 28,
        emblemColor = 0xFF2F6DD3,
    )

    private val bandClub = Club(
        id = 20L,
        name = "소리풍경 밴드",
        category = "음악 · 공연",
        description = "학기말 정기공연을 목표로 모이는 직장인 밴드 동아리",
        memberCount = 15,
        emblemColor = 0xFF68B7ED,
    )

    /**
     * 코드 → 신청 결과 매핑.
     * - `DAMOIM` / 그 외 유효 코드: 정상 접수(PENDING) → 화면 04
     * - `REJECT`: 거절 결과(REJECTED) → 화면 38
     * - `EXPIRE`: 매핑 없음 → INVALID_CODE 실패
     */
    fun joinResultForCode(code: String): JoinRequestResult? = when (code.uppercase()) {
        "REJECT" -> JoinRequestResult(
            club = bandClub,
            status = JoinStatus.REJECTED,
            rejectionReason = "이번 기수 모집이 마감되었어요. 다음 모집 때 다시 신청해주세요.",
        )
        "EXPIRE" -> null
        else -> JoinRequestResult(
            club = designClub,
            status = JoinStatus.PENDING,
        )
    }
}
