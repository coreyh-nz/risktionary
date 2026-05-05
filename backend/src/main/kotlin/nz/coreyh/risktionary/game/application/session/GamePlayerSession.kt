package nz.coreyh.risktionary.game.application.session

import nz.coreyh.risktionary.game.domain.model.player.GamePlayerId
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerIdentity
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerStatus

class GamePlayerSession(
    val id: GamePlayerId,
    val identity: GamePlayerIdentity,
    var status: GamePlayerStatus = GamePlayerStatus.PENDING,
)
