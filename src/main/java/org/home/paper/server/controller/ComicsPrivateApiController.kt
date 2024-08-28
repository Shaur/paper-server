package org.home.paper.server.controller

import org.apache.coyote.BadRequestException
import org.home.paper.server.archive.ArchiveToolFactory
import org.home.paper.server.model.PurgatoryItem
import org.home.paper.server.service.PurgatoryService
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

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
    fun upload(@RequestPart file: List<MultipartFile>): List<PurgatoryItem> {
        return file.map(::upload)
    }

    private fun upload(file: MultipartFile): PurgatoryItem {
        val name = file.originalFilename ?: throw BadRequestException("File name is null")

        val destFile = purgatoryDir.resolve(name)
        Files.copy(file.inputStream, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

        val tool = ArchiveToolFactory(name).create()
        val meta = tool.getMeta(destFile.inputStream())
        val uploadedMeta = purgatoryService.insert(meta)

        tool.extract(destFile.inputStream(), purgatoryDir.resolve(uploadedMeta.id.toString()))
        destFile.delete()

        return uploadedMeta
    }

    @GetMapping("/purgatory")
    fun getPurgatory(): List<PurgatoryItem> {
        return purgatoryService.getAll()
    }

    @GetMapping("/purgatory/file/{id}/{number}")
    fun getFile(@PathVariable id: Long, @PathVariable number: Int): ResponseEntity<Resource> {
        val file = purgatoryDir.resolve(id).listFiles()[number]
        val contentDisposition = ContentDisposition.builder("attachment")
            .filename("$number.jpeg")
            .build()

        return ResponseEntity
            .ok()
            .contentLength(file.length())
            .contentType(MediaType.IMAGE_JPEG)
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .body(FileSystemResource(file))
    }

    @DeleteMapping("/purgatory/{id}")
    fun deletePurgatoryItem(@PathVariable id: Long) = purgatoryService.delete(id)

    private fun File.resolve(name: Long): File {
        return this.resolve(name.toString())
    }
}