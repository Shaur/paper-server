package org.home.paper.server.repository.impl

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.home.paper.server.model.Series
import org.home.paper.server.model.projection.SeriesSearchViewProjection
import org.home.paper.server.repository.SeriesRepository
import org.springframework.stereotype.Repository

@Repository
class DefaultSeriesRepository(
    private val manager: EntityManager
) : SeriesRepository {

    @Transactional
    override fun create(series: Series): Series {
        return manager.merge(series)
    }

    override fun get(id: Long): Series {
        return manager.createQuery("from series s where id=:id", Series::class.java)
            .setParameter("id", id)
            .resultList
            .first()
    }

    @Transactional
    override fun update(series: Series): Series {
        return manager.merge(series)
    }

    override fun findAll(titlePart: String?, limit: Int, offset: Int): List<SeriesSearchViewProjection> {
        val hql = """
            select new SeriesSearchViewProjection(
                s.id, 
                s.title, 
                min(EXTRACT(YEAR FROM i.publicationDate)), 
                max(EXTRACT(YEAR FROM i.publicationDate))
            ) 
            from series s
                left join issue i on i.seriesId = s.id
                where s.title like :pattern
            group by s.id, s.title
        """.trimIndent()

        return manager.createQuery(hql, SeriesSearchViewProjection::class.java)
            .setParameter("pattern", "%${titlePart ?: ""}%")
            .setFirstResult(offset)
            .setMaxResults(limit)
            .resultList
    }
}