package org.home.paper.server.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class FileNotFoundException(id: Long, number: Int) : RuntimeException("File for id $id and number $number not found")