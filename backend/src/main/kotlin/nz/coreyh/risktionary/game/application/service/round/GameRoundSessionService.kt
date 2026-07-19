package nz.coreyh.risktionary.game.application.service.round

import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.domain.model.round.createRoundId
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import nz.coreyh.risktionary.words.domain.model.Word
import org.springframework.stereotype.Service

/**
 * Handles round-level operations within an active [GameSession].
 */
@Service
class GameRoundSessionService(
    private val gameEventPublisher: GameEventPublisher,
) {
    fun createRound(
        game: GameSession,
        word: Word,
    ): GameRoundSession {
        val roundId = createRoundId()
        val round = GameRoundSession(id = roundId, game = game, word = word)
        return round
    }

    fun completeRound(round: GameRoundSession) {
        round.complete()
        gameEventPublisher.publishRoundState(round)
    }
}
