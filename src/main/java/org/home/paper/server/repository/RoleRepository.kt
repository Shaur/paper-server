package org.home.paper.server.repository

import org.home.paper.server.model.auth.Role

interface RoleRepository {

    fun findAll(): List<Role>

    fun findByName(name: String): Role?
}