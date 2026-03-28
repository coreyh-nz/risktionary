package nz.coreyh.risktionary.auth.domain.model

import nz.coreyh.risktionary.user.domain.model.UserId
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.security.Principal

data class UserPrincipal(
    val userId: UserId,
    private val roles: List<String>,
) : UserDetails,
    Principal {
    override fun getAuthorities(): Collection<GrantedAuthority> = roles.map { SimpleGrantedAuthority(it) }

    override fun getPassword(): String? = null

    override fun getName(): String = userId.toString()

    override fun getUsername(): String = name

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}
