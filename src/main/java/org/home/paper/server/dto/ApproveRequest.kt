package org.home.paper.server.dto

data class ApproveRequest(
    val seriesUpdate: SeriesUpdateRequest,
    val issueUpdate: IssueUpdateRequest,
    val fileName: String
)

data class SeriesUpdateRequest(
    val id: Long? = null,
    val title: String,
    val publisher: String,
    val startDateTime: Long,
    val endDateTime: Long? = null
)

data class IssueUpdateRequest(
    val number: String? = null,
    val summary: String? = null,
    val pagesCount: Int
)
