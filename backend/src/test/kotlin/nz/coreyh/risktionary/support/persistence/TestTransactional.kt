package nz.coreyh.risktionary.support.persistence

import nz.coreyh.risktionary.shared.application.transaction.Transactional

/**
 * Test-friendly implementation of [Transactional] that executes the block
 * directly without opening a real database transaction.
 */
class TestTransactional : Transactional {
    override fun <T> execute(block: () -> T): T = block()
}
