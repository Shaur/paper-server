package org.home.paper.server.service.impl

import org.home.paper.server.dto.SeriesSearchView
import org.home.paper.server.repository.SeriesRepository
import org.home.paper.server.service.SeriesService
import org.springframework.stereotype.Service

@Service
class DefaultSeriesService(
    private val repository: SeriesRepository
) : SeriesService {

    override fun findAll(titlePart: String): List<SeriesSearchView> {
        return repository.findAll(titlePart).map { SeriesSearchView(it.id!!, it.title) }
    }
}