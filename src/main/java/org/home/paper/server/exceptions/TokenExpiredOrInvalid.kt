package org.home.paper.server.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.FORBIDDEN)
class TokenExpiredOrInvalid : RuntimeException("Token expired or invalid")