package org.home.paper.server.service.impl

import org.home.paper.server.dto.SeriesSearchView
import org.home.paper.server.repository.SeriesRepository
import org.home.paper.server.service.SeriesService
import org.springframework.stereotype.Service

@Service
class DefaultSeriesService(
    private val repository: SeriesRepository
) : SeriesService {

    override fun findAll(titlePart: String?, limit: Int, offset: Int): List<SeriesSearchView> {
        return repository.findAll(titlePart, limit, offset)
            .map {
                val title = if (it.minYear == it.maxYear) {
                    "${it.title} (${it.minYear})"
                } else {
                    "${it.title} (${it.minYear} - ${it.maxYear})"
                }

                SeriesSearchView(
                    id = it.id,
                    title = title
                )
            }
    }
}