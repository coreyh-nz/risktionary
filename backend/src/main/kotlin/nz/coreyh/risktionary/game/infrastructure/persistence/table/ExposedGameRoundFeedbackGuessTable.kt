package nz.coreyh.risktionary.game.infrastructure.persistence.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

object ExposedGameRoundFeedbackGuessTable : Table("risktionary_game_round_feedback_guess") {
    val feedbackId = javaUUID("feedback_id").references(ExposedGameRoundFeedbackTable.id, onDelete = ReferenceOption.CASCADE)
    val guessId = javaUUID("guess_id").references(ExposedGameRoundGuessTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(feedbackId, guessId)
}
