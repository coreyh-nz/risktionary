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

        fun draw(gameId: GameId) = "${base(gameId)}/draw"

        fun round(gameId: GameId) = "$BASE/${gameId.value}/round"
    }

    // Server -> Specific subscriber
    object Queue {
        const val PREFIX = "/queue"

        const val GAME = "$PREFIX/game"
        const val ROUND = "$GAME/round"
    }
}
