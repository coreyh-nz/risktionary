package nz.coreyh.risktionary.feedback.domain.model

import nz.coreyh.risktionary.ai.domain.AiUsage
import nz.coreyh.risktionary.feedback.domain.model.condition.FeedbackFramingCondition
import kotlin.time.Instant

/**
 * The outcome of a single feedback generation: the raw fact and the fact
 * rewritten in [framingCondition]'s framing. Texts are null when the stage that
 * produces them failed.
 */
data class FeedbackGenerationResult(
    val status: FeedbackGenerationStatus,
    val factText: String?,
    val framedText: String?,
    val framingCondition: FeedbackFramingCondition,
    val generatedAt: Instant,
    val usage: List<AiUsage> = emptyList(),
)
