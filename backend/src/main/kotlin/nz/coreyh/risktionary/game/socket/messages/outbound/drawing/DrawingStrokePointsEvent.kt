package nz.coreyh.risktionary.game.socket.messages.outbound.drawing

import nz.coreyh.risktionary.game.domain.model.drawing.DrawingPoint
import nz.coreyh.risktionary.shared.domain.model.Identifiable

data class DrawingStrokePointsEvent(
    val drawerId: Identifiable,
    val points: List<DrawingPoint>,
) : DrawingEvent(type = DrawingEventType.STROKE_POINTS)
