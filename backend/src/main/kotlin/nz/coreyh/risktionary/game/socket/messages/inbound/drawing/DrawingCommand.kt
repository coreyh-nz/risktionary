package nz.coreyh.risktionary.game.socket.messages.inbound.drawing

import com.fasterxml.jackson.annotation.JsonTypeInfo
import nz.coreyh.risktionary.game.socket.messages.outbound.drawing.DrawingCanvasClearEvent
import tools.jackson.databind.DatabindContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.annotation.JsonTypeIdResolver
import tools.jackson.databind.jsontype.impl.TypeIdResolverBase

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = true,
)
@JsonTypeIdResolver(DrawingCommandTypeResolver::class)
sealed class DrawingCommand(
    val type: DrawingCommandType,
)

private class DrawingCommandTypeResolver : TypeIdResolverBase() {
    override fun idFromValue(
        ctxt: DatabindContext,
        value: Any,
    ): String = (value as DrawingCommand).type.name

    override fun idFromValueAndType(
        ctxt: DatabindContext,
        value: Any,
        suggestedType: Class<*>,
    ): String = (value as DrawingCommand).type.name

    override fun typeFromId(
        context: DatabindContext,
        id: String,
    ): JavaType =
        when (DrawingCommandType.valueOf(id)) {
            DrawingCommandType.STROKE_START -> DrawingStrokeStartCommand::class
            DrawingCommandType.STROKE_POINTS -> DrawingStrokePointsCommand::class
            DrawingCommandType.STROKE_END -> DrawingStrokeEndCommand::class
            DrawingCommandType.CANVAS_CLEAR -> DrawingCanvasClearEvent::class
        }.let {
            context.constructType(it.java)
        }

    override fun getMechanism(): JsonTypeInfo.Id = JsonTypeInfo.Id.CUSTOM
}
