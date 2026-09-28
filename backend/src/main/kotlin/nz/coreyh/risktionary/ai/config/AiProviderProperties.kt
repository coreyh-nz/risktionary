package nz.coreyh.risktionary.ai.config

import java.time.Duration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AiProviderProperties::class)
class AiProviderConfiguration

/**
 * The AI providers the application can talk to, by name. Every provider must
 * speak the OpenAI chat completions API (OpenAI, Gemini, OpenRouter, Ollama and
 * others do). Use cases choose one by name, see `app.feedback.ai.use-cases`.
 */
@ConfigurationProperties(prefix = "app.ai")
data class AiProviderProperties(
    val providers: Map<String, Provider> = emptyMap(),
) {
    data class Provider(
        /** Base URL of the API, including its version path (for example `https://api.openai.com/v1`). */
        @DefaultValue("https://api.openai.com/v1") val baseUrl: String,
        @DefaultValue("") val apiKey: String,
        /** How long a single request may take before it is given up on. */
        @DefaultValue("60s") val timeout: Duration,
        @DefaultValue("2") val maxRetries: Int,
        /** Extra headers sent with every request, for example OpenRouter attribution headers. */
        val headers: Map<String, String> = emptyMap(),
    )
}
