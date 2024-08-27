package org.home.paper.server.archive

import java.io.File

class ArchiveToolFactory(private val fileName: String) {

    fun create(): ArchiveTool {
        return when(File(fileName).extension) {
            "cbz" -> CbzTool(fileName)
            else -> CbrTool(fileName)
        }
    }

}