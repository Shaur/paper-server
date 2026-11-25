package org.home.paper.server.service

import org.home.paper.server.dto.PageSize
import org.home.paper.server.exceptions.FileNotFoundException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class StorageServiceTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `get beyond bounds throws FileNotFound`() {
        val storage = StorageService.Storage(tempDir, tempDir)
        tempDir.resolve("1").mkdirs()                      // каталог выпуска СУЩЕСТВУЕТ
        tempDir.resolve("1/000.jpg").writeBytes(byteArrayOf(1))

        assertThrows(FileNotFoundException::class.java) {
            storage[1L, 5, PageSize.ORIGINAL]
        }

        assertThrows(FileNotFoundException::class.java) {
            storage[1L, -1, PageSize.ORIGINAL]
        }
    }

}