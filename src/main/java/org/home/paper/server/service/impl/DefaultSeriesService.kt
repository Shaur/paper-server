package org.home.paper.server.service.impl

import org.home.paper.server.dto.SeriesAutocompletionView
import org.home.paper.server.dto.SeriesCatalogItemView
import org.home.paper.server.dto.SeriesFilter
import org.home.paper.server.dto.SeriesUpdateRequest
import org.home.paper.server.exceptions.ObjectNotFoundException
import org.home.paper.server.extensions.entity
import org.home.paper.server.extensions.title
import org.home.paper.server.model.SeriesSubscription
import org.home.paper.server.model.projection.FilteredSeriesCatalogItemProjection
import org.home.paper.server.model.projection.SeriesCatalogueItemProjection
import org.home.paper.server.repository.IssueRepository
import org.home.paper.server.repository.SeriesFilteringRepository
import org.home.paper.server.repository.SeriesRepository
import org.home.paper.server.repository.SeriesSubscriptionRepository
import org.home.paper.server.service.SeriesService
import org.springframework.data.domain.PageRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultSeriesService(
    private val seriesRepository: SeriesRepository,
    private val filteredSeriesRepository: SeriesFilteringRepository,
    private val issueRepository: IssueRepository,
    private val subscriptionRepository: SeriesSubscriptionRepository
) : SeriesService {

    override fun findForAutocompletion(
        limit: Int,
        offset: Int
    ): List<SeriesAutocompletionView> {
        return seriesRepository.findForAutocompletion(PageRequest.of(offset, limit))
            .map {
                SeriesAutocompletionView(
                    id = it.getId(),
                    title = it.title(),
                    ended = it.getIsEnded(),
                    firstPublication = it.getFirstPublication(),
                    lastPublication = it.getLastPublication()
                )
            }
    }

    override fun find(limit: Int, pageNumber: Int): List<SeriesCatalogItemView> {
        val user = SecurityContextHolder.getContext().entity()
        return seriesRepository.find(user.id, PageRequest.of(pageNumber, limit))
            .map(::converter)
    }

    override fun findByFilter(
        filter: SeriesFilter,
        limit: Int,
        pageNumber: Int
    ): List<SeriesCatalogItemView> {
        return filteredSeriesRepository.findByFilter(filter, limit, pageNumber)
            .map(::converter)
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
        val projection = seriesRepository.getById(id, user.id) ?: throw ObjectNotFoundException("Series", id)
        return converter(projection)
    }

    @Transactional
    override fun merge(ids: List<Long>) {
        val oldestId = ids.min()
        val seriesForRemove = ids - oldestId

        val issues = issueRepository.getBySeriesIds(seriesForRemove)
        for (issue in issues) {
            issueRepository.save(issue.copy(seriesId = oldestId))
        }

        seriesRepository.deleteAllById(seriesForRemove)
    }

    override fun update(id: Long, update: SeriesUpdateRequest) {
        val series = seriesRepository.getReferenceById(id) ?: throw ObjectNotFoundException("Series", id)
        val updatedSeries = series.copy(isEnded = update.ended)
        seriesRepository.save(updatedSeries)
    }

    private fun converter(projection: SeriesCatalogueItemProjection): SeriesCatalogItemView {
        return SeriesCatalogItemView(
            id = projection.getId(),
            title = projection.title(),
            publisher = projection.getPublisher(),
            issuesCount = projection.getIssuesCount(),
            cover = "/pages/${projection.getMaxIssueId()}/0",
            completedIssuesCount = projection.getCompletedIssuesCount(),
            ended = projection.getIsEnded(),
            subscribed = projection.getSubscribed()
        )
    }

    private fun converter(projection: FilteredSeriesCatalogItemProjection): SeriesCatalogItemView {
        return SeriesCatalogItemView(
            id = projection.id,
            title = projection.title(),
            publisher = projection.publisher,
            issuesCount = projection.issuesCount.toInt(),
            cover = "/pages/${projection.maxIssueId}/0",
            completedIssuesCount = projection.completedIssuesCount.toInt(),
            ended = projection.isEnded,
            subscribed = projection.subscribed
        )
    }
}