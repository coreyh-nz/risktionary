package nz.coreyh.risktionary.game.application.service.round

import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.application.store.GameSessionStore
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.round.createRoundId
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.words.domain.model.Word
import org.springframework.stereotype.Service

/**
 * Handles round-level operations within an active [GameSession].
 */
@Service
class GameRoundSessionService(
    private val gameSessionStore: GameSessionStore,
    private val gameEventPublisher: GameEventPublisher,
) {
    /**
     * Creates a new round for the given session, draws a word, and broadcasts
     * the initial round state to all clients.
     *
     * @param gameId the game session to create the round for.
     * @throws GameNotFoundException if the session does not exist.
     */
    fun createRound(
        gameId: GameId,
        word: Word,
    ) {
        val session = getSession(gameId)
        val roundId = createRoundId()
        val round = GameRoundSession(id = roundId, gameId = gameId, word = word)
        session.currentRound = round

        gameEventPublisher.publishRoundStateChanged(gameId, round.state)
    }

    private fun getSession(gameId: GameId): GameSession = gameSessionStore.findById(gameId) ?: throw GameNotFoundException()
}
