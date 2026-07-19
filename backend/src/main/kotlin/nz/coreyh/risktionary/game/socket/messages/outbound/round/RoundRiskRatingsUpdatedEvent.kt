package nz.coreyh.risktionary.game.socket.messages.outbound.round

import nz.coreyh.risktionary.game.domain.model.risk.RiskRatingCount
import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEvent
import nz.coreyh.risktionary.game.socket.messages.event.round.RoundEventType

data class RoundRiskRatingsUpdatedEvent(
    val counts: List<RiskRatingCount>,
) : RoundEvent(RoundEventType.RISK_RATINGS_UPDATED)
