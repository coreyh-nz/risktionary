package nz.coreyh.risktionary.auth.infrastructure.persistence.table

import nz.coreyh.risktionary.auth.domain.model.OAuthProvider
import nz.coreyh.risktionary.user.infrastructure.persistence.table.ExposedUserTable
import org.jetbrains.exposed.v1.core.Table

object ExposedOAuthAccountTable : Table("risktionary_oauth_account") {
    val userId = reference("user_id", ExposedUserTable)
    val provider = enumerationByName<OAuthProvider>("provider", 32)
    val providerUserId = varchar("provider_user_id", 128)
    val email = varchar("email", 256)

    override val primaryKey = PrimaryKey(userId, provider)

    init {
        index(true, provider, providerUserId)
    }
}
