package nz.coreyh.risktionary.game.socket.messages.outbound

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.socket.messages.event.GameEvent
import nz.coreyh.risktionary.game.socket.messages.event.GameEventType

/**
 * Event emitted when a player leaves the game.
 */
class PlayerLeftEvent(
    val playerId: GamePlayerId,
) : GameEvent {
    override val type: GameEventType = GameEventType.PLAYER_LEFT
}
