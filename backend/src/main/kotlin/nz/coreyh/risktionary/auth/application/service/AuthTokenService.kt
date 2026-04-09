package nz.coreyh.risktionary.auth.application.service

import nz.coreyh.risktionary.auth.config.JwtProperties
import nz.coreyh.risktionary.auth.domain.model.AccessToken
import nz.coreyh.risktionary.shared.application.service.TokenService
import nz.coreyh.risktionary.shared.exception.UnauthenticatedException
import nz.coreyh.risktionary.user.domain.model.User
import nz.coreyh.risktionary.user.domain.model.toUserIdOrNull
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.stereotype.Service
import kotlin.time.Clock

@Service
class AuthTokenService(
    private val tokenService: TokenService,
    private val jwtProperties: JwtProperties,
) {
    fun generateAccessToken(user: User): AccessToken {
        val issuedAt = Clock.System.now()
        val expiresAt = issuedAt + jwtProperties.accessLifetime
        val token =
            tokenService.generateToken(
                subject = user.id.toString(),
                issuedAt = issuedAt,
                expiresAt = expiresAt,
            )
        return AccessToken(
            userId = user.id,
            issuedAt = token.issuedAt,
            expiresAt = token.expiresAt,
            value = token.value,
        )
    }

    fun decodeAccessToken(accessToken: String): AccessToken {
        try {
            val token = tokenService.decodeToken(accessToken)
            val userId =
                token.subject.toUserIdOrNull()
                    ?: throw BadJwtException("Invalid 'subject'")
            return AccessToken(
                userId = userId,
                issuedAt = token.issuedAt,
                expiresAt = token.expiresAt,
                value = token.value,
            )
        } catch (e: BadJwtException) {
            throw UnauthenticatedException(e)
        }
    }
}
