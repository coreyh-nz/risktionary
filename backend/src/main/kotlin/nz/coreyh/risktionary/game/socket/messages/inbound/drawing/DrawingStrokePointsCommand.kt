package nz.coreyh.risktionary.game.socket.messages.inbound.drawing

import nz.coreyh.risktionary.game.domain.model.drawing.DrawingPoint

data class DrawingStrokePointsCommand(
    val points: List<DrawingPoint>,
) : DrawingCommand(type = DrawingCommandType.STROKE_POINTS)
