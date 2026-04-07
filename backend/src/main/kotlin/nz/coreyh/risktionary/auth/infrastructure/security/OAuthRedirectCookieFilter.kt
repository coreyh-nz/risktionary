package nz.coreyh.risktionary.auth.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nz.coreyh.risktionary.auth.config.AuthConfiguration
import nz.coreyh.risktionary.shared.web.support.Routes
import nz.coreyh.risktionary.shared.web.support.addCookie
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class OAuthRedirectCookieFilter(
    private val authConfiguration: AuthConfiguration,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val uri = request.requestURI
        if (uri.startsWith(Routes.V1.OAuth.BASE)) {
            request.getParameter("redirectUrl")?.let {
                val cookieName = authConfiguration.cookie.loginSuccessRedirectUrlName
                response.addCookie(cookieName, it) {
                    path = Routes.V1.OAuth.BASE
                    secure = authConfiguration.cookie.secure
                    sameSite = authConfiguration.cookie.sameSite
                    httpOnly = true
                }
            }
        }

        filterChain.doFilter(request, response)
    }
}
