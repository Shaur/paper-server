package org.home.paper.server.service

import java.io.File
import java.io.InputStream

interface StorageService {

    fun deletePurgatoryDir(id: Long)

    fun transfer(purgatoryId: Long, issueId: Long)

    fun storePurgatoryArchive(source: InputStream, name: String): File

    fun resolvePurgatoryDir(id: Long): File

    val purgatory: Purgatory

    interface Purgatory {
        operator fun get(id: Long, number: Int): File
    }
}