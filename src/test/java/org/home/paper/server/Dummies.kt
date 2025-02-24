package org.home.paper.server

import org.home.paper.server.model.Issue
import org.home.paper.server.model.Series
import org.home.paper.server.model.User
import java.util.*

object Dummies {

    val unsavedUser = User(null, "user", "password")
    val unsavedSeries = Series(null, "test", "someone")


    fun unsavedIssue(seriesId: Long) = Issue(null, "", seriesId = seriesId, pagesCount = 5, publicationDate = Date())
}