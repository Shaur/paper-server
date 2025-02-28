package org.home.paper.server.controller

import org.home.paper.server.dto.SeriesAutocompletionView
import org.home.paper.server.dto.SeriesCatalogItemView
import org.home.paper.server.service.IssueService
import org.home.paper.server.service.SeriesService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/series")
class SeriesPublicController(
    private val seriesService: SeriesService,
    private val issueService: IssueService
) {

    @GetMapping("/autocomplete")
    fun findForAutocompletion(
        @RequestParam("title", required = false) titlePart: String?,
        @RequestParam("limit", required = false) limit: Int = 10,
        @RequestParam("offset", required = false) offset: Int = 0
    ): List<SeriesAutocompletionView> {
        return seriesService.findForAutocompletion(titlePart, limit, offset)
    }

    @GetMapping
    fun find(
        @RequestParam("limit", required = false) limit: Int = 20,
        @RequestParam("offset", required = false) offset: Int = 0
    ): List<SeriesCatalogItemView> {
        return seriesService.find(limit, offset)
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): SeriesCatalogItemView {
        return seriesService.get(id)
    }

    @GetMapping("/{id}/issues")
    fun getIssues(@PathVariable id: Long) = issueService.getBySeriesId(id)

    @PutMapping("/{id}/subscribe")
    fun subscribe(@PathVariable id: Long) {
        seriesService.subscribe(id)
    }

    @PutMapping("/{id}/unsubscribe")
    fun unsubscribe(@PathVariable id: Long) {
        seriesService.unsubscribe(id)
    }

}