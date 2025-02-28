package org.home.paper.server.service.impl

import org.home.paper.server.dto.SeriesAutocompletionView
import org.home.paper.server.dto.SeriesCatalogItemView
import org.home.paper.server.exceptions.ObjectNotFoundException
import org.home.paper.server.extensions.title
import org.home.paper.server.model.Series
import org.home.paper.server.model.SeriesSubscription
import org.home.paper.server.model.User
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
        pageNumber: Int
    ): List<SeriesAutocompletionView> {
        return repository.findForAutocompletion(titlePart, PageRequest.of(pageNumber, limit))
            .map {
                SeriesAutocompletionView(it.getId(), it.title())
            }
    }

    override fun find(limit: Int, pageNumber: Int): List<SeriesCatalogItemView> {
        val user = (SecurityContextHolder.getContext().authentication.principal as User)
        return repository.find(user.id, PageRequest.of(pageNumber, limit))
            .map { projection ->
                SeriesCatalogItemView(
                    id = projection.getId(),
                    title = projection.title(),
                    publisher = projection.getPublisher(),
                    issuesCount = projection.getIssuesCount(),
                    cover = "/pages/${projection.getMinIssueId()}/0",
                    completedIssuesCount = projection.getCompletedIssuesCount(),
                    subscribed = projection.getSubscribed()
                )
            }
    }

    override fun subscribe(seriesId: Long) {
        val user = (SecurityContextHolder.getContext().authentication.principal as User)
        subscriptionRepository.save(SeriesSubscription(user.id, seriesId))
    }

    override fun unsubscribe(seriesId: Long) {
        val user = (SecurityContextHolder.getContext().authentication.principal as User)
        subscriptionRepository.delete(SeriesSubscription(user.id, seriesId))
    }

    override fun get(id: Long): SeriesCatalogItemView {
        val user = (SecurityContextHolder.getContext().authentication.principal as User)
        return repository.getById(id, user.id) ?: throw ObjectNotFoundException("Series", id)
    }
}