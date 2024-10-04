package org.home.paper.server.service

import org.home.paper.server.dto.SeriesAutocompletionView
import org.home.paper.server.dto.SeriesCatalogItemView

interface SeriesService {

    fun findForAutocompletion(titlePart: String?, limit: Int = 10, offset: Int = 0): List<SeriesAutocompletionView>

    fun find(limit: Int, pageNumber: Int): List<SeriesCatalogItemView>
}