package org.home.paper.server.repository.impl

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.home.paper.server.model.Series
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


    override fun findAll(titlePart: String): List<Series> {
        return manager.createQuery("from series s where s.title like :pattern", Series::class.java)
            .setParameter("pattern", "%$titlePart%")
            .resultList
    }
}