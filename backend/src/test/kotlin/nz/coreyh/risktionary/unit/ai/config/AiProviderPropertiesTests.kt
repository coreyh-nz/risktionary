package nz.coreyh.risktionary.unit.ai.config

import io.kotest.matchers.shouldBe
import java.time.Duration
import nz.coreyh.risktionary.ai.config.AiProviderProperties
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource

class AiProviderPropertiesTests {
    private fun bind(vararg properties: Pair<String, String>): AiProviderProperties =
        Binder(MapConfigurationPropertySource(mapOf(*properties)))
            .bind("app.ai", AiProviderProperties::class.java)
            .get()

    @Test
    fun `a provider only needs an api key, everything else has a default`() {
        val provider = bind("app.ai.providers.openai.api-key" to "key").providers.getValue("openai")

        provider.apiKey shouldBe "key"
        provider.baseUrl shouldBe "https://api.openai.com/v1"
        provider.timeout shouldBe Duration.ofSeconds(60)
        provider.maxRetries shouldBe 2
        provider.headers shouldBe emptyMap()
    }

    @Test
    fun `every setting of a provider can be overridden`() {
        val provider =
            bind(
                "app.ai.providers.local.base-url" to "http://localhost:11434/v1",
                "app.ai.providers.local.api-key" to "ollama",
                "app.ai.providers.local.timeout" to "5m",
                "app.ai.providers.local.max-retries" to "0",
                "app.ai.providers.local.headers.X-Title" to "risktionary",
            ).providers.getValue("local")

        provider.baseUrl shouldBe "http://localhost:11434/v1"
        provider.timeout shouldBe Duration.ofMinutes(5)
        provider.maxRetries shouldBe 0
        provider.headers shouldBe mapOf("X-Title" to "risktionary")
    }

    @Test
    fun `several providers can be configured side by side`() {
        val providers =
            bind(
                "app.ai.providers.openai.api-key" to "a",
                "app.ai.providers.gemini.api-key" to "b",
                "app.ai.providers.gemini.base-url" to "https://generativelanguage.googleapis.com/v1beta/openai/",
            ).providers

        providers.keys shouldBe setOf("openai", "gemini")
        providers.getValue("gemini").baseUrl shouldBe "https://generativelanguage.googleapis.com/v1beta/openai/"
    }
}
