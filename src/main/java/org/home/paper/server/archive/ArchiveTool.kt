package org.home.paper.server.archive

import org.home.paper.server.model.ArchiveMeta
import java.io.File
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory
import kotlin.math.min

abstract class ArchiveTool(protected val fileName: String) {

    companion object {
        private val SERIES_NAME_REGEX = Regex("^[A-Za-z\\d)(.\\-\" ']+ \\d{1,3}")
        private val NUMBER_REGEX = "\\d+".toRegex()
    }

    private val xmlBuilder = DocumentBuilderFactory.newInstance()
        .apply { isNamespaceAware = true }
        .newDocumentBuilder()

    private val xpath: XPath = XPathFactory.newInstance().newXPath()
    private val titleExpr: XPathExpression = xpath.compile("/ComicInfo/Title/text()")
    private val numberExpr: XPathExpression = xpath.compile("/ComicInfo/Number/text()")
    private val issueExpr: XPathExpression = xpath.compile("/ComicInfo/Issue/text()")
    private val seriesExpr: XPathExpression = xpath.compile("/ComicInfo/Series/text()")
    private val publisherExpr: XPathExpression = xpath.compile("/ComicInfo/Publisher/text()")
    private val summaryExpr: XPathExpression = xpath.compile("/ComicInfo/Summary/text()")

    abstract fun getMeta(input: InputStream): ArchiveMeta

    abstract fun extract(input: InputStream, destination: File)

    protected fun extractSeriesNameFromFileName(fileName: String): String {
        val normalized = fileName.replace("_", " ")
        val nameWithNumber = SERIES_NAME_REGEX.find(normalized)?.value ?: fileName
        val lastIndexOfSpace = nameWithNumber.lastIndexOf(" ")
        if (lastIndexOfSpace == -1) {
            return fileName
        }

        return nameWithNumber.substring(0, lastIndexOfSpace).trim()
    }

    protected fun extractMetaFromXml(xmlFile: File): ArchiveMeta {
        val document = xmlBuilder.parse(xmlFile)

        val title = (seriesExpr.evaluate(document, XPathConstants.STRING) as String).trim()
        val number = listOf(
            numberExpr.evaluate(document, XPathConstants.STRING) as String,
            issueExpr.evaluate(document, XPathConstants.STRING) as String,
            titleExpr.evaluate(document, XPathConstants.STRING) as String
        )
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() } ?: ""

        return ArchiveMeta(
            seriesName = title,
            publisher = publisherExpr.evaluate(document, XPathConstants.STRING) as String,
            summary = summaryExpr.evaluate(document, XPathConstants.STRING) as String,
            number = extractFirstNumber(number),
            pagesCount = 0
        )
    }

    protected fun extractNumberFromFileName(fileName: String): String {
        val normalized = fileName.replace("_", " ")
        val nameWithNumber = SERIES_NAME_REGEX.find(normalized)?.value ?: fileName
        val lastIndexOfSpace = nameWithNumber.lastIndexOf(" ")
        if (lastIndexOfSpace == -1) {
            return fileName
        }

        val number = nameWithNumber.substring(lastIndexOfSpace).trim()
        return (number.toIntOrNull() ?: number.toDoubleOrNull() ?: number).toString()
    }

    protected fun extractNumber(fileName: String, seriesName: String): String {
        val substring = fileName.substring(seriesName.length + 1)
        val indexOfSpace = substring.indexOf(" ")
        if (indexOfSpace == -1) {
            return ""
        }

        var number = substring.substring(0, indexOfSpace).trim()
        number = NUMBER_REGEX.find(number)?.value ?: ""

        return (number.toIntOrNull() ?: number.toDoubleOrNull() ?: number).toString()
    }

    private fun extractFirstNumber(str: String): String {
        val number = NUMBER_REGEX.find(str)?.value ?: ""

        return (number.toIntOrNull() ?: number.toDoubleOrNull() ?: number).toString()
    }

    protected fun crossNames(name1: String, name2: String): String {
        val min = min(name1.length, name2.length)

        val name1LowerCase = name1.lowercase()
        val name2LowerCase = name2.lowercase()

        val builder: StringBuilder = StringBuilder()
        for (i in 0 until min) {
            if (name1LowerCase[i] == ' ' || name2LowerCase[i] == ' ') {
                builder.append(' ')
                continue
            }

            if (name1LowerCase[i] != name2LowerCase[i]) {
                break
            }

            builder.append(name1[i])
        }

        return builder.toString()
    }

}