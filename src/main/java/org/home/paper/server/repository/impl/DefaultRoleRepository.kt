package org.home.paper.server.repository.impl

import com.fasterxml.jackson.databind.type.CollectionType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.home.paper.server.model.auth.Role
import org.home.paper.server.repository.RoleRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository

@Repository
class DefaultRoleRepository(
    private val template: JdbcTemplate
) : RoleRepository {

    private companion object {
        val mapper = jacksonObjectMapper()
        val privilegesType: CollectionType =
            mapper.typeFactory.constructCollectionType(List::class.java, String::class.java)
    }

    override fun findAll(): List<Role> {
        return template.query("select * from user_role", handler)
    }

    override fun findByName(name: String): Role? {
        return template.query("select * from user_role where name = ?", handler, name)
            .firstOrNull()
    }

    private val handler: RowMapper<Role> = RowMapper { rs, _ ->
        Role(
            name = rs.getString(1),
            privileges = mapper.readValue(rs.getString(2), privilegesType)
        )
    }
}