package nz.coreyh.risktionary.user.domain.model

/**
 * A capability granted to a user, in addition to whatever they can already do
 * as a player or host. Roles are assigned directly in the database for now.
 */
enum class UserRole {
    /** Can read the full persisted record of any game, for research analysis. */
    RESEARCHER,
}
