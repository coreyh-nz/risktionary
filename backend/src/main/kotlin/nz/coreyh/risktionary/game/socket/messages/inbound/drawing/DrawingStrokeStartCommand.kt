package nz.coreyh.risktionary.game.socket.messages.inbound.drawing

import nz.coreyh.risktionary.game.domain.model.drawing.DrawingPoint
import nz.coreyh.risktionary.game.domain.model.drawing.DrawingTool

data class DrawingStrokeStartCommand(
    val tool: DrawingTool,
    val point: DrawingPoint,
    val colour: String,
) : DrawingCommand(type = DrawingCommandType.STROKE_START)
