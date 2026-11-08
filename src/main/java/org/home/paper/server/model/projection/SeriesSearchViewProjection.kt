package org.home.paper.server.model.projection

import java.util.Date

interface SeriesSearchViewProjection : BaseSeriesProjection {
    fun getIsEnded(): Boolean

    fun getPublisher(): String?

    fun getFirstPublication(): Date

    fun getLastPublication(): Date
}
