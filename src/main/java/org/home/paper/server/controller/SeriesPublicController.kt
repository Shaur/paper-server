package org.home.paper.server.controller

import org.home.paper.server.dto.SeriesSearchView
import org.home.paper.server.service.SeriesService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/series")
class SeriesPublicController(
    private val service: SeriesService
) {

    @GetMapping("/find")
    fun findAll(@RequestParam("title") titlePart: String): List<SeriesSearchView> {
        return service.findAll(titlePart)
    }

}