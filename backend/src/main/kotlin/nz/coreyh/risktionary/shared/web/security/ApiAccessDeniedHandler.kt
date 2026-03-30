package nz.coreyh.risktionary.shared.web.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nz.coreyh.risktionary.shared.exception.code.ErrorCode
import nz.coreyh.risktionary.shared.web.dto.ApiErrorResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

/**
 * Handler used by Spring Security when an authenticated user attempts to access a resource
 * they do not have permission to access.
 *
 * This is invoked when:
 * - the user is authenticated, but lacks the required authorities/roles,
 * - access is explicitly denied by the security configuration.
 */
@Component
class ApiAccessDeniedHandler(
    private val objectMapper: ObjectMapper,
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        val error =
            ApiErrorResponse(
                errorCode = ErrorCode.AUTH_FORBIDDEN.code,
                message = ErrorCode.AUTH_FORBIDDEN.defaultMessage,
            )

        response.status = ErrorCode.AUTH_FORBIDDEN.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(objectMapper.writeValueAsString(error))
    }
}
