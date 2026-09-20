package nz.coreyh.risktionary.feedback.application.service

import nz.coreyh.risktionary.feedback.application.exception.AiFeedbackDisabledException
import nz.coreyh.risktionary.feedback.domain.model.FeedbackGenerationMode
import nz.coreyh.risktionary.feedback.domain.service.FeedbackGenerator
import nz.coreyh.risktionary.feedback.infrastructure.generator.AiFeedbackGenerator
import nz.coreyh.risktionary.feedback.infrastructure.generator.StaticFeedbackGenerator
import org.springframework.stereotype.Service

@Service
class FeedbackGeneratorResolver(
    private val aiFeedbackGenerator: AiFeedbackGenerator,
    private val staticFeedbackGenerator: StaticFeedbackGenerator,
) {
    fun resolve(mode: FeedbackGenerationMode): FeedbackGenerator =
        when (mode) {
            FeedbackGenerationMode.NONE -> throw AiFeedbackDisabledException()
            FeedbackGenerationMode.AI -> aiFeedbackGenerator
            FeedbackGenerationMode.STATIC -> staticFeedbackGenerator
        }
}
