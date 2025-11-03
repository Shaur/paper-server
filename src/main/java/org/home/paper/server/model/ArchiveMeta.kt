package org.home.paper.server.model

import java.time.LocalDate

data class ArchiveMeta(
    val seriesName: String,
    val number: String,
    val summary: String = "",
    val publisher: String = "",
    val pagesCount: Int = 0,
    val publicationDate: LocalDate = LocalDate.now()
)