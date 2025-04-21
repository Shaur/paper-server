package org.home.paper.server.service.impl

import org.home.paper.server.configuration.properties.StorageProperties
import org.home.paper.server.dto.DiskStatsView
import org.home.paper.server.service.StorageService
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@Service
class DefaultStorageService(properties: StorageProperties) : StorageService {

    companion object {
        private const val GB_DIV = 1073741824.0
    }

    private val purgatoryDir = File(properties.purgatoryPath)
    private val issuesDir = File(properties.issuesPath)
    private val cacheDir = issuesDir.resolve("cache")
    private val purgatoryCacheDir = purgatoryDir.resolve("cache")

    init {
        if (!issuesDir.exists()) issuesDir.mkdirs()
        if (!purgatoryDir.exists()) purgatoryDir.mkdirs()
        if (!cacheDir.exists()) cacheDir.mkdirs()
        if (!purgatoryCacheDir.exists()) purgatoryCacheDir.mkdirs()
    }

    override fun deletePurgatoryDir(id: Long) {
        purgatoryDir.resolve(id.toString()).deleteRecursively()
        purgatoryCacheDir.resolve(id.toString()).deleteRecursively()
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

    override fun diskInfo(): DiskStatsView {
        return DiskStatsView(
            total = issuesDir.totalSpace / GB_DIV,
            usable = issuesDir.usableSpace / GB_DIV,
            free = issuesDir.freeSpace / GB_DIV
        )
    }

    override val purgatory: StorageService.Storage = StorageService.Storage(purgatoryDir, purgatoryCacheDir)

    override val page: StorageService.Storage = StorageService.Storage(issuesDir, cacheDir)

}