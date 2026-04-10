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

        fun lobby(gameId: GameId) = "$BASE/${gameId.value}/lobby"
    }

    // Server -> Specific subscriber
    object Queue {
        const val PREFIX = "${USER_PREFIX}/queue"
    }
}
