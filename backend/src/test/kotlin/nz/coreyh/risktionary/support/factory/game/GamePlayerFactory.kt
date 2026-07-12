package nz.coreyh.risktionary.support.factory.game

import nz.coreyh.risktionary.game.application.session.GamePlayerSession
import nz.coreyh.risktionary.game.domain.model.player.GamePlayer
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

fun createTestGamePlayer(
    id: GamePlayerId = createTestGamePlayerId(),
    identity: GamePlayerIdentity = createTestGamePlayerIdentityGuest(),
): GamePlayer =
    GamePlayer(
        id = id,
        identity = identity,
    )

fun createTestGamePlayerSession(
    player: GamePlayer = createTestGamePlayer(),
    status: GamePlayerStatus = GamePlayerStatus.PENDING,
): GamePlayerSession =
    GamePlayerSession(
        player = player,
        status = status,
    )
