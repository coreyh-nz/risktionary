package nz.coreyh.risktionary.game.application.handler.action.handler

import nz.coreyh.risktionary.game.application.handler.action.GameRoundPhaseActionHandler
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.game.domain.model.action.GameRoundPhaseRiskRatingAction
import nz.coreyh.risktionary.game.socket.messages.GameEventPublisher
import org.springframework.stereotype.Service

@Service
class GameRoundPhaseRiskRatingActionHandler(
    private val gameEventPublisher: GameEventPublisher,
) : GameRoundPhaseActionHandler<GameRoundPhase.Ranking, GameRoundPhaseRiskRatingAction, Unit> {
    override val phaseClass = GameRoundPhase.Ranking::class
    override val actionClass = GameRoundPhaseRiskRatingAction::class

    override fun handle(
        round: GameRoundSession,
        phase: GameRoundPhase.Ranking,
        player: GamePlayerSession,
        action: GameRoundPhaseRiskRatingAction,
    ) {
        val rating = action.rating
        val riskRatings = round.riskRatings
        riskRatings.submitRating(player.id, rating.likelihood, rating.severity)

        val counts = riskRatings.counts()
        gameEventPublisher.publishRiskRatingsUpdatedToHost(round.game.host.id, counts)
        round.game
            .getActivePlayers()
            .filter { riskRatings.hasRated(it.id) }
            .forEach { gameEventPublisher.publishRiskRatingsUpdatedToPlayer(it.id, counts) }
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
