package nz.coreyh.risktionary.shared.infrastructure.persistence

import nz.coreyh.risktionary.shared.application.transaction.Transactional
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Component

/**
 * Production implementation of [Transactional] using Exposed's transaction API.
 *
 * This wraps the provided block in an Exposed-managed database transaction,
 * ensuring that all repository operations inside the block are executed
 * atomically.
 */
@Component
class ExposedTransactional : Transactional {
    override fun <T> execute(block: () -> T): T = transaction { block() }
}
