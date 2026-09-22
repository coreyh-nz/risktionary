package nz.coreyh.risktionary.game.domain.repository

import nz.coreyh.risktionary.feedback.domain.model.GamePlayerFeedbackAssignment
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.details.PersistedGamePlayer
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId

interface GamePlayerRepository {
    /**
     * Inserts any of [players] not already recorded for [gameId]. Existing
     * rows are left untouched, so this is safe to call repeatedly.
     *
     * A player's feedback assignment is null when feedback generation is
     * disabled for the game.
     */
    fun insertMissing(
        gameId: GameId,
        players: Map<GamePlayerId, GamePlayerFeedbackAssignment?>,
    )

    fun findByGameId(gameId: GameId): List<PersistedGamePlayer>
}
