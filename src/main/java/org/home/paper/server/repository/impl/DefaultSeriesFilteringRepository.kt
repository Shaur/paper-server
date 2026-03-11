package org.home.paper.server.repository.impl

import jakarta.persistence.EntityManager
import org.home.paper.server.dto.SeriesFilter
import org.home.paper.server.extensions.entity
import org.home.paper.server.model.projection.FilteredSeriesCatalogItemProjection
import org.home.paper.server.repository.SeriesFilteringRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Repository

@Repository
class DefaultSeriesFilteringRepository(
    private val em: EntityManager
) : SeriesFilteringRepository {

    private val query = """
            select 
                s.id as id,
                s.title as title,
                s.publisher as publisher,
                min(EXTRACT(YEAR FROM i.publication_date)) as minYear, 
                max(EXTRACT(YEAR FROM i.publication_date)) as maxYear,
                min(i.id) as minIssueId,
                max(i.id) as maxIssueId,
                count(i.id) as issuesCount,
                exists (select 1 from series_subscription ss where ss.series_id = s.id and ss.user_id = :userId) as subscribed,
                count(case when rp.current_page + 1 = i.pages_count then 1 end) as completedIssuesCount,
                s.is_ended as isEnded
            from series s 
                left join issue i on i.series_id = s.id
                left join reading_progress rp on (rp.issue_id = i.id and rp.user_id = :userId) %s
            group by s.id, s.title
            order by count(case when rp.current_page + 1 = i.pages_count then 1 end) = count(i.id), s.title
        """

    override fun findByFilter(
        filter: SeriesFilter,
        limit: Int,
        pageNumber: Int
    ): List<FilteredSeriesCatalogItemProjection> {
        val userId = SecurityContextHolder.getContext().entity().id

        val filterConditions = listOfNotNull(
            filter.namePart?.let { "s.title like :titlePart" }
        )
            .ifEmpty { null }
            ?.joinToString(" and ", prefix = " where ") ?: ""

        val completedQuery = query.format(filterConditions)

        val typedQuery = em.createNativeQuery(completedQuery, FilteredSeriesCatalogItemProjection::class.java)
            .setParameter("userId", userId)
            .setMaxResults(limit)
            .setFirstResult(pageNumber * limit)

        filter.namePart?.let { typedQuery.setParameter("titlePart", "%$it%") }

        return typedQuery.resultList as List<FilteredSeriesCatalogItemProjection>
    }
}