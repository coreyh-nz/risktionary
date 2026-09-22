package nz.coreyh.risktionary.unit.game.domain.model.details

import io.kotest.matchers.shouldBe
import nz.coreyh.risktionary.ai.domain.AiUsage
import nz.coreyh.risktionary.ai.domain.AiUsagePurpose
import nz.coreyh.risktionary.game.domain.model.details.PersistedRound
import nz.coreyh.risktionary.game.domain.model.details.PersistedRoundDetails
import nz.coreyh.risktionary.game.domain.model.details.toAiUsageSummary
import nz.coreyh.risktionary.game.domain.model.round.GameRoundAiUsage
import nz.coreyh.risktionary.game.domain.model.round.RoundStateType
import nz.coreyh.risktionary.game.domain.model.round.createRoundId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerId
import org.junit.jupiter.api.Test
import kotlin.time.Clock

class PersistedGameDetailsTests {
    private fun usage(
        model: String,
        totalTokens: Int,
    ) = GameRoundAiUsage(
        usage = AiUsage(AiUsagePurpose.FACT_GENERATION, model, promptTokens = 0, completionTokens = 0, totalTokens = totalTokens),
        playerId = null,
        recordedAt = Clock.System.now(),
    )

    private fun testRound() =
        PersistedRound(
            id = createRoundId(),
            roundNumber = 1,
            wordId = null,
            wordValue = "word",
            drawerId = createTestGamePlayerId(),
            startedAt = Clock.System.now(),
            endedAt = Clock.System.now(),
            finalState = RoundStateType.COMPLETED,
            abandonedAiCalls = 0,
            drawerPoints = 0,
        )

    private fun roundWith(usage: List<GameRoundAiUsage>) =
        PersistedRoundDetails(
            round = testRound(),
            guesses = emptyList(),
            chatMessages = emptyList(),
            riskRatings = emptyList(),
            drawingAnalyses = emptyList(),
            feedback = emptyList(),
            aiUsage = usage,
        )

    @Test
    fun `totals are summed per model across every round`() {
        val rounds =
            listOf(
                roundWith(listOf(usage("gpt-4o", 100), usage("gpt-4o", 50))),
                roundWith(listOf(usage("gpt-4o", 25), usage("gemini-pro", 200))),
            )

        val summary = rounds.toAiUsageSummary()

        summary.totalTokensByModel shouldBe mapOf("gpt-4o" to 175, "gemini-pro" to 200)
        summary.totalTokens shouldBe 375
    }

    @Test
    fun `a game with no ai usage has an empty summary`() {
        val summary = listOf(roundWith(emptyList())).toAiUsageSummary()

        summary.totalTokensByModel shouldBe emptyMap()
        summary.totalTokens shouldBe 0
    }

    @Test
    fun `a game with no rounds has an empty summary`() {
        val summary = emptyList<PersistedRoundDetails>().toAiUsageSummary()

        summary.totalTokensByModel shouldBe emptyMap()
        summary.totalTokens shouldBe 0
    }
}
