package org.home.paper.server.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ObjectNotFoundException(
    type: String,
    id: Long
) : RuntimeException("$type not found with id: $id")