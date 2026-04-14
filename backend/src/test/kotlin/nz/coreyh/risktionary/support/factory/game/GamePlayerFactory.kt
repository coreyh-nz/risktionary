package nz.coreyh.risktionary.support.factory.game

import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerIdentity
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerStatus
import nz.coreyh.risktionary.game.domain.model.player.toGamePlayerId
import nz.coreyh.risktionary.support.factory.user.createTestUserId
import nz.coreyh.risktionary.user.domain.model.UserId
import java.util.UUID

fun createTestGamePlayerId() = UUID.randomUUID().toGamePlayerId()

fun createTestGamePlayerIdentityAuthenticated(
    displayName: String = "Authenticated Test Player",
    userId: UserId = createTestUserId(),
) = GamePlayerIdentity.Authenticated(displayName, userId)

fun createTestGamePlayerIdentityGuest(displayName: String = "Guest Test Player") = GamePlayerIdentity.Guest(displayName)

fun createTestGamePlayerSession(
    id: GamePlayerId = createTestGamePlayerId(),
    identity: GamePlayerIdentity = createTestGamePlayerIdentityGuest(),
    status: GamePlayerStatus = GamePlayerStatus.PENDING,
): GamePlayerSession =
    GamePlayerSession(
        id = id,
        identity = identity,
        status = status,
    )
