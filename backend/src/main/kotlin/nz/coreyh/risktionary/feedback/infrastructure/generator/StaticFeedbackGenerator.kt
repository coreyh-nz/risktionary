package nz.coreyh.risktionary.feedback.infrastructure.generator

import nz.coreyh.risktionary.feedback.domain.model.FeedbackFactPayload
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationResult
import nz.coreyh.risktionary.feedback.domain.service.FeedbackGenerator
import org.springframework.stereotype.Component

@Component
class StaticFeedbackGenerator : FeedbackGenerator {
    override fun generate(payload: FeedbackFactPayload): FeedbackGenerationResult = TODO("Not yet implemented")
}
