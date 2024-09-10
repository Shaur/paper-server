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
    override fun create(issue: Issue): Issue {
        return entityManager.merge(issue)
    }
}