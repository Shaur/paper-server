package org.home.paper.server.model.projection

interface SeriesSearchViewProjection : BaseSeriesProjection {
    fun getIsEnded(): Boolean
}
