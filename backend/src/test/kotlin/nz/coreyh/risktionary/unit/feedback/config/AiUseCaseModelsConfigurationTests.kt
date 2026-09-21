package nz.coreyh.risktionary.unit.feedback.config

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import nz.coreyh.risktionary.ai.config.AiProviderProperties
import nz.coreyh.risktionary.ai.domain.AiUsagePurpose
import nz.coreyh.risktionary.feedback.config.AiUseCaseModelsConfiguration
import nz.coreyh.risktionary.feedback.config.properties.AiUseCaseProperties
import org.junit.jupiter.api.Test
import java.time.Duration

class AiUseCaseModelsConfigurationTests {
    private val configuration = AiUseCaseModelsConfiguration()

    private fun provider(apiKey: String = "key") =
        AiProviderProperties.Provider(
            baseUrl = "http://localhost:0/v1",
            apiKey = apiKey,
            timeout = Duration.ofSeconds(5),
            maxRetries = 0,
            headers = mapOf("X-Title" to "risktionary"),
        )

    private fun useCases(
        image: String = "openai",
        fact: String = "openai",
        rewrite: String = "openai",
    ) = AiUseCaseProperties(
        imageAnalysis = AiUseCaseProperties.UseCaseConfig(image, "image-model", 0.2),
        factGeneration = AiUseCaseProperties.UseCaseConfig(fact, "fact-model", 0.3),
        factFramingRewrite = AiUseCaseProperties.UseCaseConfig(rewrite, "rewrite-model", 0.4),
    )

    @Test
    fun `each use case gets its configured provider, model and temperature`() {
        val models =
            configuration.aiUseCaseModels(
                AiProviderProperties(mapOf("openai" to provider(), "local" to provider())),
                useCases(image = "openai", fact = "local", rewrite = "local"),
            )

        models.get(AiUsagePurpose.DRAWING_ANALYSIS).provider shouldBe "openai"
        models.get(AiUsagePurpose.FACT_GENERATION).provider shouldBe "local"
        models.get(AiUsagePurpose.FACT_GENERATION).model shouldBe "fact-model"
        models.get(AiUsagePurpose.FRAMING_REWRITE).options.temperature shouldBe 0.4
    }

    @Test
    fun `use cases on the same provider share a chat model and different providers do not`() {
        val models =
            configuration.aiUseCaseModels(
                AiProviderProperties(mapOf("openai" to provider(), "local" to provider())),
                useCases(image = "openai", fact = "local", rewrite = "local"),
            )

        (models.get(AiUsagePurpose.FACT_GENERATION).chatModel === models.get(AiUsagePurpose.FRAMING_REWRITE).chatModel) shouldBe true
        (models.get(AiUsagePurpose.DRAWING_ANALYSIS).chatModel === models.get(AiUsagePurpose.FACT_GENERATION).chatModel) shouldBe false
    }

    @Test
    fun `the snapshot lists every use case with what it runs on`() {
        val models =
            configuration.aiUseCaseModels(
                AiProviderProperties(mapOf("openai" to provider(), "local" to provider())),
                useCases(fact = "local"),
            )

        models.snapshot().map { Triple(it.purpose, it.provider, it.model) } shouldBe
            listOf(
                Triple(AiUsagePurpose.DRAWING_ANALYSIS, "openai", "image-model"),
                Triple(AiUsagePurpose.FACT_GENERATION, "local", "fact-model"),
                Triple(AiUsagePurpose.FRAMING_REWRITE, "openai", "rewrite-model"),
            )
    }

    @Test
    fun `providers that no use case uses need no credentials`() {
        val models =
            configuration.aiUseCaseModels(
                AiProviderProperties(mapOf("openai" to provider(), "gemini" to provider(apiKey = ""))),
                useCases(),
            )

        models.get(AiUsagePurpose.FACT_GENERATION).provider shouldBe "openai"
    }

    @Test
    fun `a use case naming an unknown provider fails with a helpful message`() {
        val error =
            shouldThrow<IllegalStateException> {
                configuration.aiUseCaseModels(AiProviderProperties(mapOf("openai" to provider())), useCases(fact = "gemini"))
            }

        error.message shouldContain "fact-generation"
        error.message shouldContain "gemini"
        error.message shouldContain "openai"
    }

    @Test
    fun `a used provider without an api key fails with a helpful message`() {
        val error =
            shouldThrow<IllegalStateException> {
                configuration.aiUseCaseModels(AiProviderProperties(mapOf("openai" to provider(apiKey = ""))), useCases())
            }

        error.message shouldContain "openai"
        error.message shouldContain "api-key"
    }
}
