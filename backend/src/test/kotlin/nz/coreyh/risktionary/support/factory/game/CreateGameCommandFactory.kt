package nz.coreyh.risktionary.support.factory.game

import nz.coreyh.risktionary.game.application.command.CreateGameCommand
import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType
import nz.coreyh.risktionary.support.factory.user.createTestUserId
import nz.coreyh.risktionary.support.factory.word.createTestWord
import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.words.domain.model.Word
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

fun createTestCreateGameCommand(
    hostId: UserId = createTestUserId(),
    words: List<Word> = listOf(createTestWord()),
    lobbyCountdown: Duration = 5.seconds,
    phaseDurations: Map<RoundPhaseType, Duration> = RoundPhaseType.entries.associateWith { 60.seconds },
    skippingCountdownsEnabled: Boolean = true,
): CreateGameCommand =
    CreateGameCommand(
        hostId = hostId,
        words = words,
        lobbyCountdown = lobbyCountdown,
        phaseDurations = phaseDurations,
        skippingCountdownsEnabled = skippingCountdownsEnabled,
    )
