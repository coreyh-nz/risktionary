package nz.coreyh.risktionary.support.factory.game

import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationMode
import nz.coreyh.risktionary.game.domain.model.GameConfiguration
import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType
import nz.coreyh.risktionary.support.factory.word.createTestWord
import nz.coreyh.risktionary.words.domain.model.Word
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun createTestGameConfiguration(
    words: List<Word> = listOf(createTestWord()),
    lobbyCountdown: Duration = 5.seconds,
    phaseDurations: Map<RoundPhaseType, Duration> = RoundPhaseType.entries.associateWith { 60.seconds },
    skippingCountdownsEnabled: Boolean = true,
    feedbackGenerationMode: FeedbackGenerationMode = FeedbackGenerationMode.AI,
): GameConfiguration =
    GameConfiguration(
        words = words,
        lobbyCountdown = lobbyCountdown,
        phaseDurations = phaseDurations,
        skippingCountdownsEnabled = skippingCountdownsEnabled,
        feedbackGenerationMode = feedbackGenerationMode,
    )
