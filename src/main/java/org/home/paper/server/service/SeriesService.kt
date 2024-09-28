package org.home.paper.server.service

import org.home.paper.server.dto.SeriesSearchView
import org.home.paper.server.model.Series

interface SeriesService {
    fun findAll(titlePart: String?, limit: Int = 10, offset: Int = 0): List<SeriesSearchView>
}