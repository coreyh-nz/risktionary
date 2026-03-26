package nz.coreyh.risktionary.auth.infrastructure.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nz.coreyh.risktionary.auth.application.service.AuthService
import nz.coreyh.risktionary.auth.application.service.OAuthUserInfoService
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2SuccessHandler(
    private val oAuthUserInfoService: OAuthUserInfoService,
    private val authService: AuthService,
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val token = authentication as OAuth2AuthenticationToken
        val registrationId = token.authorizedClientRegistrationId
        val oauthUser = authentication.principal as OAuth2User
        val oauthUserInfo = oAuthUserInfoService.extract(oauthUser, registrationId)
        authService.authenticateOAuthUser(oauthUserInfo)
    }
}
