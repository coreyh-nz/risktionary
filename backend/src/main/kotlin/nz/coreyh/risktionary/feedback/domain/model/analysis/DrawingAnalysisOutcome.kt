package nz.coreyh.risktionary.feedback.domain.model.analysis

import nz.coreyh.risktionary.ai.domain.AiUsage

/**
 * The interpretation of a drawing, with the tokens spent producing it. [usage]
 * is null when the call failed and reported none.
 */
data class DrawingAnalysisOutcome(
    val result: DrawingAnalysisResult,
    val usage: AiUsage?,
)
