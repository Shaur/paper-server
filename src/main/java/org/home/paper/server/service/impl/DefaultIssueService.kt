package org.home.paper.server.service.impl

import org.home.paper.server.model.Issue
import org.home.paper.server.repository.IssueRepository
import org.home.paper.server.service.IssueService
import org.springframework.stereotype.Service

@Service
class DefaultIssueService(
    private val repository: IssueRepository
) : IssueService {

    override fun getBySeriesId(seriesId: Long): List<Issue> {
        return repository.getBySeriesId(seriesId)
    }

}