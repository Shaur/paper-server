package org.home.paper.server.dto

import java.util.Date

data class IssueUpdateRequest(
    val number: String? = null,
    val publicationDate: Date? = null,
)
