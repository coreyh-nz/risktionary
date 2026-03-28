package nz.coreyh.risktionary.auth.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nz.coreyh.risktionary.auth.application.service.AuthTokenService
import nz.coreyh.risktionary.auth.config.AuthConfiguration
import nz.coreyh.risktionary.auth.domain.model.UserPrincipal
import nz.coreyh.risktionary.shared.exception.UnauthenticatedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.WebUtils

@Component
class JwtAuthenticationFilter(
    private val authTokenService: AuthTokenService,
    private val authConfiguration: AuthConfiguration,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val cookie = WebUtils.getCookie(request, authConfiguration.cookie.accessTokenName)
        if (cookie != null) {
            try {
                val token = authTokenService.decodeAccessToken(cookie.value)
                val principal = UserPrincipal(token.userId, listOf())
                val authentication = UsernamePasswordAuthenticationToken(principal, null, listOf())
                SecurityContextHolder.getContext().authentication = authentication
            } catch (_: UnauthenticatedException) {
            }
        }
        filterChain.doFilter(request, response)
    }
}
