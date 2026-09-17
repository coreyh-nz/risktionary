package nz.coreyh.risktionary.game.application.service.round

import nz.coreyh.risktionary.game.application.handler.state.orchestrator.GameStateOrchestrator
import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import org.springframework.stereotype.Service

@Service
class GameRoundSessionLifecycleService(
    private val gameSessionService: GameSessionService,
    private val gameStateOrchestrator: GameStateOrchestrator,
    private val gameEventPublisher: GameEventPublisher,
) {
    fun completeRound(round: GameRoundSession) {
        round.complete()

        val game = round.game
        if (game.words.hasNextWord()) {
            val newRound = gameSessionService.createNextRound(game)
            gameEventPublisher.publishRoundState(newRound)
        } else {
            gameStateOrchestrator.transitionToCompleted(game)
        }
    }
}
