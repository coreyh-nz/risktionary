package nz.coreyh.risktionary.game.application.session

import nz.coreyh.risktionary.game.domain.model.player.GamePlayer
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerStatus

class GamePlayerSession(
    val player: GamePlayer,
    var status: GamePlayerStatus,
) {
    val id: GamePlayerId get() = player.id
}
