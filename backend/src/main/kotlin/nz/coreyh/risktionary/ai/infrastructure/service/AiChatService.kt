package nz.coreyh.risktionary.ai.infrastructure.service

import nz.coreyh.risktionary.ai.domain.AiResponse
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Service

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
    final inline fun <reified T : Any> send(
        chatModel: ChatModel,
        messages: List<Message>,
        options: ChatOptions? = null,
    ): AiResponse<T> =
        runCatching {
            val response =
                ChatClient
                    .create(chatModel)
                    .prompt(Prompt(messages, options))
                    .call()
                    .responseEntity(T::class.java)

            val entity = response.entity ?: throw IllegalStateException("Model returned an empty body")
            val usage = response.response?.metadata?.usage

            AiResponse.Success(
                data = entity,
                promptTokens = usage?.promptTokens ?: 0,
                completionTokens = usage?.completionTokens ?: 0,
                totalTokens = usage?.totalTokens ?: 0,
            )
        }.getOrElse { exception ->
            AiResponse.Failure(
                message = exception.message ?: "An unknown AI error occurred",
                cause = exception,
            )
        }
}
