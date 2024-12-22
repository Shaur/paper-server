package org.home.paper.server.service

import org.home.paper.server.model.Issue

interface IssueService {

    fun getBySeriesId(seriesId: Long): List<Issue>

}