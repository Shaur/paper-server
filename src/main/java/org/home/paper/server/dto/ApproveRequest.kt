package org.home.paper.server.dto

import java.util.*

data class ApproveRequest(
    val id: Long,
    val seriesUpdate: SeriesUpdateRequest,
    val issueUpdate: IssueUpdateRequest
)

data class SeriesUpdateRequest(
    val id: Long? = null,
    val title: String,
    val publisher: String
)

data class IssueUpdateRequest(
    val number: String,
    val summary: String? = null,
    val publicationDate: Date,
    val pagesCount: Int
)
