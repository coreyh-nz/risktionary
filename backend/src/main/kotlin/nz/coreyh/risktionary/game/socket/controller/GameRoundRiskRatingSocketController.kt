package nz.coreyh.risktionary.game.socket.controller

import nz.coreyh.risktionary.game.application.handler.action.dispatcher.GameActionDispatcher
import nz.coreyh.risktionary.game.domain.model.risk.RiskLikelihood
import nz.coreyh.risktionary.game.domain.model.risk.RiskRating
import nz.coreyh.risktionary.game.domain.model.risk.RiskSeverity
import nz.coreyh.risktionary.game.socket.messages.inbound.round.RiskRatingCommand
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import nz.coreyh.risktionary.game.socket.support.WebSocketMappings
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class GameRoundRiskRatingSocketController(
    private val gameActionDispatcher: GameActionDispatcher,
) {
    @MessageMapping(WebSocketMappings.RISK_RATING)
    fun onRiskRating(
        principal: GameSocketPrincipal,
        command: RiskRatingCommand,
    ) {
        if (principal !is GameSocketPrincipal.Player) return

        val likelihood = RiskLikelihood.entries.firstOrNull { it.name == command.likelihood } ?: return
        val severity = RiskSeverity.entries.firstOrNull { it.name == command.severity } ?: return
        val riskRating = RiskRating(likelihood, severity)

        gameActionDispatcher.dispatch(
            gameId = principal.gameId,
            playerId = principal.id,
            action = riskRating,
        )
    }
}
