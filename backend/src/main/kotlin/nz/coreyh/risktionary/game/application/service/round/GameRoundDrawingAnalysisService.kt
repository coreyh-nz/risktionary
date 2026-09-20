package nz.coreyh.risktionary.game.application.service.round

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.ai.infrastructure.dispatch.AiDispatcher
import nz.coreyh.risktionary.feedback.infrastructure.service.AiDrawingAnalysisService
import nz.coreyh.risktionary.game.application.session.round.GameRoundSession
import nz.coreyh.risktionary.shared.util.dataurl.DataUrlDecoder
import org.springframework.stereotype.Service
import kotlin.time.Clock

private val logger = KotlinLogging.logger {}

@Service
class GameRoundDrawingAnalysisService(
    private val aiDrawingAnalysisService: AiDrawingAnalysisService,
    private val clock: Clock = Clock.System,
) {
    fun analyse(
        round: GameRoundSession,
        aiDispatcher: AiDispatcher,
        dataUrl: String,
    ) {
        val decodedDataUrl =
            runCatching { DataUrlDecoder.decode(dataUrl) }
                .getOrElse {
                    logger.error(it) { "Failed to decode drawing dataUrl (round=${round.id})" }
                    return
                }

        val capturedAt = clock.now()

        logger.debug { "Dispatching drawing analysis (word=${round.word.value})" }

        aiDrawingAnalysisService.analyse(
            dispatcher = aiDispatcher,
            decodedDataUrl = decodedDataUrl,
            word = round.word.value,
            onResult = { result ->
                logger.debug { "Drawing analysis completed (round=${round.id}, result=\"$result\")" }
                round.drawing.record(
                    imageBytes = decodedDataUrl.bytes,
                    mimeType = decodedDataUrl.mimeType.toString(),
                    result = result,
                    capturedAt = capturedAt,
                )
            },
            onError = { e ->
                logger.error(e) { "Drawing analysis error (round=${round.id})" }
            },
        )
    }
}
