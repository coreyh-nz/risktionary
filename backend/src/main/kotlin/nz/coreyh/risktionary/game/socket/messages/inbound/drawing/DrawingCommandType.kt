package nz.coreyh.risktionary.game.socket.messages.inbound.drawing

enum class DrawingCommandType {
    STROKE_START,
    STROKE_POINTS,
    STROKE_END,
    CANVAS_CLEAR,
    SNAPSHOT,
}
