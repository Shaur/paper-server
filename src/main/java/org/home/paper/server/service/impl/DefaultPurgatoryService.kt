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
import org.home.paper.server.service.StorageService
import org.springframework.stereotype.Service

@Service
class DefaultPurgatoryService(
    private val purgatoryRepository: PurgatoryRepository,
    private val issueRepository: IssueRepository,
    private val seriesRepository: SeriesRepository,
    private val storageService: StorageService
) : PurgatoryService {

    override fun insert(meta: ArchiveMeta): PurgatoryItem {
        val item = PurgatoryItem(meta = meta)
        return purgatoryRepository.insert(item)
    }

    override fun getAll(): List<PurgatoryItem> {
        return purgatoryRepository.getAll()
    }

    @Transactional
    override fun delete(id: Long) {
        purgatoryRepository.delete(id)
        storageService.deletePurgatoryDir(id)
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
            seriesId = series.id!!,
            pagesCount = issueUpdate.pagesCount,
            publicationDate = issueUpdate.publicationDate
        )

        val savedIssue = issueRepository.create(issue)

        storageService.transfer(purgatoryId, savedIssue.id!!)

        purgatoryRepository.delete(purgatoryId)
    }
}