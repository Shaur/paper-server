package org.home.paper.server

import org.home.paper.server.model.Issue
import org.home.paper.server.model.Series
import org.home.paper.server.model.auth.User
import java.util.*

object Dummies {

    val unsavedUser = User(null, "user", "password", "user")
    val unsavedAdmin = User(null, "admin", "admin", "admin")
    val unsavedSeries = Series(null, "test", "someone")


    fun unsavedIssue(seriesId: Long, pagesCount: Int = 5) = Issue(null, "", seriesId = seriesId, pagesCount = pagesCount, publicationDate = Date())
}