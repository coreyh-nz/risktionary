package nz.coreyh.risktionary.shared.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AppProperties::class)
class AppConfiguration

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val frontendUrl: String,
)
