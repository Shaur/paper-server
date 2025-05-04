package org.home.paper.server.repository

import org.home.paper.server.model.Issue

interface IssueRepository {

    fun save(issue: Issue): Issue

    fun getById(id: Long): Issue?

    fun getBySeriesId(seriesId: Long): List<Issue>

    fun getBySeriesIds(seriesIds: Collection<Long>): List<Issue>

    fun deleteAll()
}