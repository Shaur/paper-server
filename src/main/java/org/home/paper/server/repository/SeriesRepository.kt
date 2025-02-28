package org.home.paper.server.repository

import org.home.paper.server.dto.SeriesCatalogItemView
import org.home.paper.server.model.Series
import org.home.paper.server.model.projection.SeriesCatalogueItemProjection
import org.home.paper.server.model.projection.SeriesSearchViewProjection
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SeriesRepository : CrudRepository<Series, Long> {

    @Query(
        """
            select
                s.id as id, 
                s.title as title, 
                min(EXTRACT(YEAR FROM i.publicationDate)) as minYear, 
                max(EXTRACT(YEAR FROM i.publicationDate)) as maxYear
            from series s
                left join issue i on i.seriesId = s.id
                where s.title like :titlePart
            group by s.id, s.title
        """
    )
    fun findForAutocompletion(titlePart: String?, pageable: Pageable): List<SeriesSearchViewProjection>

    @Query(
        """
            select 
                s.id as id,
                s.title as title,
                s.publisher as publisher,
                min(EXTRACT(YEAR FROM i.publicationDate)) as minYear, 
                max(EXTRACT(YEAR FROM i.publicationDate)) as maxYear,
                min(i.id) as minIssueId,
                count(i.id) as issuesCount,
                exists (select 1 from series_subscription ss where ss.seriesId = s.id and ss.userId = :userId) as subscribed,
                count (CASE WHEN rp.currentPage + 1 = i.pagesCount THEN 1 END) as completedIssuesCount
            from series s 
                left join issue i on i.seriesId = s.id
                left join reading_progress rp on (rp.issueId = i.id and rp.userId = :userId)
            group by s.id, s.title
            order by s.title
        """
    )
    fun find(userId: Long, pageable: Pageable): List<SeriesCatalogueItemProjection>

    @Query(
        """
            select 
                s.id as id,
                s.title as title,
                s.publisher as publisher,
                min(EXTRACT(YEAR FROM i.publicationDate)) as minYear, 
                max(EXTRACT(YEAR FROM i.publicationDate)) as maxYear,
                min(i.id) as minIssueId,
                count(i.id) as issuesCount,
                exists (select 1 from series_subscription ss where ss.seriesId = s.id and ss.userId = :userId) as subscribed,
                count (CASE WHEN rp.currentPage + 1 = i.pagesCount THEN 1 END) as completedIssuesCount
            from series s 
                left join issue i on i.seriesId = s.id
                left join reading_progress rp on (rp.issueId = i.id and rp.userId = :userId)
            where s.id = :id
            group by s.id, s.title
        """
    )
    fun getById(id: Long, userId: Long): SeriesCatalogItemView?
}