package nz.coreyh.risktionary.game.infrastructure.persistence.table

import nz.coreyh.risktionary.game.domain.model.risk.RiskLikelihood
import nz.coreyh.risktionary.game.domain.model.risk.RiskSeverity
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

/** Append-only: a player has several rows if they ever re-rate. The highest [seq] per player is their final rating. */
object ExposedGameRoundRiskRatingTable : UUIDTable("risktionary_game_round_risk_rating") {
    val roundId = javaUUID("round_id").references(ExposedGameRoundTable.id, onDelete = ReferenceOption.CASCADE)
    val playerId = javaUUID("player_id").references(ExposedGamePlayerTable.id)
    val seq = integer("seq")
    val likelihood = enumerationByName<RiskLikelihood>("likelihood", 16)
    val severity = enumerationByName<RiskSeverity>("severity", 16)
    val ratedAt = timestamp("rated_at")
}
