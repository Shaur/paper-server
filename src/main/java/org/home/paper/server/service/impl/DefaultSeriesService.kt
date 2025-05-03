package org.home.paper.server.service.impl

import org.home.paper.server.dto.SeriesAutocompletionView
import org.home.paper.server.dto.SeriesCatalogItemView
import org.home.paper.server.exceptions.ObjectNotFoundException
import org.home.paper.server.extensions.entity
import org.home.paper.server.extensions.title
import org.home.paper.server.model.SeriesSubscription
import org.home.paper.server.model.projection.SeriesCatalogueItemProjection
import org.home.paper.server.repository.SeriesRepository
import org.home.paper.server.repository.SeriesSubscriptionRepository
import org.home.paper.server.service.SeriesService
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class DefaultSeriesService(
    private val repository: SeriesRepository,
    private val subscriptionRepository: SeriesSubscriptionRepository
) : SeriesService {

    override fun findForAutocompletion(
        titlePart: String?,
        limit: Int,
        offset: Int
    ): List<SeriesAutocompletionView> {
        return repository.findForAutocompletion(titlePart, PageRequest.of(offset, limit))
            .map {
                SeriesAutocompletionView(it.getId(), it.title())
            }
    }

    override fun find(limit: Int, pageNumber: Int): List<SeriesCatalogItemView> {
        val user = SecurityContextHolder.getContext().entity()
        return repository.find(user.id, PageRequest.of(pageNumber, limit)).map(::converter)
    }

    override fun subscribe(seriesId: Long) {
        val user = SecurityContextHolder.getContext().entity()
        subscriptionRepository.save(SeriesSubscription(user.id, seriesId))
    }

    override fun unsubscribe(seriesId: Long) {
        val user = SecurityContextHolder.getContext().entity()
        subscriptionRepository.delete(SeriesSubscription(user.id, seriesId))
    }

    override fun get(id: Long): SeriesCatalogItemView {
        val user = SecurityContextHolder.getContext().entity()
        val projection = repository.getById(id, user.id) ?: throw ObjectNotFoundException("Series", id)
        return converter(projection)
    }

    private fun converter(projection: SeriesCatalogueItemProjection): SeriesCatalogItemView {
        return SeriesCatalogItemView(
            id = projection.getId(),
            title = projection.title(),
            publisher = projection.getPublisher(),
            issuesCount = projection.getIssuesCount(),
            cover = "/pages/${projection.getMaxIssueId()}/0",
            completedIssuesCount = projection.getCompletedIssuesCount(),
            subscribed = projection.getSubscribed()
        )
    }
}