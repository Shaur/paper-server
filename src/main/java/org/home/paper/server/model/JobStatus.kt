package org.home.paper.server.model

import jakarta.persistence.EnumeratedValue

enum class JobStatus(@field:EnumeratedValue val json: String) {
    PENDING("pending"),
    STARTED("started"),
    DONE("done"),
    FAILED("failed");
}