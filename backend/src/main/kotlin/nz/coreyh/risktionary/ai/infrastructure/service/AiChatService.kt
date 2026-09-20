package nz.coreyh.risktionary.ai.infrastructure.service

import io.github.oshai.kotlinlogging.KotlinLogging
import nz.coreyh.risktionary.ai.domain.AiResponse
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Service
import kotlin.reflect.KClass
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val logger = KotlinLogging.logger {}

/**
 * Service responsible for sending chat prompts to an AI model and
 * returning structured, typed responses.
 *
 * This service is designed to support multiple AI models, typed
 * deserialization, and unified success/failure handling through the
 * `AiResponse` sealed interface.
 */
@Service
class AiChatService {
    /**
     * Sends a chat prompt to the configured AI model and returns a typed
     * `AiResponse`.
     *
     * @param T The target type to deserialize the AI response body into.
     * @param chatModel The AI chat model used to execute the request.
     * @param messages The ordered list of chat messages forming the prompt.
     * @param options Optional chat configuration such as temperature or max
     *    tokens.
     * @return `AiResponse.Success` containing the deserialized response and
     *    token usage, or `AiResponse.Failure` if an exception occurred during
     *    the request.
     */
    @OptIn(ExperimentalUuidApi::class) // todo: update kotlin - this is now stable
    fun <T : Any> send(
        chatModel: ChatModel,
        messages: List<Message>,
        options: ChatOptions? = null,
        responseClass: KClass<T>,
    ): AiResponse<T> {
        val requestId = Uuid.random()

        logger.debug {
            "[$requestId] Sending AI request with ${messages.size} messages using model: ${options?.model ?: chatModel.options.model}"
        }

        return runCatching {
            val response =
                ChatClient
                    .create(chatModel)
                    .prompt(Prompt(messages, options))
                    .call()
                    .responseEntity(responseClass.java)

            val entity =
                response.entity ?: run {
                    logger.debug { "[$requestId] Model returned an empty body for responseClass=${responseClass.qualifiedName}" }
                    throw IllegalStateException("Model returned an empty body")
                }

            val usage = response.response?.metadata?.usage

            val success =
                AiResponse
                    .Success(
                        data = entity,
                        promptTokens = usage?.promptTokens ?: 0,
                        completionTokens = usage?.completionTokens ?: 0,
                        totalTokens = usage?.totalTokens ?: 0,
                    )

            logger.debug { "[$requestId] AI token usage: $usage" }

            success
        }.getOrElse { exception ->
            logger.error(exception) { "[$requestId] AI request failed: ${exception.message}" }

            AiResponse.Failure(
                message = exception.message ?: "An unknown AI error occurred",
                cause = exception,
            )
        }
    }
}
