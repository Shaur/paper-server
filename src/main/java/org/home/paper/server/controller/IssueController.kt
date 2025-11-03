package org.home.paper.server.controller

import org.home.paper.server.dto.IssueUpdateRequest
import org.home.paper.server.dto.ReadingProgressUpdate
import org.home.paper.server.service.IssueService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/issue")
class IssueController(
    private val service: IssueService
) {

    @PutMapping("/{id}")
    fun updateProgress(
        @PathVariable("id") id: Long,
        @RequestBody body: ReadingProgressUpdate
    ) = service.updateProgress(id, body)


    @GetMapping("/{id}")
    fun get(@PathVariable id: Long) = service.get(id)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = service.delete(id)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody body: IssueUpdateRequest) {
        service.update(id, body)
    }
}