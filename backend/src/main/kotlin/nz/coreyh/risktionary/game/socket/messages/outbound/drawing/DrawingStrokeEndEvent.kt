package nz.coreyh.risktionary.game.socket.messages.outbound.drawing

import nz.coreyh.risktionary.shared.domain.model.Identifiable

data class DrawingStrokeEndEvent(
    val drawerId: Identifiable,
) : DrawingEvent(type = DrawingEventType.STROKE_END)
