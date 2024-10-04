package org.home.paper.server.controller

import org.home.paper.server.service.StorageService
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pages")
class PagesController(
    private val storageService: StorageService
) {

    @GetMapping("/{id}/{number}")
    fun getFile(@PathVariable id: Long, @PathVariable number: Int): ResponseEntity<Resource> {
        val file = storageService.page[id, number]
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