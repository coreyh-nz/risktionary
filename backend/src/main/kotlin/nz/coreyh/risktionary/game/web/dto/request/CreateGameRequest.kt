package nz.coreyh.risktionary.game.web.dto.request

import nz.coreyh.risktionary.game.application.command.CreateGameCommand
import nz.coreyh.risktionary.game.domain.model.round.phase.RoundPhaseType
import nz.coreyh.risktionary.game.web.dto.GameConfigurationDto
import nz.coreyh.risktionary.shared.exception.ValidationException
import nz.coreyh.risktionary.shared.validation.addError
import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.words.application.service.WordService
import nz.coreyh.risktionary.words.domain.model.WordId
import nz.coreyh.risktionary.words.domain.model.toWordIdOrNull
import kotlin.time.Duration.Companion.milliseconds

data class CreateGameRequest(
    val configuration: GameConfigurationDto,
)

/**
 * Validates the request and resolves it into a [CreateGameCommand].
 *
 * All fields are validated up front so that every problem with the request
 * is reported in a single [ValidationException], keyed by the corresponding
 * paths on the request object.
 */
fun CreateGameRequest.toCommand(
    hostId: UserId,
    wordService: WordService,
): CreateGameCommand {
    val fieldErrors = mutableMapOf<String, String>()

    val wordIds: List<IndexedValue<WordId>> =
        configuration.wordIds.withIndex().mapNotNull { (index, rawId) ->
            val wordId = rawId.toWordIdOrNull()
            if (wordId == null) {
                fieldErrors.addError(
                    CreateGameRequest::configuration.name,
                    GameConfigurationDto::wordIds.name,
                    index = index,
                    message = "Invalid word id",
                )
                null
            } else {
                IndexedValue(index, wordId)
            }
        }

    val words =
        if (wordIds.isNotEmpty()) {
            val foundWordsById = wordService.findWordsByIds(wordIds.map { it.value }).associateBy { it.id }
            wordIds.mapNotNull { (index, wordId) ->
                foundWordsById[wordId] ?: run {
                    fieldErrors.addError(
                        CreateGameRequest::configuration.name,
                        GameConfigurationDto::wordIds.name,
                        index = index,
                        message = "Unknown word",
                    )
                    null
                }
            }
        } else {
            fieldErrors.addError(
                CreateGameRequest::configuration.name,
                GameConfigurationDto::wordIds.name,
                message = "At least one word is required",
            )
            emptyList()
        }

    val phaseDurations =
        configuration.phaseDurationsMs.entries.mapNotNull { (key, ms) ->
            val phaseType = RoundPhaseType.entries.find { it.name == key }
            if (phaseType == null) {
                fieldErrors.addError(
                    CreateGameRequest::configuration.name,
                    GameConfigurationDto::phaseDurationsMs.name,
                    index = key,
                    message = "Unknown round phase",
                )
                null
            } else {
                phaseType to ms.milliseconds
            }
        }

    if (fieldErrors.isNotEmpty()) {
        throw ValidationException(fieldErrors)
    }

    return CreateGameCommand(
        hostId = hostId,
        words = words,
        lobbyCountdown = configuration.lobbyCountdownMs.milliseconds,
        phaseDurations = phaseDurations.toMap(),
        skippingCountdownsEnabled = configuration.skippingCountdownsEnabled,
    )
}
