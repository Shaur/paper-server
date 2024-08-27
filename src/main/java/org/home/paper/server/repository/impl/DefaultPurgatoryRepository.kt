package org.home.paper.server.repository.impl

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.home.paper.server.model.PurgatoryItem
import org.home.paper.server.repository.PurgatoryRepository
import org.springframework.stereotype.Repository

@Repository
class DefaultPurgatoryRepository(
    private val manager: EntityManager
) : PurgatoryRepository {

    @Transactional
    override fun insert(item: PurgatoryItem): PurgatoryItem {
        return manager.merge(item)
    }

    override fun get(id: Long): PurgatoryItem? {
        return manager.createQuery("from purgatory p where p.id = :id", PurgatoryItem::class.java)
            .setParameter("id", id)
            .resultList
            .firstOrNull()
    }

    override fun getAll(): List<PurgatoryItem> {
        return manager.createQuery("from purgatory", PurgatoryItem::class.java)
            .resultList
    }

    @Transactional
    override fun delete(id: Long) {
        manager.createQuery("delete from purgatory where id = :id")
            .setParameter("id", id)
            .executeUpdate()
    }
}