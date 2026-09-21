package nz.coreyh.risktionary.game.domain.repository

import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.round.RoundId
import nz.coreyh.risktionary.game.domain.model.round.RoundStateType
import nz.coreyh.risktionary.words.domain.model.Word
import kotlin.time.Instant

interface GameRoundRepository {
    fun create(
        id: RoundId,
        gameId: GameId,
        roundNumber: Int,
        word: Word,
        drawerId: GamePlayerId,
        startedAt: Instant?,
        endedAt: Instant,
        finalState: RoundStateType,
        abandonedAiCalls: Int,
        drawerPoints: Int,
    )

    fun updateFinalState(
        id: RoundId,
        finalState: RoundStateType,
    )
}
