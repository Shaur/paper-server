package org.home.paper.server.controller

import org.home.paper.server.service.IssueService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/issue")
class IssueController(
    private val service: IssueService
) {

    @GetMapping
    fun getBySeriesId(@RequestParam("seriesId") seriesId: Long) = service.getBySeriesId(seriesId)

}