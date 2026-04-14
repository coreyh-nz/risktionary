package nz.coreyh.risktionary.game.domain.model.player

import nz.coreyh.risktionary.user.domain.model.UserId

sealed interface GamePlayerIdentity {
    val displayName: String

    data class Guest(
        override val displayName: String,
    ) : GamePlayerIdentity

    data class Authenticated(
        override val displayName: String,
        val userId: UserId,
    ) : GamePlayerIdentity
}
