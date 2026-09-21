package nz.coreyh.risktionary.game.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.time.Duration
import kotlin.time.toKotlinDuration

@Configuration
@EnableConfigurationProperties(
    TicketProperties::class,
    GameSessionCleanupProperties::class,
    GameResearchPersistenceProperties::class,
)
class GameConfiguration

@ConfigurationProperties("app.game.ticket")
class TicketProperties(
    lifetime: Duration, // spring only supports java duration afaik
) {
    val lifetime = lifetime.toKotlinDuration()
}

@ConfigurationProperties(prefix = "app.game.session.cleanup")
class GameSessionCleanupProperties(
    hostJoinGracePeriod: Duration,
    hostAbandonedTimeout: Duration,
    stuckStartingTimeout: Duration,
) {
    val hostJoinGracePeriod = hostJoinGracePeriod.toKotlinDuration()
    val hostAbandonedTimeout = hostAbandonedTimeout.toKotlinDuration()
    val stuckStartingTimeout = stuckStartingTimeout.toKotlinDuration()
}

@ConfigurationProperties(prefix = "app.game.persistence")
class GameResearchPersistenceProperties(
    pendingAiTimeout: Duration,
) {
    /** How long a round waits for in-flight AI calls to finish before it is saved. */
    val pendingAiTimeout = pendingAiTimeout.toKotlinDuration()
}
