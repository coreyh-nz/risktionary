package nz.coreyh.risktionary.shared.config

import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Configures custom Jackson serializers.
 */
@Configuration
class JacksonConfiguration {
    @Bean
    fun jacksonCustomizer(): JsonMapperBuilderCustomizer =
        JsonMapperBuilderCustomizer { builder ->
            builder.addModule(
                SimpleModule().apply {
                    addSerializer(DurationSerializer)
                    addSerializer(InstantSerializer)
                },
            )
        }
}

/**
 * Serializes a Kotlin [Duration] as a plain JSON number representing whole milliseconds.
 */
private object DurationSerializer : StdSerializer<Duration>(Duration::class.java) {
    override fun serialize(
        value: Duration,
        gen: JsonGenerator,
        ctxt: SerializationContext,
    ) {
        gen.writeNumber(value.inWholeMilliseconds)
    }
}

/**
 * Serializes a Kotlin [Instant] as an ISO 8601 offset date-time string.
 */
private object InstantSerializer : StdSerializer<Instant>(Instant::class.java) {
    override fun serialize(
        value: Instant,
        gen: JsonGenerator,
        ctxt: SerializationContext,
    ) {
        gen.writeString(value.format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET))
    }
}
