package org.home.paper.server.service

import org.home.paper.server.dto.SeriesAutocompletionView
import org.home.paper.server.dto.SeriesCatalogItemView
import org.home.paper.server.dto.SeriesFilter
import org.home.paper.server.dto.SeriesUpdateRequest

interface SeriesService {

    fun findForAutocompletion(titlePart: String?, limit: Int = 10, offset: Int = 0): List<SeriesAutocompletionView>

    fun find(limit: Int, pageNumber: Int): List<SeriesCatalogItemView>

    fun findByFilter(filter: SeriesFilter, limit: Int, pageNumber: Int): List<SeriesCatalogItemView>

    fun subscribe(seriesId: Long)

    fun unsubscribe(seriesId: Long)

    fun get(id: Long): SeriesCatalogItemView

    fun merge(ids: List<Long>)

    fun update(id: Long, update: SeriesUpdateRequest)

    fun split(oldSeriesId: Long, issuesIds: List<Long>)
}