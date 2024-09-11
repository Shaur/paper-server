package org.home.paper.server.service

import org.home.paper.server.dto.SeriesSearchView
import org.home.paper.server.model.Series

interface SeriesService {
    fun findAll(titlePart: String): List<SeriesSearchView>
}