package nz.coreyh.risktionary.auth.infrastructure.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nz.coreyh.risktionary.auth.application.service.AuthService
import nz.coreyh.risktionary.auth.application.service.OAuthUserInfoService
import nz.coreyh.risktionary.auth.config.AuthConfiguration
import nz.coreyh.risktionary.shared.web.support.addCookie
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

@Component
class OAuth2SuccessHandler(
    private val oAuthUserInfoService: OAuthUserInfoService,
    private val authService: AuthService,
    private val authConfiguration: AuthConfiguration,
    private val objectMapper: ObjectMapper,
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
        val accessToken = authService.authenticateOAuthUser(oauthUserInfo)
        response.addCookie(
            name = authConfiguration.cookie.accessTokenName,
            value = accessToken.value,
        ) {
            lifetime = accessToken.expiresAt - accessToken.issuedAt
            path = authConfiguration.cookie.path
            secure = authConfiguration.cookie.secure
            sameSite = authConfiguration.cookie.sameSite
            httpOnly = true
        }

        val body =
            mapOf(
                "accessToken" to accessToken.value,
            )
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(objectMapper.writeValueAsString(body))
        response.writer.flush()
    }
}
