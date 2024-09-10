package org.home.paper.server.service.impl

import jakarta.transaction.Transactional
import org.home.paper.server.dto.ApproveRequest
import org.home.paper.server.model.ArchiveMeta
import org.home.paper.server.model.Issue
import org.home.paper.server.model.PurgatoryItem
import org.home.paper.server.model.Series
import org.home.paper.server.repository.IssueRepository
import org.home.paper.server.repository.PurgatoryRepository
import org.home.paper.server.repository.SeriesRepository
import org.home.paper.server.service.PurgatoryService
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileNotFoundException

@Service
class DefaultPurgatoryService(
    private val purgatoryRepository: PurgatoryRepository,
    private val issueRepository: IssueRepository,
    private val seriesRepository: SeriesRepository
) : PurgatoryService {

    private val purgatoryDir = File("purgatory")

    private val issuesDir = File("issues")

    override fun insert(meta: ArchiveMeta): PurgatoryItem {
        val item = PurgatoryItem(meta = meta)
        return purgatoryRepository.insert(item)
    }

    override fun getAll(): List<PurgatoryItem> {
        return purgatoryRepository.getAll()
    }

    @Transactional
    override fun delete(id: Long) {
        val item = purgatoryRepository.get(id) ?: throw FileNotFoundException("File by id $id not found")

        purgatoryRepository.delete(id)
        purgatoryDir.resolve(item.id.toString()).deleteRecursively()
    }

    @Transactional
    override fun approve(request: ApproveRequest) {
        val (purgatoryId, seriesUpdate, issueUpdate) = request
        val series = if (seriesUpdate.id == null) {
            seriesRepository.create(
                Series(
                    title = seriesUpdate.title,
                    publisher = seriesUpdate.publisher
                )
            )
        } else {
            seriesRepository.get(seriesUpdate.id)
        }

        val issue = Issue(
            number = issueUpdate.number,
            summary = issueUpdate.summary ?: "",
            series = series,
            pagesCount = issueUpdate.pagesCount,
            publicationDate = issueUpdate.publicationDate
        )

        val savedIssue = issueRepository.create(issue)
        purgatoryDir.resolve(purgatoryId.toString()).copyRecursively(issuesDir.resolve(savedIssue.id.toString()), true)

        purgatoryRepository.delete(purgatoryId)
    }
}