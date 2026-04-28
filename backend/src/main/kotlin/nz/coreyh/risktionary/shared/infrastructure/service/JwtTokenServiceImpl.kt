package nz.coreyh.risktionary.shared.infrastructure.service

import nz.coreyh.risktionary.shared.application.service.TokenService
import nz.coreyh.risktionary.shared.domain.model.Token
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Service
class JwtTokenServiceImpl(
    private val jwtEncoder: JwtEncoder,
    private val jwtDecoder: JwtDecoder,
) : TokenService {
    override fun generateToken(
        subject: String,
        issuedAt: Instant,
        expiresAt: Instant,
        type: String,
        claims: Map<String, String>,
    ): Token {
        val claimsSet =
            JwtClaimsSet
                .builder()
                .subject(subject)
                .issuedAt(issuedAt.toJavaInstant())
                .expiresAt(expiresAt.toJavaInstant())
                .apply {
                    claims.forEach { (k, v) -> claim(k, v) }
                    claim(TYPE_CLAIM, type)
                }.build()
        val encodedToken = jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).tokenValue
        return Token(
            value = encodedToken,
            subject = subject,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            claims = claims,
        )
    }

    override fun decodeToken(
        token: String,
        type: String,
    ): Token {
        val jwt = jwtDecoder.decode(token)
        if (!jwt.hasClaim(TYPE_CLAIM)) {
            throw BadJwtException("Missing '${TYPE_CLAIM}' claim")
        }

        val actualType = jwt.getClaim<String>(TYPE_CLAIM)
        if (actualType != type) {
            throw BadJwtException("Expected type '$type' but got '$actualType'")
        }

        return Token(
            value = token,
            subject = jwt.subject,
            issuedAt =
                jwt.issuedAt?.toKotlinInstant()
                    ?: throw BadJwtException("Missing 'issuedAt' claim"),
            expiresAt =
                jwt.expiresAt?.toKotlinInstant()
                    ?: throw BadJwtException("Missing 'expiresAt' claim"),
            claims =
                jwt.claims
                    .filter { it.value is String }
                    .mapValues { it.value.toString() },
        )
    }

    companion object {
        const val TYPE_CLAIM = "type"
    }
}
