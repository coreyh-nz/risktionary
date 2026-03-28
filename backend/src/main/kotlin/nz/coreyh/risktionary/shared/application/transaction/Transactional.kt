package nz.coreyh.risktionary.shared.application.transaction

/**
 * Abstraction for executing a block of code within a transactional boundary.
 *
 * This interface allows application services to wrap multiple repository calls
 * in a single atomic operation without depending directly on the underlying
 * persistence framework.
 */
interface Transactional {
    fun <T> execute(block: () -> T): T
}
