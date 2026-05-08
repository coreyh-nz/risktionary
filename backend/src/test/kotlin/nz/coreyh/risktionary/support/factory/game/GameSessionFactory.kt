package nz.coreyh.risktionary.support.factory.game

import nz.coreyh.risktionary.game.application.session.GameSession
import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.support.factory.user.createTestUserId
import nz.coreyh.risktionary.user.domain.model.UserId

fun createTestGameSession(
    id: GameId = createTestGameId(),
    hostId: UserId = createTestUserId(),
    code: String = "123456",
): GameSession =
    GameSession(
        id = id,
        hostId = hostId,
        code = code,
    )
