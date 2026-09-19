package nz.coreyh.risktionary.game.web.dto

data class GameConfigurationDto(
    val wordIds: List<String>,
    val lobbyCountdownMs: Long,
    val phaseDurationsMs: Map<String, Long>,
    val skippingCountdownsEnabled: Boolean,
)
