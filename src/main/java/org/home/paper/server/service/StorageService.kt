package org.home.paper.server.service

interface StorageService {

    fun deletePurgatoryDir(id: Long)

    fun transfer(purgatoryId: Long, issueId: Long)
}