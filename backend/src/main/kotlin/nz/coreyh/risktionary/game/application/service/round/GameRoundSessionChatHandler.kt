package nz.coreyh.risktionary.game.application.service.round

import nz.coreyh.risktionary.game.application.service.round.phase.drawing.GameRoundPhaseGuessActionHandler
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.requireActiveRound
import org.springframework.stereotype.Service

@Service
class GameRoundSessionChatHandler(
    private val gameRoundSessionChatService: GameRoundSessionChatService,
    private val gameRoundActionCoordinator: GameRoundActionCoordinator,
    private val gameRoundPhaseGuessActionHandler: GameRoundPhaseGuessActionHandler,
) {
    fun handleChat(
        session: GameSession,
        player: GamePlayerSession,
        text: String,
    ) {
        val round = session.requireActiveRound()

        // handle potential guess
        val guessAttempt = gameRoundActionCoordinator.submit(round, gameRoundPhaseGuessActionHandler, player, text)
        if (guessAttempt) {
            return
        }

        gameRoundSessionChatService.sendPlayerMessage(
            round = round,
            player = player,
            text = text,
        )
    }
}
