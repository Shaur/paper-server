package org.home.paper.server.model.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class AuthentificationEntity(
    val id: Long,
    private val name: String,
    private val password: String,
    private val authorities: Collection<String>
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities.map { SimpleGrantedAuthority(it) }
    }

    override fun getPassword(): String {
        return this.password
    }

    override fun getUsername(): String {
        return name
    }

    fun withPrivileges(authorities: List<String>): AuthentificationEntity {
        return this.copy(authorities = authorities)
    }
}
