package nz.coreyh.risktionary.feedback.domain.model.analysis

enum class DrawingAnalysisResultType {
    FACT,
    NO_FACT,
    FAILED,
}

val DrawingAnalysisResult.type: DrawingAnalysisResultType
    get() =
        when (this) {
            is DrawingAnalysisResult.Fact -> DrawingAnalysisResultType.FACT
            is DrawingAnalysisResult.NoFact -> DrawingAnalysisResultType.NO_FACT
            is DrawingAnalysisResult.Failed -> DrawingAnalysisResultType.FAILED
        }
