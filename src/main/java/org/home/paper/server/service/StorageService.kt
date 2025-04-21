package org.home.paper.server.service

import org.home.paper.server.dto.DiskStatsView
import org.home.paper.server.dto.PageSize
import org.home.paper.server.exceptions.FileNotFoundException
import org.imgscalr.Scalr
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO

interface StorageService {

    fun deletePurgatoryDir(id: Long)

    fun transfer(purgatoryId: Long, issueId: Long)

    fun storePurgatoryArchive(source: InputStream, name: String): File

    fun resolvePurgatoryDir(id: Long): File

    fun diskInfo(): DiskStatsView

    val purgatory: Storage

    val page: Storage

    class Storage(
        private val dir: File,
        private val cacheDir: File
    ) {
        companion object {
            private val COMPARATOR = compareBy<File> { it.nameWithoutExtension.length }.then(naturalOrder())
        }

        operator fun get(id: Long, number: Int, size: PageSize): File {
            val files = dir.resolve(id.toString()).listFiles()
                ?: throw FileNotFoundException(id, number)

            val file = files.sortedWith(COMPARATOR)[number]

            return resolveCache(file, size, id)
        }

        private fun resolveCache(originalImg: File, size: PageSize, id: Long): File {
            if (size == PageSize.ORIGINAL) {
                return originalImg
            }

            val issueCacheDir = cacheDir.resolve("$id-${size.scale}x")
            if (!issueCacheDir.exists()) {
                issueCacheDir.mkdirs()
            }

            val scaledFile = issueCacheDir.listFiles()
                ?.firstOrNull { it.name == originalImg.name }

            if (scaledFile != null) {
                return scaledFile
            }

            val inputImage = ImageIO.read(originalImg)
            val resizedImage = Scalr.resize(
                inputImage,
                Scalr.Method.QUALITY,
                inputImage.width / size.scale,
                inputImage.height / size.scale
            )

            val newScaledFile = issueCacheDir.resolve(originalImg.name)
            ImageIO.write(resizedImage, "jpg", newScaledFile)

            return newScaledFile
        }

        fun deleteFile(id: Long, number: Int) {
            val files = dir.resolve(id.toString()).listFiles()
                ?: throw FileNotFoundException(id, number)

            val file = files.sortedWith(COMPARATOR)[number] ?: throw FileNotFoundException(id, number)
            file.delete()

            val scaleDirs = (cacheDir.listFiles() ?: arrayOf<File>())
                .filter { it.name.startsWith("$id-") && it.isDirectory }

            for (scaleDir in scaleDirs) {
                scaleDir.listFiles()
                    ?.find { it.name == file.name }
                    ?.delete()
            }

        }
    }

}