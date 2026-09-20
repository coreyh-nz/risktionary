package nz.coreyh.risktionary.feedback.domain.model.analysis

sealed class DrawingAnalysisResult {
    data class Fact(
        val note: String,
    ) : DrawingAnalysisResult()

    data object NoFact : DrawingAnalysisResult()

    data object Failed : DrawingAnalysisResult()

    companion object {
        private const val NO_FACT_SENTINEL = "NO_MEANINGFUL_FACT"

        fun fromRawText(text: String): DrawingAnalysisResult = if (text.trim() == NO_FACT_SENTINEL) NoFact else Fact(text.trim())
    }
}
