package org.home.paper.server.repository

import org.home.paper.server.model.Series

interface SeriesRepository {

    fun create(series: Series): Series

    fun get(id: Long): Series

    fun update(series: Series): Series
}