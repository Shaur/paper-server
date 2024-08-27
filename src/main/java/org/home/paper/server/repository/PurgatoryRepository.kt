package org.home.paper.server.repository

import org.home.paper.server.model.PurgatoryItem

interface PurgatoryRepository {

    fun insert(item: PurgatoryItem): PurgatoryItem

    fun get(id: Long): PurgatoryItem?

    fun getAll(): List<PurgatoryItem>

    fun delete(id: Long)
}