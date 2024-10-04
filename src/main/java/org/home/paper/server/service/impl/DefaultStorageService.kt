package org.home.paper.server.service.impl

import org.home.paper.server.configuration.properties.StorageProperties
import org.home.paper.server.service.StorageService
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Service
class DefaultStorageService(properties: StorageProperties) : StorageService {

    private val purgatoryDir = File(properties.purgatoryPath)
    private val issuesDir = File(properties.issuesPath)

    init {
        if (!issuesDir.exists()) issuesDir.mkdirs()
        if (!purgatoryDir.exists()) purgatoryDir.mkdirs()
    }

    override fun deletePurgatoryDir(id: Long) {
        purgatoryDir.resolve(id.toString()).deleteRecursively()
    }

    override fun transfer(purgatoryId: Long, issueId: Long) {
        purgatoryDir.resolve(purgatoryId.toString())
            .copyRecursively(issuesDir.resolve(issueId.toString()), true)
    }

    override fun storePurgatoryArchive(source: InputStream, name: String): File {
        val dest = purgatoryDir.resolve(name)
        Files.copy(source, dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
        return dest
    }

    override fun resolvePurgatoryDir(id: Long): File = purgatoryDir.resolve(id.toString())

    override val purgatory: StorageService.Purgatory = object : StorageService.Purgatory {
        override operator fun get(id: Long, number: Int): File {
            return purgatoryDir.resolve(id.toString()).listFiles()[number]
        }
    }

    override val page: StorageService.Page = object : StorageService.Page {
        override fun get(id: Long, number: Int): File {
            return issuesDir.resolve(id.toString()).listFiles()[number]
        }

    }

}