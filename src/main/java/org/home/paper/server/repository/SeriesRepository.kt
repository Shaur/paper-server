package org.home.paper.server.repository

import org.home.paper.server.dto.SeriesSearchView
import org.home.paper.server.model.Series
import org.home.paper.server.model.projection.SeriesSearchViewProjection

interface SeriesRepository {

    fun create(series: Series): Series

    fun get(id: Long): Series

    fun update(series: Series): Series

    fun findAll(titlePart: String): List<SeriesSearchViewProjection>
}