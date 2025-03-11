package org.home.paper.server.service.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import org.home.paper.server.configuration.properties.StorageProperties
import org.home.paper.server.dto.PageSize
import org.home.paper.server.service.StorageService
import org.imgscalr.Scalr
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO

@Service
class DefaultStorageService(properties: StorageProperties) : StorageService {

    private val purgatoryDir = File(properties.purgatoryPath)
    private val issuesDir = File(properties.issuesPath)
    private val cacheDir = issuesDir.resolve("cache")

    init {
        if (!issuesDir.exists()) issuesDir.mkdirs()
        if (!purgatoryDir.exists()) purgatoryDir.mkdirs()
        if (!cacheDir.exists()) cacheDir.mkdirs()
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
            return purgatoryDir.resolve(id.toString()).listFiles()
                .sortedWith(COMPARATOR)[number]
        }
    }

    override val page: StorageService.Page = object : StorageService.Page {
        override fun get(id: Long, number: Int, size: PageSize): File {
            val file = issuesDir.resolve(id.toString()).listFiles()
                .sortedWith(COMPARATOR)[number]

            if (size == PageSize.ORIGINAL) {
                return file
            }

            val issueCacheDir = cacheDir.resolve("$id-${size.scale}x")
            if (!issueCacheDir.exists()) {
                issueCacheDir.mkdirs()
            }

            val scaledFile = issueCacheDir
                .listFiles()
                ?.sortedWith(COMPARATOR)
                ?.getOrNull(number)

            if (scaledFile != null) {
                return scaledFile
            }

            val inputImage = ImageIO.read(file)
            val resizedImage = Scalr.resize(
                inputImage,
                Scalr.Method.QUALITY,
                inputImage.width / size.scale,
                inputImage.height / size.scale
            )

            val newScaledFile = issueCacheDir.resolve("$number.jpeg")
            ImageIO.write(resizedImage, "jpg", newScaledFile)

            return newScaledFile
        }

    }

    companion object {
        private val COMPARATOR = compareBy<File> { it.nameWithoutExtension.length }.then(naturalOrder())
    }
}