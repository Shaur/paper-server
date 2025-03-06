package org.home.paper.server.controller

import org.home.paper.server.dto.PageSize
import org.home.paper.server.service.StorageService
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/pages")
class PagesController(
    private val storageService: StorageService
) {

    @GetMapping("/{id}/{number}")
    fun getFile(
        @PathVariable id: Long,
        @PathVariable number: Int,
        @RequestParam("size", required = false) size: PageSize = PageSize.ORIGINAL
    ): ResponseEntity<Resource> {
        val file = storageService.page[id, number, size]

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
}