package org.home.paper.server.repository

import org.home.paper.server.dto.SeriesFilter
import org.home.paper.server.model.projection.FilteredSeriesCatalogItemProjection

interface SeriesFilteringRepository {

    fun findByFilter(filter: SeriesFilter, limit: Int, pageNumber: Int): List<FilteredSeriesCatalogItemProjection>

}