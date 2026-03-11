package org.home.paper.server.model.projection

data class FilteredSeriesCatalogItemProjection(
    val id: Long,
    val title: String,
    val publisher: String,
    val minYear: Number?,
    val maxYear: Number?,
    val minIssueId: Number?,
    val maxIssueId: Number?,
    val issuesCount: Number,
    val subscribed: Boolean,
    val completedIssuesCount: Number,
    val isEnded: Boolean
)
