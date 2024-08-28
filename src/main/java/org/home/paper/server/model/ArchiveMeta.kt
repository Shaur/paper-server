package org.home.paper.server.model

data class ArchiveMeta(
    val seriesName: String,
    val number: String,
    val summary: String = "",
    val publisher: String = "",
    val pagesCount: Int = 0
)