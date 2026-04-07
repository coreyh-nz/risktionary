package nz.coreyh.risktionary.auth.infrastructure.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nz.coreyh.risktionary.auth.application.service.AuthService
import nz.coreyh.risktionary.auth.application.service.OAuthUserInfoService
import nz.coreyh.risktionary.auth.config.AuthConfiguration
import nz.coreyh.risktionary.shared.config.AppProperties
import nz.coreyh.risktionary.shared.web.support.addCookie
import nz.coreyh.risktionary.shared.web.support.deleteCookie
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.WebUtils

@Component
class OAuth2SuccessHandler(
    private val oAuthUserInfoService: OAuthUserInfoService,
    private val authService: AuthService,
    private val appProperties: AppProperties,
    private val authConfiguration: AuthConfiguration,
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

        // if the redirect url cookie was set, redirect the user back to where they came from
        // fallback to frontend url
        val redirectUrlCookieName = authConfiguration.cookie.loginSuccessRedirectUrlName
        val redirectUrlCookie = WebUtils.getCookie(request, redirectUrlCookieName)
        val redirectUrl =
            redirectUrlCookie
                ?.let { cookie ->
                    response.deleteCookie(redirectUrlCookieName)
                    cookie.value.takeIf { it.startsWith(appProperties.frontendUrl, true) }
                } ?: appProperties.frontendUrl

        response.sendRedirect(redirectUrl)
    }
}
