package org.home.paper.server.service

import org.home.paper.server.model.ArchiveMeta
import org.home.paper.server.model.PurgatoryItem

interface PurgatoryService {

    fun insert(meta: ArchiveMeta): PurgatoryItem

    fun getAll(): List<PurgatoryItem>

    fun delete(id: Long)

}