package com.damoim.app.domain.usecase

import com.damoim.app.domain.repository.BoardHomeData
import com.damoim.app.domain.repository.BoardRepository
import kotlinx.coroutines.flow.Flow

/** 게시판 홈 관찰 (화면 10). 글 작성/고정/삭제가 실시간 반영된다. */
class GetBoardHomeUseCase(private val boardRepository: BoardRepository) {
    operator fun invoke(): Flow<BoardHomeData> = boardRepository.observeBoardHome()
}
