package nz.coreyh.risktionary.shared.application.service

import nz.coreyh.risktionary.shared.domain.model.Token
import kotlin.time.Instant

interface TokenService {
    fun generateToken(
        subject: String,
        issuedAt: Instant,
        expiresAt: Instant,
        claims: Map<String, String> = mapOf(),
    ): Token

    fun decodeToken(token: String): Token
}
