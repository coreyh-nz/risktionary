package nz.coreyh.risktionary.game.domain.model

import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationMode
import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType
import nz.coreyh.risktionary.words.domain.model.Word
import kotlin.time.Duration

data class GameConfiguration(
    /**
     * Words that may be selected during the game.
     */
    val words: List<Word>,
    /**
     * Time spent waiting in the lobby before the game starts.
     */
    val lobbyCountdown: Duration,
    /**
     * Time limit for each round phase.
     *
     * A value of null means the phase has no timer and will continue until
     * manually advanced.
     */
    val phaseDurations: Map<RoundPhaseType, Duration>,
    /**
     * Whether countdowns can be skipped by the host.
     */
    val skippingCountdownsEnabled: Boolean,
    /**
     * Controls how feedback is generated during the game.
     */
    val feedbackGenerationMode: FeedbackGenerationMode,
) {
    val feedbackGenerationEnabled = feedbackGenerationMode != FeedbackGenerationMode.NONE
}
