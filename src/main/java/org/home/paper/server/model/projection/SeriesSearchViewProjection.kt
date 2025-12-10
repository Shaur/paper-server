package org.home.paper.server.model.projection

import java.util.Date

interface SeriesSearchViewProjection : BaseSeriesProjection {
    fun getIsEnded(): Boolean

    fun getFirstPublication(): Date

    fun getLastPublication(): Date
}
