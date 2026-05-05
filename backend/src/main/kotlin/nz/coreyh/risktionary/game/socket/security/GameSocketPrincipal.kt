package nz.coreyh.risktionary.game.socket.security

import nz.coreyh.risktionary.game.domain.model.GameId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.user.domain.model.UserId
import java.security.Principal

sealed interface GameSocketPrincipal : Principal {
    val gameId: GameId

    class Host(
        override val gameId: GameId,
        val id: UserId,
    ) : GameSocketPrincipal {
        override fun getName(): String = id.value.toString()
    }

    class Player(
        override val gameId: GameId,
        val id: GamePlayerId,
    ) : GameSocketPrincipal {
        override fun getName(): String = id.value.toString()
    }
}
