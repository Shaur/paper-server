package org.home.paper.server.model

import com.fasterxml.jackson.annotation.JsonValue
import jakarta.persistence.EnumeratedValue

enum class JobStatus(
    @field:EnumeratedValue
    @field:JsonValue
    val json: String
) {
    PENDING("pending"),
    STARTED("started"),
    DONE("done"),
    FAILED("failed");
}