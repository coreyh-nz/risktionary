package nz.coreyh.risktionary.support.creator

import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.support.factory.game.createTestCreateGameCommand
import nz.coreyh.risktionary.user.domain.model.User
import nz.coreyh.risktionary.words.domain.repository.WordRepository
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.time.Clock

@Component
class TestGameSessionCreator(
    private val gameSessionService: GameSessionService,
    private val testUserCreator: TestUserCreator,
    private val wordRepository: WordRepository,
) {
    /**
     * Creates a game session whose host and word are real database rows, as
     * game creation is persisted and references them.
     */
    fun createTestGameSession(host: User = testUserCreator.createUniqueTestUser()): GameSession {
        val word =
            wordRepository.create(
                value = "word-${UUID.randomUUID()}",
                descriptionText = "description",
                descriptionContent = "description",
                synonyms = emptyList(),
                createdBy = host.id,
                createdAt = Clock.System.now(),
            )
        return gameSessionService.createSession(createTestCreateGameCommand(hostId = host.id, words = listOf(word)))
    }
}
