package nz.coreyh.risktionary.game.application.service

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.feedback.domain.model.GamePlayerFeedbackAssignment
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition
import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.application.session.GameSession
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class GameSessionFeedbackAssignmentService {
    private val allCombinations: List<GamePlayerFeedbackAssignment> =
        FeedbackFramingCondition.entries.flatMap { framing ->
            FeedbackTimingCondition.entries.map { timing ->
                GamePlayerFeedbackAssignment(framing, timing)
            }
        }

    fun assign(
        game: GameSession,
        player: GamePlayerSession,
    ) {
        var newAssignment = false
        val assignment =
            game.feedback.assign(player.id) { existingAssignments ->
                newAssignment = true
                leastAssignedCombination(existingAssignments)
            }

        logger.debug {
            when (newAssignment) {
                true -> {
                    "Assigned player ${player.id} in game ${game.id} to " +
                        "framing=${assignment.framingCondition}, " +
                        "timing=${assignment.timingCondition}"
                }

                false -> {
                    "Player ${player.id} in game ${game.id} is already assigned to " +
                        "framing=${assignment.framingCondition}, " +
                        "timing=${assignment.timingCondition}"
                }
            }
        }
    }

    private fun leastAssignedCombination(existingAssignments: Collection<GamePlayerFeedbackAssignment>): GamePlayerFeedbackAssignment {
        val counts = existingAssignments.groupingBy { it }.eachCount()
        val minCount = allCombinations.minOf { counts[it] ?: 0 }
        return allCombinations.filter { (counts[it] ?: 0) == minCount }.random()
    }
}
