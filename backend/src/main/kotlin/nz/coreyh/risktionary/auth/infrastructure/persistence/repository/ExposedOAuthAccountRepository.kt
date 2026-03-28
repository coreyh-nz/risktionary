package nz.coreyh.risktionary.auth.infrastructure.persistence.repository

import nz.coreyh.risktionary.auth.domain.model.OAuthAccount
import nz.coreyh.risktionary.auth.domain.model.OAuthProvider
import nz.coreyh.risktionary.auth.domain.repository.OAuthAccountRepository
import nz.coreyh.risktionary.auth.infrastructure.persistence.table.ExposedOAuthAccountTable
import nz.coreyh.risktionary.user.domain.model.UserId
import nz.coreyh.risktionary.user.domain.model.toUserId
import nz.coreyh.risktionary.user.infrastructure.persistence.table.ExposedUserTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insertReturning
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Repository

@Repository
class ExposedOAuthAccountRepository : OAuthAccountRepository {
    override fun findByProviderIdentity(
        provider: OAuthProvider,
        providerUserId: String,
    ): OAuthAccount? =
        transaction {
            ExposedOAuthAccountTable
                .selectAll()
                .where {
                    (ExposedOAuthAccountTable.provider eq provider) and
                        (ExposedOAuthAccountTable.providerUserId eq providerUserId)
                }.firstOrNull()
                ?.toDomain()
        }

    override fun findByUserId(userId: UserId): List<OAuthAccount> =
        transaction {
            ExposedOAuthAccountTable.selectAll().where { ExposedOAuthAccountTable.userId eq userId.value }.map { it.toDomain() }
        }

    override fun create(
        userId: UserId,
        provider: OAuthProvider,
        providerUserId: String,
        email: String,
    ): OAuthAccount =
        transaction {
            ExposedOAuthAccountTable
                .insertReturning {
                    it[ExposedOAuthAccountTable.userId] = EntityID(userId.value, ExposedUserTable)
                    it[ExposedOAuthAccountTable.provider] = provider
                    it[ExposedOAuthAccountTable.providerUserId] = providerUserId
                    it[ExposedOAuthAccountTable.email] = email
                }.firstOrNull()
                ?.toDomain() ?: throw IllegalStateException("Failed to insert oauth account")
        }

    private fun ResultRow.toDomain() =
        OAuthAccount(
            userId = this[ExposedOAuthAccountTable.userId].value.toUserId(),
            provider = this[ExposedOAuthAccountTable.provider],
            providerUserId = this[ExposedOAuthAccountTable.providerUserId],
            email = this[ExposedOAuthAccountTable.email],
        )
}
