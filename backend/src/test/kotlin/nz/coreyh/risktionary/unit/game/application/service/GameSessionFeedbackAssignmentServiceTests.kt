package nz.coreyh.risktionary.unit.game.application.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import nz.coreyh.risktionary.feedback.domain.model.GamePlayerFeedbackAssignment
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition
import nz.coreyh.risktionary.game.application.service.GameSessionFeedbackAssignmentService
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.domain.model.player.createPlayerId
import nz.coreyh.risktionary.support.annotation.MockKTest
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayer
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerSession
import nz.coreyh.risktionary.support.factory.game.createTestGameSession
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

@MockKTest
class GameSessionFeedbackAssignmentServiceTests {
    private lateinit var service: GameSessionFeedbackAssignmentService
    private lateinit var game: GameSession

    @BeforeEach
    fun setup() {
        service = GameSessionFeedbackAssignmentService()
        game = createTestGameSession()
    }

    @Test
    fun `assigns a new player to one of the valid combinations`() {
        val player = createTestGamePlayerSession()

        service.assign(game, player)

        val assignment = shouldNotThrowAny { game.feedback.assignmentFor(player.id) }
        val validCombinations =
            FeedbackFramingCondition.entries.flatMap { framing ->
                FeedbackTimingCondition.entries.map { timing -> GamePlayerFeedbackAssignment(framing, timing) }
            }
        validCombinations shouldContain assignment
    }

    @Test
    fun `returns the same assignment on repeated calls for the same player`() {
        val player = createTestGamePlayerSession()

        service.assign(game, player)
        val first = game.feedback.assignmentFor(player.id)

        service.assign(game, player)
        val second = game.feedback.assignmentFor(player.id)

        second shouldBe first
    }

    @Test
    fun `does not change an existing player's assignment when other players join afterwards`() {
        val player = createTestGamePlayerSession()
        service.assign(game, player)
        val original = game.feedback.assignmentFor(player.id)

        repeat(20) { service.assign(game, createTestGamePlayerSession()) }

        game.feedback.assignmentFor(player.id) shouldBe original
    }

    @Test
    fun `distributes players exactly evenly across all combinations when player count is a multiple of the combination count`() {
        val totalCombinations = FeedbackFramingCondition.entries.size * FeedbackTimingCondition.entries.size
        val playersPerCombination = 25
        val totalPlayers = totalCombinations * playersPerCombination
        val playerIds = (1..totalPlayers).map { createPlayerId() }

        playerIds.forEach { id ->
            service.assign(
                game,
                createTestGamePlayerSession(player = createTestGamePlayer(id = id)),
            )
        }

        val counts =
            playerIds
                .map { game.feedback.assignmentFor(it) }
                .groupingBy { it }
                .eachCount()
        counts.size shouldBe totalCombinations
        counts.values.forEach { count -> count shouldBe playersPerCombination }
    }

    @Test
    fun `never lets any combination's count fall more than one behind another`() {
        val totalCombinations = FeedbackFramingCondition.entries.size * FeedbackTimingCondition.entries.size
        val totalPlayers = totalCombinations * 10 + 3 // not a multiple, to test mid-distribution balance

        val playerIds = (1..totalPlayers).map { createPlayerId() }
        playerIds.forEach { id ->
            service.assign(
                game,
                createTestGamePlayerSession(player = createTestGamePlayer(id = id)),
            )
        }

        val counts =
            playerIds
                .map { game.feedback.assignmentFor(it) }
                .groupingBy { it }
                .eachCount()

        val max = counts.values.max()
        val min = counts.values.min()
        (max - min) shouldBeLessThanOrEqual 1
    }
}
