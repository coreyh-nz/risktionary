package nz.coreyh.risktionary.support.factory.game

import nz.coreyh.risktionary.game.domain.model.toGameId
import java.util.UUID

fun createTestGameId() = UUID.randomUUID().toGameId()
