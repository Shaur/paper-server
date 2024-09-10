package org.home.paper.server.repository

import org.home.paper.server.model.Issue

interface IssueRepository {
    fun create(issue: Issue): Issue
}