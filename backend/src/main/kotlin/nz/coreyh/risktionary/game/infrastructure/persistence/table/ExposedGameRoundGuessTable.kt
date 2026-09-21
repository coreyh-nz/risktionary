package nz.coreyh.risktionary.game.infrastructure.persistence.table

import nz.coreyh.risktionary.game.domain.model.round.guess.GuessResultType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.timestamp

object ExposedGameRoundGuessTable : UUIDTable("risktionary_game_round_guess") {
    val roundId = javaUUID("round_id").references(ExposedGameRoundTable.id, onDelete = ReferenceOption.CASCADE)
    val playerId = javaUUID("player_id").references(ExposedGamePlayerTable.id)
    val seq = integer("seq")
    val guessText = text("guess_text")
    val guessResult = enumerationByName<GuessResultType>("guess_result", 16)
    val submittedAt = timestamp("submitted_at")
    val elapsedMs = long("elapsed_ms").nullable()
}
