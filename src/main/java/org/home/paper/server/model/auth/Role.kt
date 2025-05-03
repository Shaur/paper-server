package org.home.paper.server.model.auth

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity(name = "user_role")
data class Role(
    @Id
    val name: String,
    val privileges: List<String>
) {

    override fun toString(): String {
        return this::class.simpleName + "(name = $name , privileges = $privileges )"
    }

}