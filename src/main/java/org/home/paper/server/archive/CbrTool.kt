package org.home.paper.server.archive

import com.github.junrar.Archive
import com.github.junrar.Junrar
import org.home.paper.server.model.ArchiveMeta
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files

class CbrTool(fileName: String) : ArchiveTool(fileName) {

    override fun getMeta(input: InputStream): ArchiveMeta {
        val xmlFile = Files.createTempFile("ComicsInfo" + System.currentTimeMillis(), "xml").toFile()
        val archive = Archive(input)
        val descriptors = archive.fileHeaders
            .map {
                if (it.fileName.contains("ComicInfo.xml")) {
                    archive.extractFile(it, FileOutputStream(xmlFile))
                }

                it.fileName
            }

        if (xmlFile.length() > 0) {
            val meta = extractMetaFromXml(xmlFile)
            xmlFile.delete()

            return meta.copy(pagesCount = archive.count() - 1)
        }

        val seriesName = extractSeriesNameFromFileName(fileName)

        val number = listOf(
            extractNumber(fileName, seriesName),
            extractNumberFromFileName(fileName)
        ).firstOrNull { it.isNotBlank() } ?: ""


        return ArchiveMeta(
            seriesName = seriesName,
            number = number,
            pagesCount = descriptors.count(),
            fileName = fileName
        )
    }

    override fun extract(input: InputStream, destination: File) {
        if (!destination.exists()) {
            destination.mkdirs()
        }

        val files = Junrar.extract(input, destination)
        files.filter { it.extension == "xml" }
            .forEach {
                files.remove(it)
                it.delete()
            }

        val parentFile = files.first().parentFile
        if (parentFile != destination) {
            files.forEach { it.renameTo(destination.resolve(it.name)) }
            parentFile?.deleteRecursively()
        }
    }

}
