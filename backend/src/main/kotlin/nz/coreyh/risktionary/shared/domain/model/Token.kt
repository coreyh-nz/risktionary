package nz.coreyh.risktionary.shared.domain.model

import kotlin.time.Instant

class Token(
    val value: String,
    val subject: String,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val claims: Map<String, String>,
)
