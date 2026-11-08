package org.home.paper.server.dto

import java.util.Date

data class SeriesAutocompletionView(
    val id: Long,
    val title: String,
    val publisher: String?,
    val ended: Boolean,
    val firstPublication: Date,
    val lastPublication: Date,
)
