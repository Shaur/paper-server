package org.home.paper.server.service

import org.home.paper.server.dto.IssueView
import org.home.paper.server.dto.ReadingProgressUpdate

interface IssueService {

    fun getBySeriesId(seriesId: Long): List<IssueView>

    fun updateProgress(id: Long, body: ReadingProgressUpdate)

    fun get(id: Long): IssueView

}