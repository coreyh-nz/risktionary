package nz.coreyh.risktionary.support.factory.game.socket

import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.socket.security.GameSocketPrincipal
import nz.coreyh.risktionary.support.factory.game.createTestGameId
import nz.coreyh.risktionary.support.factory.game.createTestGamePlayerId
import nz.coreyh.risktionary.support.factory.user.createTestUserId
import nz.coreyh.risktionary.user.domain.model.UserId

fun createTestGameSocketHostPrincipal(
    gameId: GameId = createTestGameId(),
    id: UserId = createTestUserId(),
) = GameSocketPrincipal.Host(gameId = gameId, id = id)

fun createTestGameSocketPlayerPrincipal(
    gameId: GameId = createTestGameId(),
    id: GamePlayerId = createTestGamePlayerId(),
) = GameSocketPrincipal.Player(gameId = gameId, id = id)
