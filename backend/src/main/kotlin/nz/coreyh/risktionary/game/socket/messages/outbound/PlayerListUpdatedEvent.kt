package nz.coreyh.risktionary.game.socket.messages.outbound

import nz.coreyh.risktionary.game.socket.messages.event.GameEvent
import nz.coreyh.risktionary.game.socket.messages.event.GameEventType
import nz.coreyh.risktionary.game.socket.messages.view.GamePlayerView

/**
 * Event emitted when the list of players is updated, after a player signals
 * they are ready and the server responds with the current full list of
 * joined players.
 */
data class PlayerListUpdatedEvent(
    val players: List<GamePlayerView>,
) : GameEvent(GameEventType.PLAYER_LIST_UPDATED)
