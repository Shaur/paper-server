package org.home.paper.server.archive

import org.home.paper.server.model.ArchiveMeta
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files
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
            pagesCount = descriptors.count(),
            fileName = fileName
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

        val comparator = compareBy<File> { it.nameWithoutExtension.length }.then(naturalOrder())

        val sorted = (destination.listFiles() ?: emptyArray()).sortedWith(comparator)
        sorted.forEachIndexed { index, file ->
            file.renameTo(destination.resolve("$index.jpg"))
        }
    }
}

private fun ZipInputStream.seq(): Sequence<ZipEntry> {
    return generateSequence { this.nextEntry.takeIf { it != null } }
}