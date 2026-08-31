package nz.coreyh.risktionary.game.application.service.round

import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.domain.model.round.createRoundId
import nz.coreyh.risktionary.words.domain.model.Word
import org.springframework.stereotype.Service

/**
 * Handles round-level operations within an active [GameSession].
 */
@Service
class GameRoundSessionService {
    fun createRound(
        game: GameSession,
        word: Word,
    ): GameRoundSession {
        val roundId = createRoundId()
        val round = GameRoundSession(id = roundId, game = game, word = word)
        return round
    }
}
