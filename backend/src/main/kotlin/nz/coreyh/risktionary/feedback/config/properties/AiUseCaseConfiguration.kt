package nz.coreyh.risktionary.feedback.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AiUseCaseProperties::class)
class AiUseCaseConfiguration

@ConfigurationProperties(prefix = "app.feedback.ai.use-cases")
data class AiUseCaseProperties(
    val imageAnalysis: UseCaseConfig,
    val factGeneration: UseCaseConfig,
    val factFramingRewrite: UseCaseConfig,
) {
    data class UseCaseConfig(
        val model: String,
        val temperature: Double,
    )
}
