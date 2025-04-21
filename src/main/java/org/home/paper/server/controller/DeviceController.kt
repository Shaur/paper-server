package org.home.paper.server.controller

import org.home.paper.server.service.StorageService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/stats")
@RestController
class DeviceController(
    private val storageService: StorageService
) {

    @GetMapping("/disk")
    fun diskInfo() = storageService.diskInfo()

}