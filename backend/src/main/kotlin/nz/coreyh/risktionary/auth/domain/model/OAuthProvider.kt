package nz.coreyh.risktionary.auth.domain.model

enum class OAuthProvider(
    val id: String,
) {
    GOOGLE("google"),
    MICROSOFT("microsoft"),
    ;

    companion object {
        fun from(id: String): OAuthProvider? = entries.find { it.id == id.lowercase() }
    }
}
