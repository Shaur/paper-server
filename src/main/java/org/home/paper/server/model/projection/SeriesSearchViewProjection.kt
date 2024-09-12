package org.home.paper.server.model.projection

data class SeriesSearchViewProjection(
    val id: Long,
    val title: String,
    val minYear: Int,
    val maxYear: Int
)
