package org.home.paper.server.controller

import org.home.paper.server.dto.SeriesAutocompletionView
import org.home.paper.server.dto.SeriesCatalogItemView
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

    @GetMapping("/autocomplete")
    fun findForAutocompletion(
        @RequestParam("title", required = false) titlePart: String?,
        @RequestParam("limit", required = false) limit: Int = 10,
        @RequestParam("offset", required = false) offset: Int = 0
    ): List<SeriesAutocompletionView> {
        return service.findForAutocompletion(titlePart, limit, offset)
    }

    @GetMapping
    fun find(
        @RequestParam("limit", required = false) limit: Int = 10,
        @RequestParam("offset", required = false) offset: Int = 0
    ): List<SeriesCatalogItemView> {
        return service.find(limit, offset)
    }

}