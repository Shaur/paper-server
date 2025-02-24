package org.home.paper.server.service.impl

import org.home.paper.server.dto.IssueView
import org.home.paper.server.dto.ReadingProgressUpdate
import org.home.paper.server.exceptions.ObjectNotFoundException
import org.home.paper.server.exceptions.IllegalStateException
import org.home.paper.server.model.Issue
import org.home.paper.server.model.ReadingProgress
import org.home.paper.server.model.ReadingProgressKey
import org.home.paper.server.model.User
import org.home.paper.server.repository.IssueRepository
import org.home.paper.server.repository.ReadingProgressRepository
import org.home.paper.server.service.IssueService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class DefaultIssueService(
    private val issueRepository: IssueRepository,
    private val readingProgressRepository: ReadingProgressRepository
) : IssueService {

    private companion object {
        const val PAGES_COUNT_VIOLATION_MESSAGE = "Current page bigger, than all pages count"
    }

    override fun updateProgress(id: Long, body: ReadingProgressUpdate) {
        val issue = issueRepository.getById(id) ?: throw ObjectNotFoundException(Issue::class.toString(), id)
        if (issue.pagesCount < body.currentPage) {
            throw IllegalStateException(PAGES_COUNT_VIOLATION_MESSAGE)
        }

        val user = (SecurityContextHolder.getContext().authentication.principal as User)

        val progressOptional = readingProgressRepository.findById(ReadingProgressKey(user.id, id))
        if (progressOptional.isEmpty) {
            readingProgressRepository.save(ReadingProgress(user.id, id, body.currentPage, body.updateTime))
        } else if (progressOptional.get().updateTime < body.updateTime) {
            val progress = progressOptional.get()

            readingProgressRepository.save(progress.update(currentPage = body.currentPage, updateTime = body.updateTime))
        }
    }

    override fun getBySeriesId(seriesId: Long): List<IssueView> {
        val user = (SecurityContextHolder.getContext().authentication.principal as User)
        val userId = user.id

        val issues = issueRepository.getBySeriesId(seriesId)
        val keys = issues.mapNotNull { it.id }.map { ReadingProgressKey(userId, it) }
        val readingHistory = readingProgressRepository.findAllById(keys).associateBy { it.issueId }

        return issues.map {
            IssueView(
                id = it.id!!,
                number = it.number,
                summary = it.summary,
                seriesId = it.seriesId,
                pagesCount = it.pagesCount,
                currentPage = readingHistory[it.id]?.currentPage ?: 0,
                publicationDate = it.publicationDate
            )
        }
    }
}