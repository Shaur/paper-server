package org.home.paper.server

import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity

typealias Entity = Pair<Any?, HttpHeaders?>

inline fun <reified T : Any> TestRestTemplate.put(
    url: String,
    entity: Entity
): ResponseEntity<T> {
    return this.exchange<T>(
        url,
        HttpMethod.PUT,
        HttpEntity(entity.first, entity.second),
    )
}