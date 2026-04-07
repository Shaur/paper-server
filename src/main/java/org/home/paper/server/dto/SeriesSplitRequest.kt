package org.home.paper.server.dto

data class SeriesSplitRequest(
    val oldSeriesId: Long,
    val issuesIds: List<Long>
)
