package org.home.paper.server.dto

import java.util.*

data class IssueView(

    val id: Long,

    val number: String,

    val summary: String = "",

    val seriesId: Long,

    val pagesCount: Int,

    val currentPage: Int = 0,

    val publicationDate: Date
)
