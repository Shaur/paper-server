package org.home.paper.server.archive

import io.github.oshai.kotlinlogging.KotlinLogging
import org.home.paper.server.model.ArchiveMeta
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class CbzTool(fileName: String) : ArchiveTool(fileName) {

    override fun getMeta(input: InputStream): ArchiveMeta {
        val zis = ZipInputStream(input)
        val xmlFile = Files.createTempFile("ComicInfo" + System.currentTimeMillis(), "xml").toFile()
        val descriptors = zis.seq()
            .filter { !it.isDirectory }
            .map {
                if (it.name.contains("ComicInfo.xml")) {
                    zis.copyTo(FileOutputStream(xmlFile))
                }
                it.name
            }
            .toList()

        if (xmlFile.length() > 0) {
            val meta = extractMetaFromXml(xmlFile)
            xmlFile.delete()

            return meta.copy(pagesCount = descriptors.count() - 1)
        }

        val firstPageName = descriptors.first()
        val seriesName = listOf(
            crossNames(fileName, firstPageName).trim(),
            extractSeriesNameFromFileName(fileName)
        )
            .filter { it != fileName && it.isNotEmpty() }
            .minByOrNull { it.length } ?: fileName

        val number = listOf(
            extractNumber(fileName, seriesName),
            extractNumberFromFileName(fileName)
        ).firstOrNull { it.isNotBlank() } ?: ""


        return ArchiveMeta(
            seriesName = seriesName,
            number = number,
            pagesCount = descriptors.count()
        )
    }

    override fun extract(input: InputStream, destination: File) {
        if (!destination.exists()) {
            destination.mkdirs()
        }

        ZipInputStream(input).use { zis ->
            zis.seq()
                .filter { !it.isDirectory && !it.name.endsWith("xml") }
                .forEach { entry ->
                    val name = entry.name.split("/").last()
                    val output = destination.resolve("tmp-$name")
                    zis.copyTo(output.outputStream())
                }
        }

        val sorted = (destination.listFiles() ?: emptyArray())

        val min = sorted.minBy { it.nameWithoutExtension.length }

        if (hasTrashPages(sorted.map { it.nameWithoutExtension })) {
            min.delete()
        }

        val digits = sorted.size.toString().length
        sorted.forEachIndexed { index, file ->
            val filename = String.format("%0${digits}d.jpg", index)
            Files.copy(file.inputStream(), destination.resolve(filename).toPath(), StandardCopyOption.REPLACE_EXISTING)
            file.delete()
        }
    }
}

private fun ZipInputStream.seq(): Sequence<ZipEntry> {
    return generateSequence { this.nextEntry.takeIf { it != null } }
}