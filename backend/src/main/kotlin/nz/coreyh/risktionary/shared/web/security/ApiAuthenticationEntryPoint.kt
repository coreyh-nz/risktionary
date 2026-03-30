package nz.coreyh.risktionary.shared.web.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nz.coreyh.risktionary.shared.exception.code.ErrorCode
import nz.coreyh.risktionary.shared.web.dto.ApiErrorResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

/**
 * Entry point used by Spring Security when a request is unauthenticated.
 *
 * This is invoked when:
 * - a request is missing authentication credentials (e.g. no access token),
 * - the provided token is invalid or expired,
 * - authentication cannot be established for any reason.
 */
@Component
class ApiAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val error =
            ApiErrorResponse(
                errorCode = ErrorCode.AUTH_UNAUTHENTICATED.code,
                message = ErrorCode.AUTH_UNAUTHENTICATED.defaultMessage,
            )
        response.status = ErrorCode.AUTH_UNAUTHENTICATED.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(objectMapper.writeValueAsString(error))
    }
}
