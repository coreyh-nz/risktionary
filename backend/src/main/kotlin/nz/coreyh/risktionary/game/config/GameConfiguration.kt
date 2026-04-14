package nz.coreyh.risktionary.game.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.time.Duration
import kotlin.time.toKotlinDuration

@Configuration
@EnableConfigurationProperties(TicketProperties::class)
class GameConfiguration(
    val ticketProperties: TicketProperties,
)

@ConfigurationProperties("app.game.ticket")
class TicketProperties(
    lifetime: Duration, // spring only supports java duration afaik
) {
    val lifetime = lifetime.toKotlinDuration()
}
