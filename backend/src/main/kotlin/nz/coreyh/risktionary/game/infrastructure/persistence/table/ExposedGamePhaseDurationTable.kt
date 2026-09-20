package nz.coreyh.risktionary.game.infrastructure.persistence.table

import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

object ExposedGamePhaseDurationTable : Table("risktionary_game_phase_duration") {
    val gameId = javaUUID("game_id").references(ExposedGameTable.id, onDelete = ReferenceOption.CASCADE)
    val phase = enumerationByName<RoundPhaseType>("phase", 32)
    val durationMs = long("duration_ms")

    override val primaryKey = PrimaryKey(gameId, phase)
}
