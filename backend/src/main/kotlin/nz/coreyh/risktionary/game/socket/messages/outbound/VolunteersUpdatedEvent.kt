package nz.coreyh.risktionary.game.socket.messages.outbound

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.socket.messages.event.GameEvent
import nz.coreyh.risktionary.game.socket.messages.event.GameEventType

data class VolunteersUpdatedEvent(
    val volunteers: List<GamePlayerId>,
) : GameEvent(GameEventType.VOLUNTEERS_UPDATED)
