package org.home.paper.server.dto

data class SeriesCatalogItemView(
    val id: Long,
    val title: String,
    val publisher: String,
    val issuesCount: Int,
    val cover: String
)
