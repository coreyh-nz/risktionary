package nz.coreyh.risktionary.integration.game.web.controller

import io.kotest.matchers.shouldBe
import nz.coreyh.risktionary.ai.domain.AiUsage
import nz.coreyh.risktionary.ai.domain.AiUsagePurpose
import nz.coreyh.risktionary.feedback.domain.model.GamePlayerFeedbackAssignment
import nz.coreyh.risktionary.feedback.domain.model.GeneratedFeedback
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationStatus
import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisResult
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
import nz.coreyh.risktionary.game.domain.model.round.guess.GuessResultType
import nz.coreyh.risktionary.shared.web.support.Routes
import nz.coreyh.risktionary.support.annotation.IntegrationTest
import nz.coreyh.risktionary.support.creator.TestGameSessionCreator
import nz.coreyh.risktionary.support.creator.TestUserCreator
import nz.coreyh.risktionary.support.extensions.andBody
import nz.coreyh.risktionary.support.extensions.auth
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerSession
import nz.coreyh.risktionary.user.domain.model.UserRole
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper
import java.util.UUID
import kotlin.time.Clock

@IntegrationTest
@Transactional
@SpringBootTest
class GameDetailsControllerIntegrationTests(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val testUserCreator: TestUserCreator,
    private val testGameSessionCreator: TestGameSessionCreator,
    private val gameSessionService: GameSessionService,
    private val persistenceService: GameResearchPersistenceService,
) {
    private fun listUrl() = Routes.V1.Game.BASE

    private fun detailsUrl(gameId: String) = Routes.V1.Game.DETAILS.replace("{gameId}", gameId)

    private fun drawingUrl(
        gameId: String,
        analysisId: String,
    ) = Routes.V1.Game.DRAWING_IMAGE.replace("{gameId}", gameId).replace("{analysisId}", analysisId)

    @Test
    fun `given no authentication, when listing games, then returns unauthorized`() {
        mockMvc.get(listUrl()).andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `given an authenticated user without the researcher role, when listing games, then returns forbidden`() {
        val user = testUserCreator.createUniqueTestUser()

        mockMvc
            .get(listUrl()) {
                auth(user)
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    fun `given a researcher, when listing games, then returns a summary of every persisted game`() {
        val user = testUserCreator.createUniqueTestUser()
        val ongoing = testGameSessionCreator.createTestGameSession(host = user)
        val ended = testGameSessionCreator.createTestGameSession(host = user)
        persistenceService.markGameEnded(ended, GameEndReason.COMPLETED)

        mockMvc
            .get(listUrl()) {
                auth(user, roles = setOf(UserRole.RESEARCHER))
            }.andExpect {
                status { isOk() }
            }.andBody { body ->
                val json = objectMapper.readTree(body)
                val byId = json.associateBy { it["id"].asText() }
                byId.keys shouldBe setOf(ongoing.id.toString(), ended.id.toString())

                val endedJson = byId.getValue(ended.id.toString())
                endedJson["code"].asText() shouldBe ended.code
                endedJson["hostUserId"].asText() shouldBe user.id.toString()
                endedJson["feedbackGenerationMode"].asText() shouldBe ended.config.feedbackGenerationMode.name
                endedJson["endReason"].asText() shouldBe "COMPLETED"
                endedJson["endedAt"].isNull.shouldBe(false)

                val ongoingJson = byId.getValue(ongoing.id.toString())
                ongoingJson["endReason"].isNull.shouldBe(true)
                ongoingJson["endedAt"].isNull.shouldBe(true)
            }
    }

    @Test
    fun `given no authentication, when getting game details, then returns unauthorized`() {
        mockMvc.get(detailsUrl(UUID.randomUUID().toString())).andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `given an authenticated user without the researcher role, when getting game details, then returns forbidden`() {
        val user = testUserCreator.createUniqueTestUser()
        val game = testGameSessionCreator.createTestGameSession(host = user)

        mockMvc
            .get(detailsUrl(game.id.toString())) {
                auth(user)
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    fun `given a researcher, when getting details of a game that was never persisted, then returns not found`() {
        val user = testUserCreator.createUniqueTestUser()

        mockMvc
            .get(detailsUrl(UUID.randomUUID().toString())) {
                auth(user, roles = setOf(UserRole.RESEARCHER))
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `given a researcher, when getting details of a persisted game, then returns everything recorded about it`() {
        val user = testUserCreator.createUniqueTestUser()
        val game = testGameSessionCreator.createTestGameSession(host = user)

        val drawer = createTestGamePlayerSession(status = GamePlayerStatus.ACTIVE)
        val guesser = createTestGamePlayerSession(status = GamePlayerStatus.ACTIVE)
        listOf(drawer, guesser).forEach {
            game.requestJoin(it)
            game.feedback.assign(it.id) { GamePlayerFeedbackAssignment(FeedbackFramingCondition.NEUTRAL, FeedbackTimingCondition.INSTANT) }
        }
        val round = gameSessionService.createNextRound(game)
        round.selectDrawer(drawer)
        round.updatePhase(GameRoundPhase.Drawing(timeWindow = null))
        val guess = round.guesses.recordGuess(guesser.id, "wrong", GuessResultType.INCORRECT)
        round.riskRatings.submitRating(guesser.id, RiskLikelihood.LIKELY, RiskSeverity.MAJOR)
        round.drawing.record(byteArrayOf(1, 2, 3), "image/png", DrawingAnalysisResult.Fact("a fact"), Clock.System.now())
        round.aiUsage.record(AiUsage(AiUsagePurpose.DRAWING_ANALYSIS, "gpt-4o", promptTokens = 10, completionTokens = 5, totalTokens = 15), null)
        round.aiUsage.record(AiUsage(AiUsagePurpose.FACT_GENERATION, "gpt-4o", promptTokens = 20, completionTokens = 8, totalTokens = 28), guesser.id)
        round.feedback.record(
            GeneratedFeedback(
                id = createFeedbackId(),
                playerId = guesser.id,
                sourceGuessIds = listOf(guess.id),
                framingCondition = FeedbackFramingCondition.NEUTRAL,
                timingCondition = FeedbackTimingCondition.INSTANT,
                status = FeedbackGenerationStatus.SUCCESS,
                factText = "fact",
                framedText = "framed",
                generatedAt = Clock.System.now(),
            ),
        )
        persistenceService.persistRound(round)

        var analysisId: String? = null
        mockMvc
            .get(detailsUrl(game.id.toString())) {
                auth(user, roles = setOf(UserRole.RESEARCHER))
            }.andExpect {
                status { isOk() }
            }.andBody { body ->
                val json = objectMapper.readTree(body)
                json["game"]["code"].asText() shouldBe game.code
                json["players"].size() shouldBe 2
                json["rounds"].size() shouldBe 1
                json["aiUsageSummary"]["totalTokensByModel"]["gpt-4o"].asInt() shouldBe 43
                json["aiUsageSummary"]["totalTokens"].asInt() shouldBe 43

                val roundJson = json["rounds"][0]
                roundJson["round"]["roundNumber"].asInt() shouldBe 1
                roundJson["round"]["drawerId"].asText() shouldBe drawer.id.toString()
                roundJson["guesses"].size() shouldBe 1
                roundJson["guesses"][0]["text"].asText() shouldBe "wrong"
                roundJson["riskRatings"].size() shouldBe 1
                roundJson["feedback"].size() shouldBe 1
                roundJson["feedback"][0]["framedText"].asText() shouldBe "framed"

                val analysisJson = roundJson["drawingAnalyses"][0]
                analysisJson["resultType"].asText() shouldBe "FACT"
                analysisJson.has("image").shouldBe(false)
                analysisId = analysisJson["id"].asText()
            }

        mockMvc
            .get(drawingUrl(game.id.toString(), analysisId!!)) {
                auth(user, roles = setOf(UserRole.RESEARCHER))
            }.andExpect {
                status { isOk() }
                content { contentType("image/png") }
                content { bytes(byteArrayOf(1, 2, 3)) }
            }
    }

    @Test
    fun `given a researcher, when getting a drawing image that does not exist, then returns not found`() {
        val user = testUserCreator.createUniqueTestUser()
        val game = testGameSessionCreator.createTestGameSession(host = user)

        mockMvc
            .get(drawingUrl(game.id.toString(), UUID.randomUUID().toString())) {
                auth(user, roles = setOf(UserRole.RESEARCHER))
            }.andExpect {
                status { isNotFound() }
            }
    }
}
