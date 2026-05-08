package nz.coreyh.risktionary.game.socket.support

import nz.coreyh.risktionary.game.domain.model.GameId

object WebSocketDestinations {
    const val USER_PREFIX = "/user"

    // Client -> Server (via @MessageMapping)
    object App {
        const val PREFIX = "/app"
    }

    // Server -> All subscribers
    object Topic {
        const val PREFIX = "/topic"
        private const val BASE = "$PREFIX/game"

        fun base(gameId: GameId) = "$BASE/${gameId.value}"

        fun players(gameId: GameId) = "$BASE/${gameId.value}/players"
    }

    // Server -> Specific subscriber
    object Queue {
        const val PREFIX = "/queue"

        const val PLAYER_LIST = "$PREFIX/player-list"
    }
}
