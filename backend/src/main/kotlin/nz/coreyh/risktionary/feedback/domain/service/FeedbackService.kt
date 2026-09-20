package nz.coreyh.risktionary.feedback.domain.service

import nz.coreyh.risktionary.feedback.application.service.FeedbackGeneratorResolver
import nz.coreyh.risktionary.feedback.domain.model.FeedbackFactPayload
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationMode
import org.springframework.stereotype.Service

@Service
class FeedbackService(
    private val feedbackGeneratorResolver: FeedbackGeneratorResolver,
) {
    fun generate(
        mode: FeedbackGenerationMode,
        payload: FeedbackFactPayload,
    ) = feedbackGeneratorResolver
        .resolve(mode)
        .generate(payload)
}
