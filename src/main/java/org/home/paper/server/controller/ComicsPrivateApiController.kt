package org.home.paper.server.controller

import org.apache.coyote.BadRequestException
import org.home.paper.server.archive.ArchiveToolFactory
import org.home.paper.server.model.PurgatoryItem
import org.home.paper.server.service.PurgatoryService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files

@RestController
@RequestMapping("/private/comics")
class ComicsPrivateApiController(
    private val purgatoryService: PurgatoryService
) {

    private val purgatoryDir = File("purgatory")

    init {
        purgatoryDir.mkdirs()
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestPart file: MultipartFile): PurgatoryItem {
        val name = file.originalFilename ?: throw BadRequestException("File name is null")

        val destFile = purgatoryDir.resolve(name)
        Files.copy(file.inputStream, destFile.toPath())

        val tool = ArchiveToolFactory(name).create()
        val meta = tool.getMeta(destFile.inputStream())

        return purgatoryService.insert(meta)
    }

    @GetMapping("/purgatory")
    fun getPurgatory(): List<PurgatoryItem> {
        return purgatoryService.getAll()
    }

    @DeleteMapping("/purgatory/{id}")
    fun deletePurgatoryItem(@PathVariable id: Long) = purgatoryService.delete(id)

}