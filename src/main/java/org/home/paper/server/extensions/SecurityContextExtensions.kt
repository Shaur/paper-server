package org.home.paper.server.extensions

import org.home.paper.server.model.auth.AuthentificationEntity
import org.springframework.security.core.context.SecurityContext

fun SecurityContext.entity(): AuthentificationEntity {
    return this.authentication.principal as AuthentificationEntity
}