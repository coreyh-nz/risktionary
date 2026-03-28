package nz.coreyh.risktionary.auth.infrastructure.security

import com.nimbusds.jwt.proc.BadJWTException
import nz.coreyh.risktionary.auth.application.service.TokenService
import nz.coreyh.risktionary.auth.domain.model.Token
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
        claims: Map<String, String>,
    ): Token {
        val claimsSet =
            JwtClaimsSet
                .builder()
                .subject(subject)
                .issuedAt(issuedAt.toJavaInstant())
                .expiresAt(expiresAt.toJavaInstant())
                .apply { claims.forEach { (k, v) -> claim(k, v) } }
                .build()
        val encodedToken = jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).tokenValue
        return Token(
            value = encodedToken,
            subject = subject,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            claims = claims,
        )
    }

    override fun decodeToken(token: String): Token {
        val jwt = jwtDecoder.decode(token)
        return Token(
            value = token,
            subject = jwt.subject,
            issuedAt =
                jwt.issuedAt?.toKotlinInstant()
                    ?: throw BadJWTException("Missing 'issuedAt' claim"),
            expiresAt =
                jwt.issuedAt?.toKotlinInstant()
                    ?: throw BadJWTException("Missing 'expiresAt' claim"),
            claims = jwt.claims.mapValues { it.toString() }, // all should be strings anyway from the issue method
        )
    }
}
