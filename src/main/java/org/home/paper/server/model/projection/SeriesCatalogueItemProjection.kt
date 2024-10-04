package org.home.paper.server.model.projection

interface SeriesCatalogueItemProjection : BaseSeriesProjection {
    fun getPublisher(): String
    fun getMinIssueId(): Long
    fun getIssuesCount(): Int
}
