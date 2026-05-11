package nz.coreyh.risktionary.game.socket.messages.outbound.drawing

import nz.coreyh.risktionary.game.domain.model.drawing.DrawingPoint
import nz.coreyh.risktionary.game.domain.model.drawing.DrawingTool
import nz.coreyh.risktionary.shared.domain.model.Identifiable

data class DrawingStrokeStartEvent(
    val drawerId: Identifiable,
    val tool: DrawingTool,
    val point: DrawingPoint,
    val colour: String,
) : DrawingEvent(type = DrawingEventType.STROKE_START)
