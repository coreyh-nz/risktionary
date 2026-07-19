package nz.coreyh.risktionary.game.socket.messages.inbound.round

data class RiskRatingCommand(
    val likelihood: String,
    val severity: String,
)
