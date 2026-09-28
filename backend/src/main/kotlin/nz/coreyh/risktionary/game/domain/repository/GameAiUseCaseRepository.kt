package nz.coreyh.risktionary.game.domain.repository

import nz.coreyh.risktionary.ai.domain.AiUseCaseSetup
import nz.coreyh.risktionary.game.domain.model.GameId

interface GameAiUseCaseRepository {
    fun insertAll(
        gameId: GameId,
        useCases: List<AiUseCaseSetup>,
    )
}
