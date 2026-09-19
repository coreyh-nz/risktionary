package nz.coreyh.risktionary.support.creator

import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.support.factory.game.createTestCreateGameCommand
import nz.coreyh.risktionary.user.domain.model.User
import org.springframework.stereotype.Component

@Component
class TestGameSessionCreator(
    private val gameSessionService: GameSessionService,
    private val testUserCreator: TestUserCreator,
) {
    fun createTestGameSession(host: User = testUserCreator.createUniqueTestUser()): GameSession =
        gameSessionService.createSession(createTestCreateGameCommand(hostId = host.id))
}
