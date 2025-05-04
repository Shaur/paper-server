package org.home.paper.server.repository.impl

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.home.paper.server.model.Issue
import org.home.paper.server.repository.IssueRepository
import org.springframework.stereotype.Repository

@Repository
class DefaultIssueRepository(
    private val entityManager: EntityManager
) : IssueRepository {

    @Transactional
    override fun save(issue: Issue): Issue {
        return entityManager.merge(issue)
    }

    override fun getById(id: Long): Issue? {
        return entityManager.createQuery("select i from issue i where i.id = :id", Issue::class.java)
            .setParameter("id", id)
            .resultList
            .firstOrNull()
    }

    override fun getBySeriesId(seriesId: Long): List<Issue> {
        return entityManager.createQuery("select i from issue i where i.seriesId = :seriesId", Issue::class.java)
            .setParameter("seriesId", seriesId)
            .resultList
    }

    override fun getBySeriesIds(seriesIds: Collection<Long>): List<Issue> {
        return entityManager.createQuery("select i from issue i where i.seriesId in (:seriesIds)", Issue::class.java)
            .setParameter("seriesIds", seriesIds)
            .resultList
    }

    @Transactional
    override fun deleteAll() {
        entityManager.createQuery("delete from issue").executeUpdate()
    }


}