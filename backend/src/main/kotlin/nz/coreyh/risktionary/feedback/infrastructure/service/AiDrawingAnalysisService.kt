package nz.coreyh.risktionary.feedback.infrastructure.service

import nz.coreyh.risktionary.ai.domain.AiResponse
import nz.coreyh.risktionary.ai.domain.AiUsagePurpose
import nz.coreyh.risktionary.ai.domain.toUsage
import nz.coreyh.risktionary.ai.infrastructure.dispatch.AiDispatcher
import nz.coreyh.risktionary.ai.infrastructure.service.AiChatService
import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisOutcome
import nz.coreyh.risktionary.feedback.domain.model.analysis.DrawingAnalysisResult
import nz.coreyh.risktionary.feedback.infrastructure.ai.AiUseCaseModels
import nz.coreyh.risktionary.feedback.infrastructure.prompt.FeedbackPromptTemplates
import nz.coreyh.risktionary.shared.util.dataurl.DecodedDataUrl
import org.springframework.ai.content.Media
import org.springframework.stereotype.Service

@Service
class AiDrawingAnalysisService(
    private val aiUseCaseModels: AiUseCaseModels,
    private val feedbackPromptTemplates: FeedbackPromptTemplates,
    private val aiChatService: AiChatService,
) {
    fun analyse(
        dispatcher: AiDispatcher,
        word: String,
        decodedDataUrl: DecodedDataUrl,
        onResult: (DrawingAnalysisOutcome) -> Unit,
        onError: (Throwable) -> Unit = {},
    ) {
        dispatcher.launch(
            block = { analyse(word, decodedDataUrl) },
            onResult = onResult,
            onError = onError,
        )
    }

    private fun analyse(
        word: String,
        decodedDataUrl: DecodedDataUrl,
    ): DrawingAnalysisOutcome {
        val media =
            Media
                .builder()
                .mimeType(decodedDataUrl.mimeType)
                .data(decodedDataUrl.bytes)
                .build()

        val model = aiUseCaseModels.get(AiUsagePurpose.DRAWING_ANALYSIS)

        val response =
            aiChatService.send(
                chatModel = model.chatModel,
                messages =
                    listOf(
                        feedbackPromptTemplates.drawingAnalysisSystem(),
                        feedbackPromptTemplates.drawingAnalysisMediaUser(word, media),
                    ),
                options = model.options,
                responseClass = String::class,
            )

        return when (response) {
            is AiResponse.Success -> {
                DrawingAnalysisOutcome(
                    result = DrawingAnalysisResult.fromRawText(response.data),
                    usage = response.toUsage(AiUsagePurpose.DRAWING_ANALYSIS, model.provider, model.model),
                )
            }

            is AiResponse.Failure -> {
                DrawingAnalysisOutcome(DrawingAnalysisResult.Failed, usage = null)
            }
        }
    }
}
