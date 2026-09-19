package nz.coreyh.risktionary.feedback.domain.service

import nz.coreyh.risktionary.feedback.domain.model.FeedbackFactPayload
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationResult

interface FeedbackGenerator {
    fun generate(payload: FeedbackFactPayload): FeedbackGenerationResult
}
