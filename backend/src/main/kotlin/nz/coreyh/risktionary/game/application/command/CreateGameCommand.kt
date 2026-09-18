package nz.coreyh.risktionary.game.application.command

import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType
import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.words.domain.model.Word
import kotlin.time.Duration

data class CreateGameCommand(
    val hostId: UserId,
    val words: List<Word>,
    val lobbyCountdown: Duration,
    val phaseDurations: Map<RoundPhaseType, Duration>,
    val skippingCountdownsEnabled: Boolean,
)
