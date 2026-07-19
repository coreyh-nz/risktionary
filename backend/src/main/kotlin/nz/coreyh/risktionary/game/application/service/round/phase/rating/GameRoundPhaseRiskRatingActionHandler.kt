package nz.coreyh.risktionary.game.application.service.round.phase.rating

import nz.coreyh.risktionary.game.application.service.round.phase.GameRoundPhaseActionHandler
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.domain.model.risk.PlayerRiskRating
import nz.coreyh.risktionary.game.domain.model.risk.RiskRating
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import org.springframework.stereotype.Service

/**
 * Handles a player's risk rating (likelihood + severity) submitted during
 * the [GameRoundPhase.Ranking] phase.
 *
 * The phase is complete once every eligible player (all players except the
 * drawer) has submitted a rating.
 */
@Service
class GameRoundPhaseRiskRatingActionHandler(
    private val gameEventPublisher: GameEventPublisher,
) : GameRoundPhaseActionHandler<GameRoundPhase.Ranking, RiskRating, PlayerRiskRating> {
    override val phaseClass = GameRoundPhase.Ranking::class

    override fun handle(
        round: GameRoundSession,
        phase: GameRoundPhase.Ranking,
        player: GamePlayerSession,
        action: RiskRating,
    ): PlayerRiskRating {
        val riskRatings = round.riskRatings
        val playerRating = riskRatings.submitRating(player.id, action.likelihood, action.severity)
        val counts = riskRatings.counts()

        // resend all counts to host and players in case somehow the player changed their vote.
        // better than sending individual risk rating as the client doesn't track which player
        // voted for which combination which would cause incorrect counts on the client side
        gameEventPublisher.publishRiskRatingsUpdatedToHost(round.game.host.id, counts)
        round.game
            .getActivePlayers()
            .filter { riskRatings.hasRated(it.id) }
            .forEach { gameEventPublisher.publishRiskRatingsUpdatedToPlayer(it.id, counts) }

        return playerRating
    }

    override fun isPhaseComplete(
        session: GameSession,
        round: GameRoundSession,
        phase: GameRoundPhase.Ranking,
    ): Boolean {
        val eligiblePlayerIds = session.getActivePlayers().map { it.id }
        val submittedPlayerIds = round.riskRatings.getRatings().map { it.playerId }
        return submittedPlayerIds.containsAll(eligiblePlayerIds)
    }
}
