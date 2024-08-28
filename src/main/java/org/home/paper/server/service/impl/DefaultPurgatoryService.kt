package org.home.paper.server.service.impl

import jakarta.transaction.Transactional
import org.home.paper.server.model.ArchiveMeta
import org.home.paper.server.model.PurgatoryItem
import org.home.paper.server.repository.PurgatoryRepository
import org.home.paper.server.service.PurgatoryService
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileNotFoundException

@Service
class DefaultPurgatoryService(
    private val repository: PurgatoryRepository
) : PurgatoryService {

    private val purgatoryDir = File("purgatory")

    override fun insert(meta: ArchiveMeta): PurgatoryItem {
        val item = PurgatoryItem(meta = meta)
        return repository.insert(item)
    }

    override fun getAll(): List<PurgatoryItem> {
        return repository.getAll()
    }

    @Transactional
    override fun delete(id: Long) {
        val item = repository.get(id) ?: throw FileNotFoundException("File by id $id not found")

        repository.delete(id)
        purgatoryDir.resolve(item.id.toString()).deleteRecursively()
    }
}