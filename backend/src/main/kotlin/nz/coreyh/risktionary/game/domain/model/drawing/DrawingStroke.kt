package nz.coreyh.risktionary.game.domain.model.drawing

data class DrawingStroke(
    val points: List<DrawingPoint>,
    val tool: DrawingTool,
    val colour: String,
)
