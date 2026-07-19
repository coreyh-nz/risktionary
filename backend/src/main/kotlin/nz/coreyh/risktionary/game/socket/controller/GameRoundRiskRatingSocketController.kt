package nz.coreyh.risktionary.game.socket.controller

import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.domain.model.risk.RiskLikelihood
import nz.coreyh.risktionary.game.domain.model.risk.RiskRating
import nz.coreyh.risktionary.game.domain.model.risk.RiskSeverity
import nz.coreyh.risktionary.game.socket.messages.inbound.round.RiskRatingCommand
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class GameRoundRiskRatingSocketController(
    private val gameSessionService: GameSessionService,
) {
    @MessageMapping("/game/risk-rating")
    fun onRiskRating(
        principal: GameSocketPrincipal,
        command: RiskRatingCommand,
    ) {
        if (principal !is GameSocketPrincipal.Player) return

        val likelihood = RiskLikelihood.entries.firstOrNull { it.name == command.likelihood } ?: return
        val severity = RiskSeverity.entries.firstOrNull { it.name == command.severity } ?: return
        val riskRating = RiskRating(likelihood, severity)

        gameSessionService.handleRiskRating(
            gameId = principal.gameId,
            playerId = principal.id,
            rating = riskRating,
        )
    }
}
