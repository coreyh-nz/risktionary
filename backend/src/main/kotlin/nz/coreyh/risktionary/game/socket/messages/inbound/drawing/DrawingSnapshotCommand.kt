package nz.coreyh.risktionary.game.socket.messages.inbound.drawing

data class DrawingSnapshotCommand(
    val data: String,
) : DrawingCommand(type = DrawingCommandType.SNAPSHOT)
