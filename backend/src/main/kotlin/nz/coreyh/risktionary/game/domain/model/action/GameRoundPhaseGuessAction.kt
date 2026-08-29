package nz.coreyh.risktionary.game.domain.model.action

data class GameRoundPhaseGuessAction(
    val guess: String,
)

enum class GameRoundPhaseGuessActionResult {
    CONSUMED,
    SKIPPED,
}
