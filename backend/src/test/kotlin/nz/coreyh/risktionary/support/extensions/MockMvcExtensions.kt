package nz.coreyh.risktionary.support.extensions

import nz.coreyh.risktionary.auth.domain.model.UserPrincipal
import nz.coreyh.risktionary.user.domain.model.User
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.ResultActionsDsl
import tools.jackson.databind.ObjectMapper

fun MockHttpServletRequestDsl.auth(user: User) {
    val principal = UserPrincipal(user.id, roles = listOf())
    val authToken =
        UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.authorities,
        )
    with(authentication(authToken))
}

fun ResultActionsDsl.andBody(block: (String) -> Unit): ResultActionsDsl {
    block(andReturn().response.contentAsString)
    return this
}

inline fun <reified T> ResultActionsDsl.andReturn(objectMapper: ObjectMapper): T {
    val content = andReturn().response.contentAsString
    return objectMapper.readValue(content, T::class.java)
}
