package org.home.paper.server.controller

import org.home.paper.server.dto.ApproveRequest
import org.home.paper.server.dto.PageSize
import org.home.paper.server.model.PurgatoryItem
import org.home.paper.server.service.PurgatoryService
import org.home.paper.server.service.StorageService
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/private/comics")
class ComicsPrivateApiController(
    private val purgatoryService: PurgatoryService,
    private val storageService: StorageService
) {

    @GetMapping("/purgatory")
    fun getPurgatory(): List<PurgatoryItem> {
        return purgatoryService.getAll()
    }

    @PutMapping("/purgatory")
    fun approvePurgatoryItem(@RequestBody request: ApproveRequest) {
        purgatoryService.approve(request)
    }

    @GetMapping("/purgatory/file/{id}/{number}")
    fun getFile(
        @PathVariable id: Long,
        @PathVariable number: Int,
        @RequestParam("size", required = false) size: PageSize = PageSize.ORIGINAL
    ): ResponseEntity<Resource> {
        val file = storageService.purgatory[id, number, size]
        val contentDisposition = ContentDisposition.builder("attachment")
            .filename("$number.jpeg")
            .build()

        return ResponseEntity.ok()
            .contentLength(file.length())
            .contentType(MediaType.IMAGE_JPEG)
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
            .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
            .body(FileSystemResource(file))
    }

    @DeleteMapping("/purgatory/{id}")
    fun deletePurgatoryItem(@PathVariable id: Long) = purgatoryService.delete(id)

    @DeleteMapping("/purgatory/{id}/{number}")
    fun deletePage(
        @PathVariable("id") id: Long,
        @PathVariable("number") number: Int
    ) {
        return purgatoryService.deletePage(id, number)
    }
}