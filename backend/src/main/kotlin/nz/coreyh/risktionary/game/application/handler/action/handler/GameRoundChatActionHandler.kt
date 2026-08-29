package nz.coreyh.risktionary.game.application.handler.action.handler

import nz.coreyh.risktionary.game.application.handler.action.GameActionHandler
import nz.coreyh.risktionary.game.application.handler.action.dispatcher.GameRoundActionDispatcher
import nz.coreyh.risktionary.game.application.service.round.GameRoundSessionChatService
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.requireActiveRound
import nz.coreyh.risktionary.game.domain.model.action.GameRoundChatAction
import nz.coreyh.risktionary.game.domain.model.action.GameRoundPhaseGuessAction
import nz.coreyh.risktionary.game.domain.model.action.GameRoundPhaseGuessActionResult
import org.springframework.stereotype.Service

@Service
class GameRoundChatActionHandler(
    private val gameRoundSessionChatService: GameRoundSessionChatService,
    private val gameRoundActionDispatcher: GameRoundActionDispatcher,
    private val gameRoundPhaseGuessActionHandler: GameRoundPhaseGuessActionHandler,
) : GameActionHandler<GameRoundChatAction> {
    override val actionClass = GameRoundChatAction::class

    override fun handle(
        session: GameSession,
        player: GamePlayerSession,
        action: GameRoundChatAction,
    ) {
        val round = session.requireActiveRound()
        val text = action.text

        // try handle it as a guess
        val result =
            gameRoundActionDispatcher.submitIfPhaseMatches(
                round,
                gameRoundPhaseGuessActionHandler,
                player,
                GameRoundPhaseGuessAction(text),
            )
        if (result == GameRoundPhaseGuessActionResult.CONSUMED) return

        // otherwise treat it as just a chat message
        gameRoundSessionChatService.sendPlayerMessage(
            round = round,
            player = player,
            text = text,
        )
    }
}
