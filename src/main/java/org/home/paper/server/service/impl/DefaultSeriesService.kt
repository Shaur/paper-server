package org.home.paper.server.service.impl

import org.home.paper.server.dto.SeriesAutocompletionView
import org.home.paper.server.dto.SeriesCatalogItemView
import org.home.paper.server.extensions.title
import org.home.paper.server.model.projection.SeriesCatalogueItemProjection
import org.home.paper.server.repository.SeriesRepository
import org.home.paper.server.service.SeriesService
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class DefaultSeriesService(
    private val repository: SeriesRepository
) : SeriesService {

    override fun findForAutocompletion(
        titlePart: String?,
        limit: Int,
        pageNumber: Int
    ): List<SeriesAutocompletionView> {
        return repository.findForAutocompletion(titlePart, PageRequest.of(pageNumber, limit))
            .map {
                SeriesAutocompletionView(it.getId(), it.title())
            }
    }

    override fun find(limit: Int, pageNumber: Int): List<SeriesCatalogItemView> {
        return repository.find(PageRequest.of(pageNumber, limit))
            .map { projection ->
                SeriesCatalogItemView(
                    id = projection.getId(),
                    title = projection.title(),
                    publisher = projection.getPublisher(),
                    issuesCount = projection.getIssuesCount(),
                    cover = "/pages/${projection.getMinIssueId()}/0"
                )
            }
    }
}