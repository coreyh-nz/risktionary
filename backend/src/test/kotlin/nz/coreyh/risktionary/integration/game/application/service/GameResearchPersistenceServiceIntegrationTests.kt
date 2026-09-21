package nz.coreyh.risktionary.integration.game.application.service

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nz.coreyh.risktionary.ai.domain.AiUsage
import nz.coreyh.risktionary.ai.domain.AiUsagePurpose
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationStatus
import nz.coreyh.risktionary.feedback.domain.model.GamePlayerFeedbackAssignment
import nz.coreyh.risktionary.feedback.domain.model.GeneratedFeedback
import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisResult
import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisResultType
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackTimingCondition
import nz.coreyh.risktionary.feedback.domain.model.createFeedbackId
import nz.coreyh.risktionary.game.application.service.GameResearchPersistenceService
import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.application.session.round.GameRoundPhase
import nz.coreyh.risktionary.game.domain.model.GameEndReason
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerStatus
import nz.coreyh.risktionary.game.domain.model.risk.RiskLikelihood
import nz.coreyh.risktionary.game.domain.model.risk.RiskSeverity
import nz.coreyh.risktionary.game.domain.model.round.RoundStateType
import nz.coreyh.risktionary.game.domain.model.round.chat.ChatMessage
import nz.coreyh.risktionary.game.domain.model.round.guess.GuessResultType
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGamePlayerTable
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundAiUsageTable
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundChatMessageTable
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundDrawingAnalysisTable
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundFeedbackGuessTable
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundFeedbackTable
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundGuessTable
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundRiskRatingTable
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameRoundTable
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameTable
import nz.coreyh.risktionary.game.infrastructure.persistence.table.ExposedGameWordTable
import nz.coreyh.risktionary.game.socket.messages.view.toView
import nz.coreyh.risktionary.support.annotation.IntegrationTest
import nz.coreyh.risktionary.support.creator.TestGameSessionCreator
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerSession
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.time.Clock

@IntegrationTest
@Transactional
@SpringBootTest
class GameResearchPersistenceServiceIntegrationTests(
    private val persistenceService: GameResearchPersistenceService,
    private val gameSessionService: GameSessionService,
    private val testGameSessionCreator: TestGameSessionCreator,
) {
    @Test
    fun `creating a game session persists the game and its words`() {
        val game = testGameSessionCreator.createTestGameSession()

        transaction {
            val row = ExposedGameTable.selectAll().where { ExposedGameTable.id eq game.id.value }.single()
            row[ExposedGameTable.code] shouldBe game.code
            row[ExposedGameTable.hostUserId] shouldBe game.host.id.value
            ExposedGameWordTable
                .selectAll()
                .where { ExposedGameWordTable.gameId eq game.id.value }
                .toList() shouldHaveSize game.config.words.size
        }
    }

    @Test
    fun `scoring is persisted with the game, each guess and the drawer`() {
        val game = testGameSessionCreator.createTestGameSession()
        val drawer = createTestGamePlayerSession(status = GamePlayerStatus.ACTIVE)
        val guesser = createTestGamePlayerSession(status = GamePlayerStatus.ACTIVE)
        listOf(drawer, guesser).forEach {
            game.requestJoin(it)
            game.feedback.assign(it.id) { GamePlayerFeedbackAssignment(FeedbackFramingCondition.NEUTRAL, FeedbackTimingCondition.INSTANT) }
        }
        val round = gameSessionService.createNextRound(game)
        round.selectDrawer(drawer)
        round.updatePhase(GameRoundPhase.Drawing(timeWindow = null))
        round.guesses.recordGuess(guesser.id, "wrong", GuessResultType.INCORRECT)
        round.guesses.recordGuess(guesser.id, "right", GuessResultType.CORRECT) { 640 }
        game.scoreboard.recordCorrectGuess(game.roundNumber, drawer.id, guesser.id, 640)

        persistenceService.persistRound(round)

        transaction {
            val gameRow = ExposedGameTable.selectAll().where { ExposedGameTable.id eq game.id.value }.single()
            gameRow[ExposedGameTable.scoringMaxPoints] shouldBe game.config.scoring.maxPoints
            gameRow[ExposedGameTable.scoringMinPoints] shouldBe game.config.scoring.minPoints
            gameRow[ExposedGameTable.scoringUntimedWindowMs] shouldBe game.config.scoring.untimedReferenceWindow.inWholeMilliseconds

            ExposedGameRoundTable
                .selectAll()
                .where { ExposedGameRoundTable.id eq round.id.value }
                .single()[ExposedGameRoundTable.drawerPoints] shouldBe 640

            ExposedGameRoundGuessTable
                .selectAll()
                .orderBy(ExposedGameRoundGuessTable.seq)
                .map { it[ExposedGameRoundGuessTable.points] } shouldBe listOf(null, 640)
        }
    }

    @Test
    fun `persisting a round records everything that happened in it`() {
        val game = testGameSessionCreator.createTestGameSession()
        val drawer = createTestGamePlayerSession(status = GamePlayerStatus.ACTIVE)
        val guesser = createTestGamePlayerSession(status = GamePlayerStatus.ACTIVE)
        listOf(drawer, guesser).forEach {
            game.requestJoin(it)
            game.feedback.assign(
                it.id,
            ) { GamePlayerFeedbackAssignment(FeedbackFramingCondition.CORRECTIVE, FeedbackTimingCondition.DELAYED) }
        }

        val round = gameSessionService.createNextRound(game)
        round.selectDrawer(drawer)
        round.updatePhase(GameRoundPhase.Drawing(timeWindow = null))

        val guess = round.guesses.recordGuess(guesser.id, "wrong", GuessResultType.INCORRECT)
        round.addMessage(ChatMessage.Player(guesser.player.toView(), "wrong"), guess.id)
        round.riskRatings.submitRating(guesser.id, RiskLikelihood.LIKELY, RiskSeverity.MAJOR)
        round.riskRatings.submitRating(guesser.id, RiskLikelihood.RARE, RiskSeverity.MINOR)

        val image = byteArrayOf(1, 2, 3)
        round.drawing.record(image, "image/png", DrawingAnalysisResult.Fact("a fact"), Clock.System.now())
        round.drawing.record(image, "image/png", DrawingAnalysisResult.NoFact, Clock.System.now())
        round.aiUsage.record(AiUsage(AiUsagePurpose.FACT_GENERATION, "test-model", 10, 5, 15), guesser.id)
        round.aiUsage.record(AiUsage(AiUsagePurpose.DRAWING_ANALYSIS, "test-model", 20, 8, 28))
        round.feedback.record(
            GeneratedFeedback(
                id = createFeedbackId(),
                playerId = guesser.id,
                sourceGuessIds = listOf(guess.id),
                framingCondition = FeedbackFramingCondition.CORRECTIVE,
                timingCondition = FeedbackTimingCondition.DELAYED,
                status = FeedbackGenerationStatus.SUCCESS,
                factText = "fact",
                framedText = "framed",
                generatedAt = Clock.System.now(),
            ),
        )

        persistenceService.persistRound(round, abandonedAiCalls = 2)
        persistenceService.markRoundCompleted(round)
        persistenceService.markGameEnded(game, GameEndReason.COMPLETED)

        transaction {
            val roundRow = ExposedGameRoundTable.selectAll().where { ExposedGameRoundTable.id eq round.id.value }.single()
            roundRow[ExposedGameRoundTable.drawerId] shouldBe drawer.id.value
            roundRow[ExposedGameRoundTable.roundNumber] shouldBe 1
            roundRow[ExposedGameRoundTable.startedAt].shouldNotBeNull()
            roundRow[ExposedGameRoundTable.finalState] shouldBe RoundStateType.COMPLETED
            roundRow[ExposedGameRoundTable.abandonedAiCalls] shouldBe 2

            ExposedGamePlayerTable
                .selectAll()
                .where { ExposedGamePlayerTable.gameId eq game.id.value }
                .toList() shouldHaveSize 2

            val guessRow = ExposedGameRoundGuessTable.selectAll().single()
            guessRow[ExposedGameRoundGuessTable.guessText] shouldBe "wrong"
            guessRow[ExposedGameRoundGuessTable.elapsedMs].shouldNotBeNull()

            ExposedGameRoundChatMessageTable.selectAll().single()[ExposedGameRoundChatMessageTable.guessId] shouldBe guess.id.value

            // re-ratings are kept, with the latest having the highest seq
            ExposedGameRoundRiskRatingTable
                .selectAll()
                .orderBy(ExposedGameRoundRiskRatingTable.seq)
                .map { it[ExposedGameRoundRiskRatingTable.likelihood] } shouldBe listOf(RiskLikelihood.LIKELY, RiskLikelihood.RARE)

            val analyses = ExposedGameRoundDrawingAnalysisTable.selectAll().orderBy(ExposedGameRoundDrawingAnalysisTable.seq).toList()
            analyses.map { it[ExposedGameRoundDrawingAnalysisTable.resultType] } shouldBe
                listOf(DrawingAnalysisResultType.FACT, DrawingAnalysisResultType.NO_FACT)
            analyses.first()[ExposedGameRoundDrawingAnalysisTable.image].toList() shouldBe image.toList()

            ExposedGameRoundFeedbackTable.selectAll().single()[ExposedGameRoundFeedbackTable.framedText] shouldBe "framed"
            ExposedGameRoundFeedbackGuessTable.selectAll().single()[ExposedGameRoundFeedbackGuessTable.guessId] shouldBe guess.id.value

            val usage = ExposedGameRoundAiUsageTable.selectAll().orderBy(ExposedGameRoundAiUsageTable.seq).toList()
            usage.map { it[ExposedGameRoundAiUsageTable.usagePurpose] } shouldBe
                listOf(AiUsagePurpose.FACT_GENERATION, AiUsagePurpose.DRAWING_ANALYSIS)
            usage.first()[ExposedGameRoundAiUsageTable.playerId] shouldBe guesser.id.value
            usage.first()[ExposedGameRoundAiUsageTable.totalTokens] shouldBe 15
            usage.last()[ExposedGameRoundAiUsageTable.playerId] shouldBe null

            ExposedGameTable
                .selectAll()
                .where { ExposedGameTable.id eq game.id.value }
                .single()[ExposedGameTable.endReason] shouldBe GameEndReason.COMPLETED
        }
    }
}
