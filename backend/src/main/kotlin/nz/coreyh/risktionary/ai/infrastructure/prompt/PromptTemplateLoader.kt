package nz.coreyh.risktionary.ai.infrastructure.prompt

import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.content.Media
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component

@Component
class PromptTemplateLoader(
    private val resourceLoader: ResourceLoader,
) {
    fun resource(path: String): Resource {
        val resolved = resourceLoader.getResource(if (path.startsWith("classpath:")) path else "classpath:$path")
        check(resolved.exists()) { "Prompt resource not found: $path" }
        return resolved
    }

    fun render(
        resource: Resource,
        variables: Map<String, Any> = emptyMap(),
    ): String = PromptTemplate(resource).render(variables)

    fun systemMessage(
        resource: Resource,
        variables: Map<String, Any> = emptyMap(),
    ): Message = SystemMessage(render(resource, variables))

    fun userMessage(
        resource: Resource,
        variables: Map<String, Any> = emptyMap(),
    ): Message = UserMessage(render(resource, variables))

    fun userMediaMessage(
        resource: Resource,
        media: Media,
        variables: Map<String, Any> = emptyMap(),
    ): Message =
        UserMessage
            .builder()
            .text(resource)
            .media(media)
            .build()

    fun raw(resource: Resource): String = resource.inputStream.bufferedReader().use { it.readText() }
}
