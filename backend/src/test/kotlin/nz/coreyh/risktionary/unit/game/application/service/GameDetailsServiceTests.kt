package nz.coreyh.risktionary.unit.game.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationMode
import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.application.service.GameDetailsService
import nz.coreyh.risktionary.game.domain.model.details.PersistedDrawingImage
import nz.coreyh.risktionary.game.domain.model.details.PersistedGameSummary
import nz.coreyh.risktionary.game.domain.repository.GamePlayerRepository
import nz.coreyh.risktionary.game.domain.repository.GameRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundAiUsageRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundChatMessageRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundDrawingAnalysisRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundFeedbackRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundGuessRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundRepository
import nz.coreyh.risktionary.game.domain.repository.GameRoundRiskRatingRepository
import nz.coreyh.risktionary.support.factory.game.createTestGameId
import nz.coreyh.risktionary.support.factory.user.createTestUserId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.time.Clock

class GameDetailsServiceTests {
    private lateinit var gameRepository: GameRepository
    private lateinit var gamePlayerRepository: GamePlayerRepository
    private lateinit var gameRoundRepository: GameRoundRepository
    private lateinit var gameRoundGuessRepository: GameRoundGuessRepository
    private lateinit var gameRoundChatMessageRepository: GameRoundChatMessageRepository
    private lateinit var gameRoundRiskRatingRepository: GameRoundRiskRatingRepository
    private lateinit var gameRoundDrawingAnalysisRepository: GameRoundDrawingAnalysisRepository
    private lateinit var gameRoundFeedbackRepository: GameRoundFeedbackRepository
    private lateinit var gameRoundAiUsageRepository: GameRoundAiUsageRepository
    private lateinit var service: GameDetailsService

    @BeforeEach
    fun setup() {
        gameRepository = mockk()
        gamePlayerRepository = mockk(relaxed = true)
        gameRoundRepository = mockk(relaxed = true)
        gameRoundGuessRepository = mockk(relaxed = true)
        gameRoundChatMessageRepository = mockk(relaxed = true)
        gameRoundRiskRatingRepository = mockk(relaxed = true)
        gameRoundDrawingAnalysisRepository = mockk(relaxed = true)
        gameRoundFeedbackRepository = mockk(relaxed = true)
        gameRoundAiUsageRepository = mockk(relaxed = true)
        service =
            GameDetailsService(
                gameRepository = gameRepository,
                gamePlayerRepository = gamePlayerRepository,
                gameRoundRepository = gameRoundRepository,
                gameRoundGuessRepository = gameRoundGuessRepository,
                gameRoundChatMessageRepository = gameRoundChatMessageRepository,
                gameRoundRiskRatingRepository = gameRoundRiskRatingRepository,
                gameRoundDrawingAnalysisRepository = gameRoundDrawingAnalysisRepository,
                gameRoundFeedbackRepository = gameRoundFeedbackRepository,
                gameRoundAiUsageRepository = gameRoundAiUsageRepository,
            )
    }

    @Test
    fun `listing games returns what the repository lists`() {
        val summary =
            PersistedGameSummary(
                id = createTestGameId(),
                code = "123456",
                hostUserId = createTestUserId(),
                createdAt = Clock.System.now(),
                feedbackGenerationMode = FeedbackGenerationMode.AI,
                endReason = null,
                endedAt = null,
            )
        every { gameRepository.findAll() } returns listOf(summary)

        service.listGames() shouldBe listOf(summary)
    }

    @Test
    fun `getting details of a game that was never persisted throws`() {
        val gameId = createTestGameId()
        every { gameRepository.findById(gameId) } returns null

        shouldThrow<GameNotFoundException> { service.getDetails(gameId) }
    }

    @Test
    fun `getting an image delegates to the drawing analysis repository`() {
        val gameId = createTestGameId()
        val analysisId = UUID.randomUUID()
        val image = PersistedDrawingImage("image/png", byteArrayOf(1, 2, 3))
        every { gameRoundDrawingAnalysisRepository.findImage(gameId, analysisId) } returns image

        service.getDrawingImage(gameId, analysisId) shouldBe image
    }

    @Test
    fun `getting an image that does not exist returns null`() {
        val gameId = createTestGameId()
        val analysisId = UUID.randomUUID()
        every { gameRoundDrawingAnalysisRepository.findImage(gameId, analysisId) } returns null

        service.getDrawingImage(gameId, analysisId) shouldBe null
    }
}
